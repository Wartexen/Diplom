package com.example.myapplication
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Commission
import com.example.myapplication.Models.CommissionScheduleRequest
import com.example.myapplication.Models.DefenseSchedule
import com.example.myapplication.Models.Specialization
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity() {
    private lateinit var spinnerDpp: Spinner
    private lateinit var spinnerCommission: Spinner
    private lateinit var spinnerDefenseSchedule: Spinner
    private lateinit var buttonNext: Button
    private lateinit var commissionsList: List<Commission>
    private lateinit var defenseSchedulesList: List<DefenseSchedule>
    private lateinit var specializationsList: List<Specialization> // Список направлений
    private var selectedScheduleId: Int? = null // Переменная для хранения ID выбранного времени защиты

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonNext = findViewById(R.id.buttonNext)
        spinnerDpp = findViewById(R.id.spinnerDpp)
        spinnerCommission = findViewById(R.id.spinnerCommission)
        spinnerDefenseSchedule = findViewById(R.id.spinnerDate)

        // Инициализация спиннеров
        setupSpinners()

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // Получаем ID секретаря из интента
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

        // Установка обработчика нажатия кнопки
        buttonNext.setOnClickListener {
            val selectedCommissionName = spinnerCommission.selectedItem?.toString()
            val selectedScheduleName = spinnerDefenseSchedule.selectedItem?.toString()

            if (selectedCommissionName != null && selectedScheduleName != null) {
                // Получаем ID комиссии и расписания
                val commissionId = commissionsList.find { it.Name == selectedCommissionName }?.ID
                selectedScheduleId = defenseSchedulesList.find { formatDate(it.DateTime) == selectedScheduleName }?.ID // Сохраняем ID расписания

                if (commissionId != null && selectedScheduleId != null) {
                    // Отправка ID комиссии и расписания
                    sendCommissionId(apiService, commissionId, selectedScheduleId!!)

                    // Создание Intent для перехода на ProjectListActivity
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
        // Установка начальных значений
        updateSpinnerDpp(listOf("Выберите направление"))
        updateSpinnerCommission(listOf("Выберите комиссию"))
        updateSpinnerDefenseSchedule(listOf("Выберите дату защиты"))

        // Делаем спиннеры недоступными для выбора по умолчанию
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
            spinnerCommission.isEnabled = true // Активируем второй спиннер
        })
    }

    private fun fetchDefenseSchedule(apiService: ApiService, specialization_id: Int) {
        val date = "2024-12-21" // Замените на нужную вам дату
        apiService.getTodayDefensesBySpecialization(specialization_id, date).enqueue(createCallback { schedules ->
            this.defenseSchedulesList = schedules
            val dateTimeValues = listOf("Выберите дату защиты") + schedules.map { formatDate(it.DateTime) }
            updateSpinnerDefenseSchedule(dateTimeValues)
            spinnerDefenseSchedule.isEnabled = true // Активируем третий спиннер
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
