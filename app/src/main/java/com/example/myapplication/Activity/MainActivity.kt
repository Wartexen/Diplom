package com.example.myapplication.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Db.Commission
import com.example.myapplication.Models.Db.DefenseSchedule
import com.example.myapplication.Models.Requests.CommissionScheduleRequest
import com.example.myapplication.Models.Db.Specialization
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

    private var dppValues: List<String> = emptyList()
    private var commissionValues: List<String> = emptyList()
    private var scheduleValues: List<String> = emptyList()

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

        spinnerCommission.isEnabled = false
        spinnerDefenseSchedule.isEnabled = false

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
        spinnerDpp.setOnClickListener {
            if (dppValues.isEmpty()) {
                showToast("Список направлений пуст")
                return@setOnClickListener
            }

            showSelectionDialog("Выберите направление", dppValues) { position ->
                spinnerDpp.setText(dppValues[position])
                val selectedSpecialization = specializationsList[position]
                selectedSpecializationID = selectedSpecialization.ID
                spinnerCommission.isEnabled = true
                spinnerCommission.setText("")
                spinnerDefenseSchedule.isEnabled = false
                spinnerDefenseSchedule.setText("")

                fetchCommissions(apiService, secretaryId, selectedSpecialization.ID)
            }
        }

        spinnerCommission.setOnClickListener {
            if (commissionValues.isEmpty()) {
                showToast("Сначала выберите направление")
                return@setOnClickListener
            }
            showSelectionDialog("Выберите комиссию", commissionValues) { position ->
                spinnerCommission.setText(commissionValues[position])
                spinnerDefenseSchedule.isEnabled = true
                spinnerDefenseSchedule.setText("")
                fetchDefenseSchedule(apiService, selectedSpecializationID)
            }
        }

        spinnerDefenseSchedule.setOnClickListener {
            if (scheduleValues.isEmpty()) {
                showToast("Сначала выберите комиссию")
                return@setOnClickListener
            }

            showSelectionDialog("Выберите дату защиты", scheduleValues) { position ->
                spinnerDefenseSchedule.setText(scheduleValues[position])
            }
        }

        buttonNext.setOnClickListener {
            try {
                val selectedDpp = spinnerDpp.text.toString()
                val selectedCommissionName = spinnerCommission.text.toString()
                val selectedScheduleName = spinnerDefenseSchedule.text.toString()

                if (selectedDpp.isEmpty() || selectedCommissionName.isEmpty() || selectedScheduleName.isEmpty()) {
                    showToast("Пожалуйста, выберите все необходимые параметры")
                    return@setOnClickListener
                }

                val commissionId = commissionsList.find { it.Name == selectedCommissionName }?.ID
                selectedScheduleId = defenseSchedulesList.find { formatDate(it.DateTime) == selectedScheduleName }?.ID

                if (commissionId != null && selectedScheduleId != null) {
                    sendCommissionId(apiService, commissionId, selectedScheduleId!!)

                    val intent = Intent(this, com.example.myapplication.ProjectListActivity::class.java).apply {
                        putExtra("selectedDpp", selectedDpp)
                        putExtra("selectedCommission", selectedCommissionName)
                        putExtra("selectedDate", selectedScheduleName)
                        putExtra("selectedScheduleId", selectedScheduleId)
                    }
                    startActivity(intent)
                } else {
                    showToast("Ошибка: не удалось определить ID комиссии или расписания")
                }
            } catch (e: Exception) {
                showToast("Ошибка: ${e.message}")
            }
        }
    }

    private fun showSelectionDialog(title: String, items: List<String>, onItemSelected: (Int) -> Unit) {
        if (items.isEmpty()) {
            showToast("Нет доступных элементов для выбора")
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        val adapter = object : ArrayAdapter<String>(this, R.layout.dialog_list_item, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.dialog_list_item, parent, false)

                val textView = view.findViewById<TextView>(R.id.text)
                textView.text = getItem(position)

                return view
            }
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_selection, null)
        val listView = dialogView.findViewById<ListView>(R.id.listView)
        listView.adapter = adapter
        builder.setView(dialogView)
        builder.setNegativeButton("Отмена", null)
        val dialog = builder.create()

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            onItemSelected(position)
            dialog.dismiss()
        }

        dialog.show()
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

    private fun fetchSpecializations(apiService: ApiService, secretaryId: Int) {
        apiService.getSecretarySpecializations(secretaryId).enqueue(createCallback { responses ->
            this.specializationsList = responses.mapNotNull { it.ID_Specialization }
            val specializationNames = this.specializationsList.map { it.Name ?: "" }
            updateSpinnerDpp(specializationNames)
        })
    }

    private fun fetchCommissions(apiService: ApiService, secretaryId: Int, specializationId: Int) {
        apiService.getCommissionsBySecretary(secretaryId, "Секретарь").enqueue(createCallback { responses ->
            this.commissionsList = responses.mapNotNull { it.ID_Commission }
            val commissionNames = this.commissionsList.map { it.Name ?: "" }
            updateSpinnerCommission(commissionNames)
        })
    }

    private fun fetchDefenseSchedule(apiService: ApiService, specialization_id: Int) {
        apiService.getDefensesBySpecialization(specialization_id).enqueue(createCallback { responses ->
            this.defenseSchedulesList = responses
            val dateTimeValues = responses.map { formatDate(it.DateTime) }
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
        this.dppValues = dppValues
        if (dppValues.isEmpty()) {
            spinnerDpp.setText("")
        }
    }

    private fun updateSpinnerCommission(commissionValues: List<String>) {
        this.commissionValues = commissionValues
        spinnerCommission.setText("")
    }

    private fun updateSpinnerDefenseSchedule(scheduleValues: List<String>) {
        this.scheduleValues = scheduleValues
        spinnerDefenseSchedule.setText("")
    }

    private fun sendCommissionId(apiService: ApiService, commissionId: Int, scheduleId: Int) {
        val request = CommissionScheduleRequest(commissionId)
        apiService.updateDefenseSchedule(scheduleId, request).enqueue(object : Callback<DefenseSchedule> {
            override fun onResponse(call: Call<DefenseSchedule>, response: Response<DefenseSchedule>) {
                if (response.isSuccessful) {
                    val updatedSchedule = response.body()
                } else {
                    showError(response.code())
                }
            }
            override fun onFailure(call: Call<DefenseSchedule>, t: Throwable) {
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