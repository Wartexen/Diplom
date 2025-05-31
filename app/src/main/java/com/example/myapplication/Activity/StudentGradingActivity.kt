package com.example.myapplication.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import com.example.myapplication.Adapter.StudentGradeAdapter
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Db.Project
import com.example.myapplication.Models.Db.ProjectWithStudents
import com.example.myapplication.Models.Db.Student
import com.example.myapplication.Models.Db.StudentGrade
import com.example.myapplication.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


class StudentGradingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentGradeAdapter
    private lateinit var buttonFinish: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var apiService: ApiService
    private val projectsWithStudents = mutableListOf<ProjectWithStudents>()
    private lateinit var sharedPref: SharedPreferences
    private var selectedScheduleId: Int? = null
    private var secretaryId: Int = -1
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var profileIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_grading)
        sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        toolbar = findViewById(R.id.toolbar)
        profileIcon = findViewById(R.id.profileIcon)
        checkAuthStatus()
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

            // Инициализируем Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl("http://172.20.10.5:8000/")
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
                finishGrading()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun getProjects(apiService: ApiService, defenseScheduleId: Int) {
        apiService.getProjectsByDefenseSchedule(defenseScheduleId).enqueue(object : Callback<List<Project>> {
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
                        val projectId = project.ID
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
                        val fullName = "${student.Surname ?: ""} ${student.Name ?: ""} ${student.Patronymic ?: ""}".trim()
                        val studentGrade = StudentGrade(
                            id = student.ID,
                            name = fullName,
                            projectTitle = projectWithStudents.projectTitle,
                            grade = student.grade ?: "",
                            groupName = student.ID_Group?.Name ?: ""
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

       adapter = StudentGradeAdapter(projectsWithStudents, apiService)
       recyclerView.adapter = adapter
   }

    private fun finishGrading() {
        if (!adapter.areAllStudentsGraded()) {
            Toast.makeText(this, "Пожалуйста, оцените всех студентов", Toast.LENGTH_SHORT).show()
            return
        }
        progressBar.visibility = View.VISIBLE
        buttonFinish.isEnabled = false
        FileUtils.deleteSavedAudioFiles(this@StudentGradingActivity)
        Toast.makeText(
            this,
            "Все оценки сохранены. Аудиофайлы удалены",
            Toast.LENGTH_SHORT
        ).show()

        startActivity(Intent(this@StudentGradingActivity, MainActivity::class.java))
        finish()
    }

    object FileUtils {
        fun deleteSavedAudioFiles(context: Context) {
            val audioDir = File(context.filesDir, "saved_audio")
            deleteDirectory(audioDir)
            deleteDirectory(context.externalCacheDir)
            deleteDirectory(context.cacheDir)

            val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().remove("saved_audio_files").apply()
        }

        private fun deleteDirectory(directory: File?) {
            directory?.let {
                if (it.exists() && it.isDirectory) {
                    it.listFiles()?.forEach { file ->
                        if (file.isDirectory) {
                            deleteDirectory(file)
                        } else {
                            file.delete()
                        }
                    }
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
            profileIcon.setOnClickListener {
                showProfilePopup(it)
            }
        }
    }

    private fun showProfilePopup(anchor: View) {
        val popup = PopupMenu(this, anchor)
        val menu = popup.menu
        popup.menuInflater.inflate(R.menu.profile_menu, menu)

        // Добавляем ФИО как первый элемент
        val fullName = sharedPref.getString("fullName", "") ?: "Не указано"
        menu.add(Menu.NONE, Menu.NONE, 0, fullName).isEnabled = false

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
