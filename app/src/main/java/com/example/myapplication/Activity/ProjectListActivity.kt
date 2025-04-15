package com.example.myapplication.Activity
import android.annotation.SuppressLint
import android.content.Context
import com.example.myapplication.Adapter.ProjectAdapter
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
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
import androidx.appcompat.widget.PopupMenu
import com.example.myapplication.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProjectListActivity : AppCompatActivity() {
    private lateinit var projectsList: List<Project>
    private lateinit var filterButton: FloatingActionButton
    private lateinit var filterLayout: View
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var userName: TextView
    private lateinit var profileIcon: ImageView
    private lateinit var sharedPref: SharedPreferences

    @SuppressLint("MissingInflatedId")
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

        sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        toolbar = findViewById(R.id.toolbar)
        userName = findViewById(R.id.userName)
        profileIcon = findViewById(R.id.profileIcon)
        checkAuthStatus()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

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

