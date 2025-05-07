package com.example.myapplication

import android.content.Intent
import android.os.Bundle
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.Activity.ProjectDetailsActivity
import com.example.myapplication.Adapter.ProjectAdapter
import com.example.myapplication.Models.Response.ProjectStatusResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProjectListActivity : AppCompatActivity() {
    private lateinit var projectsList: List<Project>
    private lateinit var filterButton: FloatingActionButton
    private lateinit var filterLayout: View
    private lateinit var apiService: ApiService
    private lateinit var adapter: ProjectAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var selectedScheduleId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_list)

        try {
            val selectedDpp = intent.getStringExtra("selectedDpp")
            val selectedScheduleId = intent.getIntExtra("selectedScheduleId", -1)
            val selectedCommission = intent.getStringExtra("selectedCommission")
            val selectedDate = intent.getStringExtra("selectedDate")

            this.selectedScheduleId = selectedScheduleId

            val selectedDppTextView = findViewById<TextView>(R.id.selectedDpp)
            val selectedCommissionTextView = findViewById<TextView>(R.id.selectedCommission)
            val selectedDateTextView = findViewById<TextView>(R.id.selectedDate)

            selectedDppTextView.text = selectedDpp ?: "Не указано"
            selectedCommissionTextView.text = selectedCommission ?: "Не указано"
            selectedDateTextView.text = selectedDate ?: "Не указано"

            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)

            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
            swipeRefreshLayout.setColorSchemeResources(R.color.status_ready)
            swipeRefreshLayout.setOnRefreshListener {
                refreshProjects()
            }

            if (selectedScheduleId != -1) {
                getProjects(apiService, selectedScheduleId)
            } else {
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
                    val dpp = selectedDppTextView.text?.toString() ?: ""
                    val commission = selectedCommissionTextView.text?.toString() ?: ""
                    val date = selectedDateTextView.text?.toString() ?: ""

                    val scheduleId = selectedScheduleId
                    if (scheduleId == -1) {
                        throw IllegalStateException("Invalid schedule ID: $scheduleId")
                    }

                    val intent = Intent(this, com.example.myapplication.Activity.StudentGradingActivity::class.java)
                    intent.putExtra("selectedDpp", dpp)
                    intent.putExtra("selectedCommission", commission)
                    intent.putExtra("selectedDate", date)
                    intent.putExtra("selectedScheduleId", scheduleId)

                    startActivity(intent)
                } catch (e: Exception) {

                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при инициализации: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshProjects() {
        if (selectedScheduleId != -1) {
            getProjects(apiService, selectedScheduleId)
        } else {
            swipeRefreshLayout.isRefreshing = false
            showToast("Ошибка: неверный ID расписания")
        }
    }

    private fun getProjects(apiService: ApiService, defenseScheduleId: Int) {
        apiService.getProjectsByDefenseSchedule(defenseScheduleId).enqueue(object : Callback<List<Project>> {
            override fun onResponse(call: Call<List<Project>>, response: Response<List<Project>>) {
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    response.body()?.let { projects ->
                        projectsList = projects
                        setupRecyclerView(projects)

                        updateProjectStatuses(projects)
                    } ?: run {
                        showToast("Ответ пустой")
                    }
                } else {
                    showToast("Ошибка: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Project>>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                showToast("Ошибка: ${t.message}")
            }
        })
    }

    private fun updateProjectStatuses(projects: List<Project>) {
        for (project in projects) {
            getProjectStatus(project.ID)
        }
    }

    private fun getProjectStatus(projectId: Int) {
        apiService.getProjectStatus(projectId).enqueue(object : Callback<ProjectStatusResponse> {
            override fun onResponse(call: Call<ProjectStatusResponse>, response: Response<ProjectStatusResponse>) {
                if (response.isSuccessful) {
                    val statusResponse = response.body()
                    if (statusResponse != null) {
                        updateProjectInList(statusResponse.project_id, statusResponse.status)
                    }
                }
            }

            override fun onFailure(call: Call<ProjectStatusResponse>, t: Throwable) {
                showToast("Ошибка: ${t.message}")
            }
        })
    }

    private fun updateProjectInList(projectId: Int, newStatus: Boolean) {
        val updatedProjects = projectsList.map { project ->
            if (project.ID == projectId) {
                Project(project.ID, project.Title, project.Supervisor, newStatus)
            } else {
                project
            }
        }
        projectsList = updatedProjects
        setupRecyclerView(updatedProjects)
    }

    private fun setupRecyclerView(projects: List<Project>) {
        try {
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProjects)
            recyclerView.layoutManager = LinearLayoutManager(this)
            adapter = ProjectAdapter(projects)
            recyclerView.adapter = adapter

            adapter.onProjectClickListener = { project ->
                val intent = Intent(this, ProjectDetailsActivity::class.java)
                intent.putExtra("project", project)
                startActivity(intent)
            }
        } catch (e: Exception) {
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

    override fun onResume() {
        super.onResume()
        if (selectedScheduleId != -1) {
            refreshProjects()
        }
    }
}
