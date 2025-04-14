package com.example.myapplication.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Commission
import com.example.myapplication.Models.CommissionScheduleRequest
import com.example.myapplication.Models.DefenseSchedule
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
/*
@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity() {
    private lateinit var spinnerDpp: Spinner
    private lateinit var spinnerCommission: Spinner
    private lateinit var spinnerDefenseSchedule: Spinner
    private lateinit var buttonNext: Button
    private lateinit var commissionsList: List<Commission>
    private lateinit var defenseSchedulesList: List<DefenseSchedule>
    private lateinit var specializationsList: List<Specialization>
    private var selectedScheduleId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonNext = findViewById(R.id.buttonNext)
        spinnerDpp = findViewById(R.id.spinnerDpp)
        spinnerCommission = findViewById(R.id.spinnerCommission)
        spinnerDefenseSchedule = findViewById(R.id.spinnerDate)

        setupSpinners()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)


        val secretaryId = intent.getIntExtra("secretaryId", -1)
        if (secretaryId != -1) {
            fetchSpecializations(apiService, secretaryId) // Запрос направлений
        }
        var selectedSpecializationID = 0

        // Установка обработчика нажатия на первый спиннер
        spinnerDpp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) { // Проверяем, что выбран не "Выберите направление"
                    val selectedSpecialization = specializationsList[position - 1] // -1 из-за начального элемента
                    selectedSpecializationID = selectedSpecialization.ID
                    fetchCommissions(apiService, secretaryId, selectedSpecialization.ID) // Запрос комиссий
                } else {
                    updateSpinnerCommission(emptyList()) // Очищаем второй спиннер
                    updateSpinnerDefenseSchedule(emptyList()) // Очищаем третий спиннер
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Установка обработчика нажатия на второй спиннер
        spinnerCommission.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) { // Проверяем, что выбран не "Выберите комиссию"
                    val selectedCommission = commissionsList[position - 1] // -1 из-за начального элемента
                    fetchDefenseSchedule(apiService, selectedSpecializationID) // Запрос расписания
                } else {
                    updateSpinnerDefenseSchedule(emptyList()) // Очищаем третий спиннер
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        buttonNext.setOnClickListener {
            val selectedCommissionName = spinnerCommission.selectedItem?.toString()
            val selectedScheduleName = spinnerDefenseSchedule.selectedItem?.toString()

            if (selectedCommissionName != null && selectedScheduleName != null) {

                val commissionId = commissionsList.find { it.Name == selectedCommissionName }?.ID
                selectedScheduleId = defenseSchedulesList.find { formatDate(it.DateTime) == selectedScheduleName }?.ID // Сохраняем ID расписания

                if (commissionId != null && selectedScheduleId != null) {

                    sendCommissionId(apiService, commissionId, selectedScheduleId!!)


                    val intent = Intent(this, ProjectListActivity::class.java).apply {
                        putExtra("selectedDpp", spinnerDpp.selectedItem?.toString())
                        putExtra("selectedCommission", selectedCommissionName)
                        putExtra("selectedDate", selectedScheduleName)
                        putExtra("selectedScheduleId", selectedScheduleId) // Передаем ID расписания
                    }
                    startActivity(intent) // Запуск новой активности
                } else {
                    showToast("Пожалуйста, выберите аттестационную комиссию и расписание")
                }
            }
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
        apiService.getSpecializationsBySecretary(secretaryId).enqueue(createCallback { specializations ->
            this.specializationsList = specializations
            val specializationNames = listOf("Выберите направление") + specializations.map { it.Name }
            updateSpinnerDpp(specializationNames)
        })
    }

    private fun fetchCommissions(apiService: ApiService, secretaryId: Int, specializationId: Int) {
        apiService.getCommissionsBySecretary(secretaryId).enqueue(createCallback { commissions ->
            this.commissionsList = commissions
            val commissionNames = listOf("Выберите комиссию") + commissions.map { it.Name }
            updateSpinnerCommission(commissionNames)
            spinnerCommission.isEnabled = true
        })
    }

    private fun fetchDefenseSchedule(apiService: ApiService, specialization_id: Int) {
        val date = "2024-12-21" // Заменить на нужную  дату
        apiService.getTodayDefensesBySpecialization(specialization_id, date).enqueue(createCallback { schedules ->
            this.defenseSchedulesList = schedules
            val dateTimeValues = listOf("Выберите дату защиты") + schedules.map { formatDate(it.DateTime) }
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

    private fun updateSpinner(spinner: Spinner, values: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun sendCommissionId(apiService: ApiService, commissionId: Int, scheduleId: Int) {
        val request = CommissionScheduleRequest(commissionId, scheduleId)
        apiService.addCommissionToSchedule(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Успешно отправлено
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
*/
@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity() {
    private lateinit var spinnerDpp: Spinner
    private lateinit var spinnerCommission: Spinner
    private lateinit var spinnerDefenseSchedule: Spinner
    private lateinit var buttonNext: Button
    private lateinit var commissionsList: List<Commission>
    private lateinit var defenseSchedulesList: List<DefenseSchedule>
    private lateinit var specializationsList: List<Specialization>
    private lateinit var sharedPref: SharedPreferences
    private var selectedScheduleId: Int? = null
    private var secretaryId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация SharedPreferences
        sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Проверка авторизации
        checkAuthStatus()

        buttonNext = findViewById(R.id.buttonNext)
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
            fetchSpecializations(apiService, secretaryId) // Запрос направлений
        }
        var selectedSpecializationID = 0

        // Установка обработчика нажатия на первый спиннер
        spinnerDpp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) { // Проверяем, что выбран не "Выберите направление"
                    val selectedSpecialization = specializationsList[position - 1] // -1 из-за начального элемента
                    selectedSpecializationID = selectedSpecialization.ID
                    fetchCommissions(apiService, secretaryId, selectedSpecialization.ID) // Запрос комиссий
                } else {
                    updateSpinnerCommission(emptyList()) // Очищаем второй спиннер
                    updateSpinnerDefenseSchedule(emptyList()) // Очищаем третий спиннер
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Установка обработчика нажатия на второй спиннер
        spinnerCommission.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) { // Проверяем, что выбран не "Выберите комиссию"
                    val selectedCommission = commissionsList[position - 1] // -1 из-за начального элемента
                    fetchDefenseSchedule(apiService, selectedSpecializationID) // Запрос расписания
                } else {
                    updateSpinnerDefenseSchedule(emptyList()) // Очищаем третий спиннер
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        buttonNext.setOnClickListener {
            val selectedCommissionName = spinnerCommission.selectedItem?.toString()
            val selectedScheduleName = spinnerDefenseSchedule.selectedItem?.toString()

            if (selectedCommissionName != null && selectedScheduleName != null) {
                val commissionId = commissionsList.find { it.Name == selectedCommissionName }?.ID
                selectedScheduleId = defenseSchedulesList.find { formatDate(it.DateTime) == selectedScheduleName }?.ID

                if (commissionId != null && selectedScheduleId != null) {
                    sendCommissionId(apiService, commissionId, selectedScheduleId!!)

                    val intent = Intent(this, ProjectListActivity::class.java).apply {
                        putExtra("selectedDpp", spinnerDpp.selectedItem?.toString())
                        putExtra("selectedCommission", selectedCommissionName)
                        putExtra("selectedDate", selectedScheduleName)
                        putExtra("selectedScheduleId", selectedScheduleId)
                    }
                    startActivity(intent)
                } else {
                    showToast("Пожалуйста, выберите аттестационную комиссию и расписание")
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
            // Устанавливаем имя пользователя в Toolbar
            //val fullName = "S"
            val fullName = sharedPref.getString("fullName", "")
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.title = fullName
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
        apiService.getSpecializationsBySecretary(secretaryId).enqueue(createCallback { specializations ->
            this.specializationsList = specializations
            val specializationNames = listOf("Выберите направление") + specializations.map { it.Name }
            updateSpinnerDpp(specializationNames)
        })
    }

    private fun fetchCommissions(apiService: ApiService, secretaryId: Int, specializationId: Int) {
        apiService.getCommissionsBySecretary(secretaryId).enqueue(createCallback { commissions ->
            this.commissionsList = commissions
            val commissionNames = listOf("Выберите комиссию") + commissions.map { it.Name }
            updateSpinnerCommission(commissionNames)
            spinnerCommission.isEnabled = true
        })
    }

    private fun fetchDefenseSchedule(apiService: ApiService, specialization_id: Int) {
        val date = "2024-12-21" // Заменить на нужную дату
        apiService.getTodayDefensesBySpecialization(specialization_id, date).enqueue(createCallback { schedules ->
            this.defenseSchedulesList = schedules
            val dateTimeValues = listOf("Выберите дату защиты") + schedules.map { formatDate(it.DateTime) }
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

    private fun updateSpinner(spinner: Spinner, values: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun sendCommissionId(apiService: ApiService, commissionId: Int, scheduleId: Int) {
        val request = CommissionScheduleRequest(commissionId, scheduleId)
        apiService.addCommissionToSchedule(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Успешно отправлено
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