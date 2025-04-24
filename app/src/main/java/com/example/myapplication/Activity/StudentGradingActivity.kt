package com.example.myapplication.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.ProjectStudentsAdapter
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.GradeRequest
import com.example.myapplication.Models.GradeResponse
import com.example.myapplication.Models.Project
import com.example.myapplication.Models.ProjectWithStudents
import com.example.myapplication.Models.Student
import com.example.myapplication.Models.StudentGrade
import com.example.myapplication.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StudentGradingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProjectStudentsAdapter
    private lateinit var buttonFinish: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var apiService: ApiService
    private lateinit var sharedPref: SharedPreferences
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var userName: TextView
    private lateinit var profileIcon: ImageView
    private val projectsWithStudents = mutableListOf<ProjectWithStudents>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_grading)

        try {
            val selectedDpp = intent.getStringExtra("selectedDpp") ?: ""
            val selectedCommission = intent.getStringExtra("selectedCommission") ?: ""
            val selectedDate = intent.getStringExtra("selectedDate") ?: ""
            val selectedScheduleId = intent.getIntExtra("selectedScheduleId", -1)

            findViewById<TextView>(R.id.tvSelectedDpp).text = selectedDpp
            findViewById<TextView>(R.id.tvSelectedCommission).text = selectedCommission
            findViewById<TextView>(R.id.tvSelectedDate).text = selectedDate

            progressBar = findViewById(R.id.progressBar)
            progressBar.visibility = View.VISIBLE

            recyclerView = findViewById(R.id.recyclerViewStudents)
            recyclerView.layoutManager = LinearLayoutManager(this)
            // Инициализация SharedPreferences
            sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            // Инициализация элементов Toolbar
            toolbar = findViewById(R.id.toolbar)
            userName = findViewById(R.id.userName)
            profileIcon = findViewById(R.id.profileIcon)
            // Проверка авторизации
            checkAuthStatus()
            // Инициализируем Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)

            if (selectedScheduleId != -1) {
                getProjects(apiService, selectedScheduleId)
            } else {
                Toast.makeText(this, "Ошибка: ID расписания не найден", Toast.LENGTH_SHORT).show()
                finish()
            }
            buttonFinish = findViewById(R.id.buttonFinish)
            buttonFinish.setOnClickListener {
                if (::adapter.isInitialized && adapter.areAllStudentsGraded()) {
                    submitGrades()
                } else {
                    Toast.makeText(this, "Пожалуйста, оцените всех студентов", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun getProjects(apiService: ApiService, defenseScheduleId: Int) {
        apiService.getProjectsBydefense_schedule_id(defenseScheduleId).enqueue(object : Callback<List<Project>> {
            override fun onResponse(call: Call<List<Project>>, response: Response<List<Project>>) {
                if (response.isSuccessful) {
                    val projects = response.body() ?: emptyList()
                    if (projects.isEmpty()) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@StudentGradingActivity, "Нет проектов для оценивания", Toast.LENGTH_SHORT).show()
                        return
                    }

                    var loadedProjects = 0
                    for (project in projects) {
                        val projectId = project.ID.toInt()
                        val projectWithStudents = ProjectWithStudents(project.Title)
                        projectsWithStudents.add(projectWithStudents)

                        getStudentsByProject(apiService, projectId, projectWithStudents) {
                            loadedProjects++
                            if (loadedProjects == projects.size) {
                                setupAdapter()
                            }
                        }
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@StudentGradingActivity, "Ошибка при загрузке проектов: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Project>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@StudentGradingActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getStudentsByProject(apiService: ApiService, projectId: Int, projectWithStudents: ProjectWithStudents, callback: () -> Unit) {
        apiService.getStudentsByProject(projectId).enqueue(object : Callback<List<Student>> {
            override fun onResponse(call: Call<List<Student>>, response: Response<List<Student>>) {
                if (response.isSuccessful) {
                    val students = response.body() ?: emptyList()
                    for (student in students) {
                        val studentGrade = StudentGrade(
                            id = student.ID,
                            name = "${student.Surname} ${student.Name} ${student.Patronymic}",
                            projectTitle = projectWithStudents.projectTitle,
                           // groupName = student.GroupName ?: ""
                        )
                        projectWithStudents.students.add(studentGrade)
                    }
                    callback()
                } else {
                    Toast.makeText(this@StudentGradingActivity, "Ошибка при загрузке студентов: ${response.code()}", Toast.LENGTH_SHORT).show()
                    callback()
                }
            }

            override fun onFailure(call: Call<List<Student>>, t: Throwable) {
                Toast.makeText(this@StudentGradingActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                callback()
            }
        })
    }


    private fun setupAdapter() {
        progressBar.visibility = View.GONE

        if (projectsWithStudents.isEmpty()) {
            Toast.makeText(this, "Нет проектов для оценивания", Toast.LENGTH_SHORT).show()
            return
        }

        adapter = ProjectStudentsAdapter(projectsWithStudents)
        recyclerView.adapter = adapter
    }

    private fun submitGrades() {
        progressBar.visibility = View.VISIBLE
        buttonFinish.isEnabled = false

        val grades = adapter.getGrades()
        var submittedCount = 0
        var errorCount = 0

        if (grades.isEmpty()) {
            progressBar.visibility = View.GONE
            buttonFinish.isEnabled = true
            Toast.makeText(this, "Нет студентов для оценивания", Toast.LENGTH_SHORT).show()
            return
        }

        for (studentGrade in grades) {
            try {
                val gradeRequest = GradeRequest(
                    student_id = studentGrade.id,
                    grade = studentGrade.grade.toString()
                )
                apiService.gradeStudent(gradeRequest).enqueue(object : Callback<GradeResponse> {
                    override fun onResponse(call: Call<GradeResponse>, response: Response<GradeResponse>) {
                        submittedCount++
                        if (response.isSuccessful) {
                        } else {
                            errorCount++
                        }
                        checkAllGradesSubmitted(submittedCount, errorCount, grades.size)
                    }
                    override fun onFailure(call: Call<GradeResponse>, t: Throwable) {
                        submittedCount++
                        errorCount++
                        checkAllGradesSubmitted(submittedCount, errorCount, grades.size)
                    }
                })
            } catch (e: Exception) {
                submittedCount++
                errorCount++
                checkAllGradesSubmitted(submittedCount, errorCount, grades.size)
            }
        }
    }

    private fun checkAllGradesSubmitted(submittedCount: Int, errorCount: Int, totalCount: Int) {
        if (submittedCount == totalCount) {
            runOnUiThread {
                progressBar.visibility = View.GONE
                buttonFinish.isEnabled = true

                if (errorCount == 0) {
                    Toast.makeText(this, "Все оценки успешно сохранены", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Ошибка при сохранении $errorCount оценок", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun checkAuthStatus() {
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            startActivity(Intent(this, BitrixAuthActivity::class.java))
            finish()
        } else {
            // Устанавливаем данные пользователя
            val fullName = sharedPref.getString("fullName", "") ?: ""
            userName.text = formatUserName(fullName)

            // Обработка клика по иконке профиля
            profileIcon.setOnClickListener {
                showProfilePopup(it)
            }
        }
    }

    private fun formatUserName(fullName: String): String {
        return try {
            val parts = fullName.split(" ")
            when {
                parts.size >= 3 -> "${parts[0]} ${parts[1].first()}.${parts[2].first()}."
                parts.size == 2 -> "${parts[0]} ${parts[1].first()}."
                else -> fullName
            }
        } catch (e: Exception) {
            fullName
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                showProfilePopup(findViewById(item.itemId))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showProfilePopup(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Выйти") { _, _ ->
                logout()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun logout() {
        sharedPref.edit().clear().apply()
        val intent = Intent(this, BitrixAuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!sharedPref.getBoolean("isLoggedIn", false)) {
            logout()
        }
    }
}
