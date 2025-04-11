package com.example.myapplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Api.LoginRequest
import com.example.myapplication.Api.LoginResponse
import com.example.myapplication.Models.SecretaryIdResponse
import com.example.myapplication.Models.SecretaryResponse
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.CookiePolicy

/*class LoginActivity : AppCompatActivity() {
    *private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Retrofit

//        val cookieManager = CookieManager()
//        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
//
//        val client = OkHttpClient.Builder()
//            .cookieJar(JavaNetCookieJar(cookieManager))
//            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:9000/") // Замените на IP вашего сервера (10.0.2.2 для эмулятора)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)


        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val request = LoginRequest(username, password)


            apiService.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse?.status == "success") {
                            // Авторизация успешна
                            Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                            // Переход на главный экран
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Ошибка авторизации
                            Toast.makeText(this@LoginActivity, loginResponse?.message ?: "ANНеизвестная ошибка", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Ошибка HTTP
                        Toast.makeText(this@LoginActivity, "Ошибка HTTP: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", "HTTP Error: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    // Ошибка сети
                    Toast.makeText(this@LoginActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Network Error: ${t.message}")
                }
            })
        }
    }
}*/

/*class LoginActivity : AppCompatActivity() {
    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Создайте файл макета activity_login.xml

        editTextLogin = findViewById(R.id.editTextLogin)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            val login = editTextLogin.text.toString()
            val password = editTextPassword.text.toString()
            authenticateUser(login, password)
        }
    }

    private fun authenticateUser(login: String, password: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val requestBody = mapOf("login" to login, "password" to password)

        apiService.authenticateUser(requestBody).enqueue(object : Callback<SecretaryResponse> {
            override fun onResponse(call: Call<SecretaryResponse>, response: Response<SecretaryResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { secretary ->
                        val fio = secretary.full_name
                        // Передаем ФИО секретаря в MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            putExtra("secretaryName", fio)
                        }
                        startActivity(intent)
                        finish() // Закрываем LoginActivity
                    } ?: showToast("Ответ пустой")
                } else {
                    showToast("Ошибка авторизации: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SecretaryResponse>, t: Throwable) {
                showToast("Ошибка: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}*/


class LoginActivity : AppCompatActivity() {
    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextLogin = findViewById(R.id.editTextLogin)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Установка обработчика нажатия кнопки
        buttonLogin.setOnClickListener {
            val login = editTextLogin.text.toString()
            val password = editTextPassword.text.toString()
            authenticateUser(login, password)
        }
    }

    private fun authenticateUser(login: String, password: String) {
        val requestBody = mapOf("login" to login, "password" to password)

        apiService.authenticateUser(requestBody).enqueue(object : Callback<SecretaryResponse> {
            override fun onResponse(call: Call<SecretaryResponse>, response: Response<SecretaryResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { secretary ->
                        val fullName = secretary.full_name
                        val nameParts = fullName.split(" ")
                        if (nameParts.size == 3) {
                            val surname = nameParts[0]
                            val name = nameParts[1]
                            val patronymic = nameParts[2]

                            getSecretaryId(apiService, surname, name, patronymic)
                        } else {
                            showToast("Некорректный формат ФИО")
                        }
                    } ?: showToast("Ответ пустой")
                } else {
                    showToast("Ошибка авторизации: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SecretaryResponse>, t: Throwable) {
                showToast("Ошибка: ${t.message}")
            }
        })
    }

    private fun getSecretaryId(apiService: ApiService, surname: String, name: String, patronymic: String) {
        val requestBody = mapOf(
            "surname" to surname,
            "name" to name,
            "patronymic" to patronymic
        )

        apiService.getSecretaryId(requestBody).enqueue(object : Callback<SecretaryIdResponse> {
            override fun onResponse(call: Call<SecretaryIdResponse>, response: Response<SecretaryIdResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { secretaryIdResponse ->
                        val secretaryId = secretaryIdResponse.id

                        // Передаем ФИО и ID секретаря в MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            putExtra("secretaryId", secretaryId)
                        }
                        startActivity(intent)
                        finish() // Закрываем LoginActivity
                    } ?: showToast("Ответ пустой")
                } else {
                    showToast("Ошибка получения ID секретаря: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SecretaryIdResponse>, t: Throwable) {
                showToast("Ошибка: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
