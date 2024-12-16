package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity() {
//
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
            val selectedDpp =  spinnerDpp.selectedItem.toString()
            val selectedCommission = spinnerCommission.selectedItem.toString()
            val selectedDate = spinnerDate.selectedItem.toString()

            if (selectedDpp.isNullOrBlank() || selectedCommission.isNullOrBlank() || selectedDate.isNullOrBlank()) {
                return@setOnClickListener
            }

            val intent = Intent(this, ProjectListActivity::class.java)
            intent.putExtra("selectedDpp", selectedDpp)
            intent.putExtra("selectedCommission", selectedCommission)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
            Toast.makeText(this, "text", Toast.LENGTH_LONG).show()
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


//    fun getProjects(): {
//    }
}