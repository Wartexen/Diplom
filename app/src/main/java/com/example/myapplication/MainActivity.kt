package com.example.myapplication
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Commission
import com.example.myapplication.Models.Specialization
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Suppress("UNREACHABLE_CODE")
/*class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonNext = findViewById<Button>(R.id.buttonNext)

        val spinnerDpp = findViewById<Spinner>(R.id.spinnerDpp)
        val spinnerCommission = findViewById<Spinner>(R.id.spinnerCommission)
        val spinnerDate = findViewById<Spinner>(R.id.spinnerDate)

        val dppValues = listOf("ДПП 1", "ДПП 2")
        val commissionValues = listOf("Комиссия A", "Комиссия B")
        val dateValues = listOf("Дата 1", "Дата 2")

        val adapterDpp = ArrayAdapter(this, android.R.layout.simple_spinner_item, dppValues)
        val adapterCommission = ArrayAdapter(this, android.R.layout.simple_spinner_item, commissionValues)
        val adapterDate = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateValues)

        adapterDpp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterCommission.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerDpp.adapter = adapterDpp
        spinnerCommission.adapter = adapterCommission
        spinnerDate.adapter = adapterDate


        buttonNext.setOnClickListener {
            val selectedDpp = spinnerDpp.selectedItem.toString()
            val selectedCommission = spinnerCommission.selectedItem.toString()
            val selectedDate = spinnerDate.selectedItem.toString()
            val selectedInstitute = spinnerDpp.selectedItem.toString() // Получение выбранного института

            if (selectedDpp.isBlank() || selectedCommission.isBlank() || selectedDate.isBlank() || selectedInstitute.isBlank()) {
                Toast.makeText(this, "Пожалуйста, выберите все значения.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, ProjectListActivity::class.java)
            intent.putExtra("selectedDpp", selectedDpp)
            intent.putExtra("selectedCommission", selectedCommission)
            intent.putExtra("selectedDate", selectedDate)
            intent.putExtra("selectedInstitute", selectedInstitute) // Передача значения института
            startActivity(intent)
            Toast.makeText(this, "Выбран институт: $selectedInstitute", Toast.LENGTH_LONG).show()
        }
    }

    private fun getSelectedSpinnerValue(spinnerId: Int): String? {
        val spinner = findViewById<Spinner>(spinnerId)
        val selectedItemPosition = spinner.selectedItemPosition
        return if (selectedItemPosition != -1) {
            (spinner.adapter.getItem(selectedItemPosition) as? String) ?: ""
        } else {
            null
        }
    }
}*/


class MainActivity : AppCompatActivity() {
    private lateinit var spinnerDpp: Spinner
    private lateinit var spinnerCommission: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerDpp = findViewById(R.id.spinnerDpp)
        spinnerCommission = findViewById(R.id.spinnerCommission)

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Замените на ваш URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // Получение данных с сервера
        fetchSpecializations(apiService)
    }

    private fun fetchSpecializations(apiService: ApiService) {
        apiService.getSpecializations().enqueue(object : Callback<List<Specialization>> {
            override fun onResponse(call: Call<List<Specialization>>, response: Response<List<Specialization>>) {
                if (response.isSuccessful) {
                    response.body()?.let { specializations ->
                        updateSpinnerDpp(specializations.map { it.Name })
                        fetchCommissions(apiService)
                    } ?: showToast("Ответ по специализациям пустой")
                } else {
                    showError(response.code())
                }
            }

            override fun onFailure(call: Call<List<Specialization>>, t: Throwable) {
                showToast("Ошибка: ${t.message}")
            }
        })
    }

    private fun fetchCommissions(apiService: ApiService) {
        apiService.getCommissions().enqueue(object : Callback<List<Commission>> {
            override fun onResponse(call: Call<List<Commission>>, response: Response<List<Commission>>) {
                if (response.isSuccessful) {
                    response.body()?.let { commissions ->
                        updateSpinnerCommission(commissions.map { it.Name })
                    } ?: showToast("Ответ по комиссиям пустой")
                } else {
                    showError(response.code())
                }
            }

            override fun onFailure(call: Call<List<Commission>>, t: Throwable) {
                showToast("Ошибка: ${t.message}")
            }
        })
    }

    private fun updateSpinnerDpp(dppValues: List<String>) {
        ArrayAdapter(this, android.R.layout.simple_spinner_item, dppValues).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDpp.adapter = this
        }
    }

    private fun updateSpinnerCommission(commissionValues: List<String>) {
        ArrayAdapter(this, android.R.layout.simple_spinner_item, commissionValues).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCommission.adapter = this
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(code: Int) {
        showToast("Ошибка загрузки данных: $code")
    }
}
