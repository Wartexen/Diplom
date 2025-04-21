package com.example.myapplication.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем статус авторизации
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        //sharedPref.edit().clear().apply()
        // Определяем, куда переходить
        val intent = if (isLoggedIn) {
            Intent(this, MainActivity::class.java).apply {
                putExtra("secretaryId", sharedPref.getInt("secretaryId", -1))
                putExtra("fullName", sharedPref.getString("fullName", ""))
            }
        } else {
            Intent(this, BitrixAuthActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}