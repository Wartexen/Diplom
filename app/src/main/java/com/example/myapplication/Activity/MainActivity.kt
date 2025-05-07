package com.example.myapplication.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Commission
import com.example.myapplication.Models.DefenseSchedule
import com.example.myapplication.Models.Requests.CommissionScheduleRequest
import com.example.myapplication.Models.Specialization
import com.example.myapplication.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity() {
    private lateinit var spinnerDpp: AutoCompleteTextView
    private lateinit var spinnerCommission: AutoCompleteTextView
    private lateinit var spinnerDefenseSchedule: AutoCompleteTextView
    private lateinit var buttonNext: Button
    private lateinit var commissionsList: List<Commission>
    private lateinit var defenseSchedulesList: List<DefenseSchedule>
    private lateinit var specializationsList: List<Specialization>
    private lateinit var sharedPref: SharedPreferences
    private var selectedScheduleId: Int? = null
    private var secretaryId: Int = -1
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var userName: TextView
    private lateinit var profileIcon: ImageView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        toolbar = findViewById(R.id.toolbar)
        userName = findViewById(R.id.userName)
        profileIcon = findViewById(R.id.profileIcon)
        checkAuthStatus()
        buttonNext = findViewById(R.id.buttonNextt)
        spinnerDpp = findViewById(R.id.spinnerDpp)
        spinnerCommission = findViewById(R.id.spinnerCommission)
        spinnerDefenseSchedule = findViewById(R.id.spinnerDate)
        setupSpinners()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        secretaryId = intent.getIntExtra("secretaryId", -1)
        if (secretaryId == -1) {
            secretaryId = sharedPref.getInt("secretaryId", -1)
        }

        if (secretaryId != -1) {
            fetchSpecializations(apiService, secretaryId)
        }
        var selectedSpecializationID = 0
        spinnerDpp.setOnItemClickListener { parent, _, position, _ ->
            if (position > 0) {
                val selectedSpecialization = specializationsList[position - 1]
                selectedSpecializationID = selectedSpecialization.ID
                fetchCommissions(apiService, secretaryId, selectedSpecialization.ID)
            } else {
                updateSpinnerCommission(emptyList())
                updateSpinnerDefenseSchedule(emptyList())
            }
        }

        spinnerCommission.setOnItemClickListener { parent, _, position, _ ->
            if (position > 0) {
                val selectedCommission = commissionsList[position - 1]
                fetchDefenseSchedule(apiService, selectedSpecializationID)
            } else {
                updateSpinnerDefenseSchedule(emptyList())
            }
        }

        buttonNext.setOnClickListener {
            try {
                val selectedDpp = spinnerDpp.text.toString()
                val selectedCommissionName = spinnerCommission.text.toString()
                val selectedScheduleName = spinnerDefenseSchedule.text.toString()
                if (selectedCommissionName != null && selectedScheduleName != null &&
                    selectedCommissionName != "Выберите комиссию" &&
                    selectedScheduleName != "Выберите дату защиты") {

                    val commissionId = commissionsList.find { it.Name == selectedCommissionName }?.ID
                    selectedScheduleId = defenseSchedulesList.find { formatDate(it.DateTime) == selectedScheduleName }?.ID
                    if (commissionId != null && selectedScheduleId != null) {
                        sendCommissionId(apiService, commissionId, selectedScheduleId!!)

                        // Создаем Intent с полным путем к классу
                        val intent = Intent(this, com.example.myapplication.ProjectListActivity::class.java).apply {
                            putExtra("selectedDpp", selectedDpp)
                            putExtra("selectedCommission", selectedCommissionName)
                            putExtra("selectedDate", selectedScheduleName)
                            putExtra("selectedScheduleId", selectedScheduleId)
                        }
                        startActivity(intent)
                    } else {
                        showToast("Пожалуйста, выберите аттестационную комиссию и расписание")
                    }
                } else {
                    showToast("Пожалуйста, выберите все необходимые параметры")
                }
            } catch (e: Exception) {
                showToast("Ошибка: ${e.message}")
            }
        }
    }

    private fun checkAuthStatus() {
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            startActivity(Intent(this, BitrixAuthActivity::class.java))
            finish()
        } else {
            val fullName = sharedPref.getString("fullName", "") ?: ""
            userName.text = formatUserName(fullName)
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

    private fun setupSpinners() {
        updateSpinnerDpp(listOf("Выберите направление"))
        updateSpinnerCommission(listOf("Выберите комиссию"))
        updateSpinnerDefenseSchedule(listOf("Выберите дату защиты"))
        spinnerCommission.isEnabled = false
        spinnerDefenseSchedule.isEnabled = false
    }


    private fun fetchSpecializations(apiService: ApiService, secretaryId: Int) {
        apiService.getSecretarySpecializations(secretaryId).enqueue(createCallback { responses ->
            this.specializationsList = responses.mapNotNull { it.ID_Specialization }
            val specializationNames = listOf("Выберите направление") + this.specializationsList.map { it.Name ?: "" }
            updateSpinnerDpp(specializationNames)
        })
    }

   private fun fetchCommissions(apiService: ApiService, secretaryId: Int, specializationId: Int) {
       apiService.getCommissionsBySecretary(secretaryId, "Секретарь").enqueue(createCallback { responses ->
           this.commissionsList =  responses.mapNotNull { it.ID_Commission }
           val commissionNames = listOf("Выберите комиссию") + this.commissionsList.map { it.Name ?: "" }
           updateSpinnerCommission(commissionNames)
           spinnerCommission.isEnabled = true
       })
   }
    private fun fetchDefenseSchedule(apiService: ApiService, specialization_id: Int) {
        apiService.getDefensesBySpecialization(specialization_id).enqueue(createCallback { responses ->
            this.defenseSchedulesList = responses
            val dateTimeValues = listOf("Выберите дату защиты") + responses.map { formatDate(it.DateTime) }
            updateSpinnerDefenseSchedule(dateTimeValues)
            spinnerDefenseSchedule.isEnabled = true
        })
    }

    private fun <T> createCallback(onSuccess: (List<T>) -> Unit): Callback<List<T>> {
        return object : Callback<List<T>> {
            override fun onResponse(call: Call<List<T>>, response: Response<List<T>>) {
                if (response.isSuccessful) {
                    response.body()?.let(onSuccess) ?: showToast("Ответ пустой")
                } else {
                    showError(response.code())
                }
            }
            override fun onFailure(call: Call<List<T>>, t: Throwable) {
                showToast("Ошибка: ${t.message}")
            }
        }
    }

    private fun formatDate(dateTime: String): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateTime)
        return formatter.format(date!!)
    }

    private fun updateSpinnerDpp(dppValues: List<String>) {
        updateSpinner(spinnerDpp, dppValues)
    }

    private fun updateSpinnerCommission(commissionValues: List<String>) {
        updateSpinner(spinnerCommission, commissionValues)
    }

    private fun updateSpinnerDefenseSchedule(scheduleValues: List<String>) {
        updateSpinner(spinnerDefenseSchedule, scheduleValues)
    }

    private fun updateSpinner(spinner: AutoCompleteTextView, values: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, values)
        spinner.setAdapter(adapter)
        if (values.isNotEmpty()) {
            spinner.setText(values[0], false)
        }
    }

    private fun sendCommissionId(apiService: ApiService, commissionId: Int, scheduleId: Int) {
        val request = CommissionScheduleRequest(commissionId, scheduleId)
        apiService.addCommissionToSchedule(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                } else {
                    showError(response.code())
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Ошибка: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(code: Int) {
        Toast.makeText(this, "Ошибка: $code", Toast.LENGTH_SHORT).show()
    }
}
