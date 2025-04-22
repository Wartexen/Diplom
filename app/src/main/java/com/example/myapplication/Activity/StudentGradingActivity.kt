package com.example.myapplication.Activity

import android.os.Bundle
import android.util.Log
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
import com.example.myapplication.Models.GradeRequest
import com.example.myapplication.Models.GradeResponse
import com.example.myapplication.Models.Project
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
    private val studentGrades = mutableListOf<StudentGrade>()
    private val TAG = "StudentGradingActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_grading)

        Log.d(TAG, "onCreate started")

        try {
            val selectedDpp = intent.getStringExtra("selectedDpp") ?: ""
            val selectedCommission = intent.getStringExtra("selectedCommission") ?: ""
            val selectedDate = intent.getStringExtra("selectedDate") ?: ""
            val selectedScheduleId = intent.getIntExtra("selectedScheduleId", -1)

            Log.d(TAG, "Received data: DPP=$selectedDpp, Commission=$selectedCommission, Date=$selectedDate, ScheduleID=$selectedScheduleId")

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
                Log.d(TAG, "Getting projects for schedule ID: $selectedScheduleId")
                getProjects(apiService, selectedScheduleId)
            } else {
                Log.e(TAG, "Invalid schedule ID: $selectedScheduleId")
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
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Ошибка при инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun getProjects(apiService: ApiService, defenseScheduleId: Int) {
        Log.d(TAG, "Calling API to get projects for defense schedule ID: $defenseScheduleId")

        apiService.getProjectsBydefense_schedule_id(defenseScheduleId).enqueue(object : Callback<List<Project>> {
            override fun onResponse(call: Call<List<Project>>, response: Response<List<Project>>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "API call successful, status code: ${response.code()}")

                    val projects = response.body() ?: emptyList()
                    Log.d(TAG, "Received ${projects.size} projects")

                    if (projects.isEmpty()) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@StudentGradingActivity, "Нет проектов для оценивания", Toast.LENGTH_SHORT).show()
                        return
                    }

                    var loadedProjects = 0
                    for (project in projects) {
                        val projectId = project.ID.toInt()
                        getStudentsByProject(apiService, projectId, project.Title) {
                            loadedProjects++
                            Log.d(TAG, "Loaded students for project $loadedProjects of ${projects.size}")

                            // Если загрузили всех студентов, обновляем UI
                            if (loadedProjects == projects.size) {
                                Log.d(TAG, "All students loaded, setting up adapter")
                                setupAdapter()
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "API call failed, status code: ${response.code()}")
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@StudentGradingActivity, "Ошибка при загрузке проектов: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Project>>, t: Throwable) {
                Log.e(TAG, "API call failed with exception: ${t.message}", t)
                progressBar.visibility = View.GONE
                Toast.makeText(this@StudentGradingActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getStudentsByProject(apiService: ApiService, projectId: Int, projectTitle: String, callback: () -> Unit) {
        Log.d(TAG, "Getting students for project ID: $projectId")

        apiService.getStudentsByProject(projectId).enqueue(object : Callback<List<Student>> {
            override fun onResponse(call: Call<List<Student>>, response: Response<List<Student>>) {
                if (response.isSuccessful) {
                    val students = response.body() ?: emptyList()

                    Log.d(TAG, "Received ${students.size} students for project ID: $projectId")

                    for (student in students) {
                        studentGrades.add(
                            StudentGrade(
                                id = student.ID,
                                name = "${student.Surname} ${student.Name} ${student.Patronymic}",
                                projectTitle = projectTitle,

                                )
                        )
                    }
                    callback()
                } else {
                    Log.e(TAG, "Error getting students: ${response.code()}")
                    Toast.makeText(this@StudentGradingActivity, "Ошибка при загрузке студентов: ${response.code()}", Toast.LENGTH_SHORT).show()
                    callback()
                }
            }

            override fun onFailure(call: Call<List<Student>>, t: Throwable) {
                Log.e(TAG, "Network error getting students: ${t.message}")
                Toast.makeText(this@StudentGradingActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                callback()
            }
        })
    }

    private fun setupAdapter() {
        progressBar.visibility = View.GONE

        if (studentGrades.isEmpty()) {
            Toast.makeText(this, "Нет студентов для оценивания", Toast.LENGTH_SHORT).show()
            return
        }

        adapter = StudentGradeAdapter(studentGrades)
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
                Log.d(
                    TAG,
                    "Оценка ${studentGrade.grade} для студента: ${studentGrade.id}"
                )

                // Используем метод API для отправки оценки
                apiService.gradeStudent(gradeRequest).enqueue(object : Callback<GradeResponse> {
                    override fun onResponse(call: Call<GradeResponse>, response: Response<GradeResponse>) {
                        submittedCount++
                        if (response.isSuccessful) {
                            Log.d(TAG, "Оценка сохранена для ${studentGrade.id}")
                        } else {
                            errorCount++
                            Log.e(TAG, "Ошибка сервера: ${response.code()}")
                        }
                        checkAllGradesSubmitted(submittedCount, errorCount, grades.size)
                    }

                    override fun onFailure(call: Call<GradeResponse>, t: Throwable) {
                        submittedCount++
                        errorCount++
                        Log.e(TAG, "Ошибка: ${t.message}")
                        checkAllGradesSubmitted(submittedCount, errorCount, grades.size)
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при оценке: ${e.message}")
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
}
