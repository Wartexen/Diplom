package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Project
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast
import com.example.myapplication.Activity.ProjectDetailsActivity
import com.example.myapplication.Activity.StudentGradingActivity
import com.example.myapplication.Adapter.ProjectAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProjectListActivity : AppCompatActivity() {
    private lateinit var projectsList: List<Project>
    private lateinit var filterButton: FloatingActionButton
    private lateinit var filterLayout: View
    private val TAG = "ProjectListActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_list)

        Log.d(TAG, "onCreate started")

        try {
            val selectedDpp = intent.getStringExtra("selectedDpp")
            val selectedScheduleId = intent.getIntExtra("selectedScheduleId", -1)
            val selectedCommission = intent.getStringExtra("selectedCommission")
            val selectedDate = intent.getStringExtra("selectedDate")

            Log.d(TAG, "Received extras: DPP=$selectedDpp, Commission=$selectedCommission, Date=$selectedDate, ScheduleID=$selectedScheduleId")

            val selectedDppTextView = findViewById<TextView>(R.id.selectedDpp)
            val selectedCommissionTextView = findViewById<TextView>(R.id.selectedCommission)
            val selectedDateTextView = findViewById<TextView>(R.id.selectedDate)

            selectedDppTextView.text = selectedDpp ?: "Не указано"
            selectedCommissionTextView.text = selectedCommission ?: "Не указано"
            selectedDateTextView.text = selectedDate ?: "Не указано"

            // Инициализация Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)

            // Проверка ID расписания
            if (selectedScheduleId != -1) {
                Log.d(TAG, "Getting projects for schedule ID: $selectedScheduleId")
                getProjects(apiService, selectedScheduleId)
            } else {
                Log.e(TAG, "Invalid schedule ID: $selectedScheduleId")
                showToast("Ошибка: неверный ID расписания")
            }

            filterButton = findViewById(R.id.filterButton)
            filterLayout = findViewById(R.id.filterLayout)

            filterButton.setOnClickListener {
                toggleFilterVisibility()
            }
            filterLayout.setOnClickListener{
                toggleFilterVisibility()
            }

            val buttonNext = findViewById<Button>(R.id.buttonNext)
            buttonNext.setOnClickListener {
                try {
                    // Логирование для отладки
                    Log.d(TAG, "Button clicked, preparing intent")

                    // Получаем и проверяем данные перед передачей
                    val dpp = selectedDppTextView.text?.toString() ?: ""
                    val commission = selectedCommissionTextView.text?.toString() ?: ""
                    val date = selectedDateTextView.text?.toString() ?: ""

                    // Проверяем ID расписания
                    val scheduleId = selectedScheduleId
                    if (scheduleId == -1) {
                        throw IllegalStateException("Invalid schedule ID: $scheduleId")
                    }

                    Log.d(TAG, "Selected schedule ID: $scheduleId")

                    // Создаем и настраиваем Intent с полным путем к классу
                    val intent = Intent(this, com.example.myapplication.Activity.StudentGradingActivity::class.java)
                    intent.putExtra("selectedDpp", dpp)
                    intent.putExtra("selectedCommission", commission)
                    intent.putExtra("selectedDate", date)
                    intent.putExtra("selectedScheduleId", scheduleId)

                    // Запускаем активность
                    Log.d(TAG, "Starting StudentGradingActivity")
                    startActivity(intent)
                } catch (e: Exception) {
                    // Логируем ошибку и показываем сообщение пользователю
                    Log.e(TAG, "Error starting activity: ${e.message}", e)
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Ошибка при инициализации: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getProjects(apiService: ApiService, defenseScheduleId: Int) {
        Log.d(TAG, "Calling API to get projects for defense schedule ID: $defenseScheduleId")

        apiService.getProjectsByDefenseSchedule(defenseScheduleId).enqueue(object : Callback<List<Project>> {
            override fun onResponse(call: Call<List<Project>>, response: Response<List<Project>>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "API call successful, status code: ${response.code()}")

                    response.body()?.let { projects ->
                        Log.d(TAG, "Received ${projects.size} projects")
                        projectsList = projects // Сохраняем проекты в переменной класса
                        setupRecyclerView(projects) // Настройка RecyclerView после получения данных
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        showToast("Ответ пустой")
                    }
                } else {
                    Log.e(TAG, "API call failed, status code: ${response.code()}")
                    showToast("Ошибка: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Project>>, t: Throwable) {
                Log.e(TAG, "API call failed with exception: ${t.message}", t)
                showToast("Ошибка: ${t.message}")
            }
        })
    }

    private fun setupRecyclerView(projects: List<Project>) {
        Log.d(TAG, "Setting up RecyclerView with ${projects.size} projects")

        try {
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProjects)
            recyclerView.layoutManager = LinearLayoutManager(this)
            val adapter = ProjectAdapter(projects)
            recyclerView.adapter = adapter

            adapter.onProjectClickListener = { project ->
                Log.d(TAG, "Project clicked: ${project.Title}")

                val intent = Intent(this, ProjectDetailsActivity::class.java)
                intent.putExtra("project", project)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView: ${e.message}", e)
            showToast("Ошибка при настройке списка проектов: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun toggleFilterVisibility() {
        if (filterLayout.visibility == View.VISIBLE) {
            filterLayout.visibility = View.GONE
        } else {
            filterLayout.visibility = View.VISIBLE
        }
    }
}
