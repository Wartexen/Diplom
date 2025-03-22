package com.example.myapplication
import ProjectAdapter
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProjectListActivity : AppCompatActivity() {
    private lateinit var projectsList: List<Project>
    private lateinit var filterButton: FloatingActionButton
    private lateinit var filterLayout: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_list)

        val selectedDpp = intent.getStringExtra("selectedDpp")
        val selectedScheduleId = intent.getIntExtra("selectedScheduleId", -1)
        val selectedCommission = intent.getStringExtra("selectedCommission")
        val selectedDate = intent.getStringExtra("selectedDate")
        val selectedDppTextView = findViewById<TextView>(R.id.selectedDpp)
        val selectedCommissionTextView = findViewById<TextView>(R.id.selectedCommission)
        val selectedDateTextView = findViewById<TextView>(R.id.selectedDate)

        selectedDppTextView.text = "$selectedDpp"
        selectedCommissionTextView.text = "$selectedCommission"
        selectedDateTextView.text = "$selectedDate"

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        // Получение проектов
        getProjects(apiService, selectedScheduleId)

        filterButton = findViewById(R.id.filterButton)
        filterLayout = findViewById(R.id.filterLayout)

        filterButton.setOnClickListener {
            toggleFilterVisibility()
        }
        filterLayout.setOnClickListener{
            toggleFilterVisibility()
        }

        val buttonNext = findViewById<Button>(R.id.buttonNext)
//        buttonNext.setOnClickListener {
//            val intent = Intent(this, StudentGradingActivity::class.java)
//            intent.putExtra("selectedDpp", selectedDppTextView.text.toString())
//            intent.putExtra("selectedCommission", selectedCommissionTextView.text.toString())
//            intent.putExtra("selectedDate", selectedDateTextView.text.toString())
//            startActivity(intent)
//        }

    }

    private fun getProjects(apiService: ApiService, defenseScheduleId: Int) {
        apiService.getProjectsBydefense_schedule_id(defenseScheduleId).enqueue(object : Callback<List<Project>> {
            override fun onResponse(call: Call<List<Project>>, response: Response<List<Project>>) {
                if (response.isSuccessful) {
                   response.body()?.let { projects ->
                        projectsList = projects // Сохраняем проекты в переменной класса
                        setupRecyclerView(projects) // Настройка RecyclerView после получения данных
                    } ?: showToast("Ответ пустой")
                } else {
                    showToast("Ошибка: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<Project>>, t: Throwable) {
                showToast("Ошибка: ${t.message}")
            }
        })
    }

    private fun setupRecyclerView(projects: List<Project>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProjects)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ProjectAdapter(projects)
        recyclerView.adapter = adapter

        adapter.onProjectClickListener = { project ->
            val intent = Intent(this, ProjectDetailsActivity::class.java)
            intent.putExtra("project", project)
            startActivity(intent)
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

