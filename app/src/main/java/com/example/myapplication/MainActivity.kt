package com.example.myapplication
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Commission
import com.example.myapplication.Models.CommissionScheduleRequest
import com.example.myapplication.Models.DefenseSchedule
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonNext = findViewById(R.id.buttonNext)
        spinnerDpp = findViewById(R.id.spinnerDpp)
        spinnerCommission = findViewById(R.id.spinnerCommission)
        spinnerDefenseSchedule = findViewById(R.id.spinnerDate)

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // Получение данных с сервера
        fetchSpecializations(apiService)
        fetchCommissions(apiService)
        fetchDefenseSchedule(apiService)

        // Установка обработчика нажатия кнопки
        buttonNext.setOnClickListener {
            val selectedCommissionName = spinnerCommission.selectedItem?.toString()
            val selectedCommission = commissionsList.find { it.Name == selectedCommissionName }

            val selectedScheduleName = spinnerDefenseSchedule.selectedItem?.toString()
            val selectedSchedule = defenseSchedulesList.find { formatDate(it.DateTime) == selectedScheduleName }

            if (selectedCommission != null && selectedSchedule != null) {
                // Получаем ID комиссии и расписания
                val commissionId = selectedCommission.ID // Предполагается, что у вас есть поле ID в классе Commission
                val scheduleId = selectedSchedule.ID // Предполагается, что у вас есть поле ID в классе DefenseSchedule

                // Отправка ID комиссии и расписания
                sendCommissionId(apiService, commissionId, scheduleId)

                // Создание Intent для перехода на ProjectListActivity
                val intent = Intent(this, ProjectListActivity::class.java).apply {
                    putExtra("selectedDpp", spinnerDpp.selectedItem?.toString())
                    putExtra("selectedCommission", selectedCommissionName)
                    putExtra("selectedDate", selectedScheduleName)
                }
                startActivity(intent) // Запуск новой активности
            } else {
                showToast("Пожалуйста, выберите аттестационную комиссию и расписание")
            }
        }
    }

    private fun fetchSpecializations(apiService: ApiService) {
        apiService.getSpecializations().enqueue(createCallback { specializations ->
            updateSpinnerDpp(specializations.map { it.Name })
        })
    }

    private fun fetchCommissions(apiService: ApiService) {
        apiService.getCommissions().enqueue(createCallback { commissions ->
            this.commissionsList = commissions
            updateSpinnerCommission(commissions.map { it.Name })
        })
    }

    private fun fetchDefenseSchedule(apiService: ApiService) {
        apiService.getDefenseSchedules().enqueue(createCallback { schedules ->
            this.defenseSchedulesList = schedules
            val dateTimeValues = schedules.map { "${formatDate(it.DateTime)}" }
            updateSpinnerDefenseSchedule(dateTimeValues)
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


