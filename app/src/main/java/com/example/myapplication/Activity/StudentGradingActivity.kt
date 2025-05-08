package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.StudentGradeAdapter
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Project
import com.example.myapplication.Models.ProjectWithStudents
import com.example.myapplication.Models.Requests.GradeRequest
import com.example.myapplication.Models.Response.GradeResponse
import com.example.myapplication.Models.Student
import com.example.myapplication.Models.StudentGrade
import com.example.myapplication.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StudentGradingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentGradeAdapter
    private lateinit var buttonFinish: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var apiService: ApiService
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
                        val fullName = "${student.Surname ?: ""} ${student.Name ?: ""} ${student.Patronymic ?: ""}"
                        val studentGrade = StudentGrade(
                            id = student.ID,
                            name = fullName,
                            projectTitle = projectWithStudents.projectTitle,
                            grade = "",
                            //groupName = student.GroupName ?: "Группа не указана"
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

        adapter = StudentGradeAdapter(projectsWithStudents)
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
                    ID_Student = studentGrade.id,
                    Grade = studentGrade.grade
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
                    startActivity(Intent(this@StudentGradingActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Ошибка при сохранении $errorCount оценок", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
