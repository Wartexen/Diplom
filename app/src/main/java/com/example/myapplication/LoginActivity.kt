package com.example.myapplication
import android.content.Context
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.BitrixAuthResponse
import com.example.myapplication.Models.SecretaryIdResponse

// Замените на реальный package name вашего приложения

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
/*

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
}*/

class LoginActivity : AppCompatActivity() {
    private val AUTH_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        fun startBitrixAuth() {

        }
        val buttonBitrixLogin = findViewById<Button>(R.id.buttonBitrixLogin)
        buttonBitrixLogin.setOnClickListener {
            val intent = BitrixAuthActivity.createStartIntent(this)
            startActivityForResult(intent, AUTH_REQUEST_CODE)
           // startActivity(Intent(this, BitrixAuthActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    // Авторизация успешна
                    val prefs = getSharedPreferences("BitrixAuth", Context.MODE_PRIVATE)
                    val email = prefs.getString("email", null)
                    Toast.makeText(this, "Welcome, $email!", Toast.LENGTH_SHORT).show()
                }
                RESULT_CANCELED -> {
                    // Ошибка авторизации
                    val error = data?.getStringExtra("error") ?: "Unknown error"
                    Toast.makeText(this, "Auth failed: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}/*
class LoginActivity : AppCompatActivity() {
   private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var apiService: ApiService
    private lateinit var webView: WebView
    private lateinit var authDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextLogin = findViewById(R.id.editTextLogin)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonBitrixLogin)



        // Инициализация Retrofit для вашего сервера
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Для эмулятора Android
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Настройка WebView для OAuth авторизации
        setupWebView()

        buttonLogin.setOnClickListener {
            startBitrixOAuthFlow()
        }
    }

    private fun setupWebView() {
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true // Добавьте эту строку
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    val url = request.url.toString()
                    if (url.contains("code=")) {
                        val code = url.split("code=")[1].split("&")[0]
                        exchangeCodeForToken(code)
                        authDialog.dismiss()
                        return true
                    }
                    return false
                }
            }
        }
    }
    private fun startBitrixOAuthFlow() {
        val authUrl = "https://int.istu.edu/oauth/authorize/" +
                "?client_id=${BuildConfig.BITRIX_CLIENT_ID}" +
                "&response_type=code" +
                "&redirect_uri=http://10.0.2.2:8000/auth/bitrix/"

        webView.loadUrl(authUrl)
        authDialog.show()
    }

    private fun exchangeCodeForToken(code: String) {
        // Отправляем код авторизации на ваш сервер
        val request = BitrixAuthRequest(code = code)

        apiService.authenticateWithBitrix(request).enqueue(object : Callback<BitrixAuthResponse> {
            override fun onResponse(
                call: Call<BitrixAuthResponse>,
                response: Response<BitrixAuthResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Получаем ФИО пользователя
                        processUserData(authResponse.user)
                    } ?: showError("Пустой ответ от сервера")
                } else {
                    showError("Ошибка авторизации: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BitrixAuthResponse>, t: Throwable) {
                showError("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun processUserData(user: com.example.myapplication.Models.BitrixUser) {
        // Разбиваем ФИО на составляющие
        val nameParts = user.full_name.split(" ")
        if (nameParts.size < 3) {
            showError("Некорректный формат ФИО")
            return
        }

        // Отправляем данные на ваш сервер для получения ID секретаря
        getSecretaryId(
            surname = nameParts[0],
            name = nameParts[1],
            patronymic = nameParts[2]
        )
    }

    private fun getSecretaryId(surname: String, name: String, patronymic: String) {
        val request = SecretaryRequest(
            surname = surname,
            name = name,
            patronymic = patronymic
        )

        apiService.getSecretaryId(request).enqueue(object : Callback<SecretaryIdResponse> {
            override fun onResponse(
                call: Call<SecretaryIdResponse>,
                response: Response<SecretaryIdResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { secretary ->
                        openMainActivity(
                            secretaryId = secretary.id,
                            fullName = secretary.full_name
                        )
                    } ?: showError("Пустой ответ от сервера")
                } else {
                    showError("Ошибка получения ID: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SecretaryIdResponse>, t: Throwable) {
                showError("Ошибка: ${t.message}")
            }
        })
    }

    private fun openMainActivity(secretaryId: Int, fullName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("SECRETARY_ID", secretaryId)
            putExtra("FULL_NAME", fullName)
        }
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
   /*private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bitrix_auth)

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                Toast.makeText(this@LoginActivity, "Ошибка: ${error.description}", Toast.LENGTH_SHORT).show()
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // Действия после загрузки страницы
            }
        }

        webView.loadUrl("https://www.example.com") // Замените на нужный URL
    }*/
}
*/

// Модели данных
data class BitrixAuthRequest(val code: String)
data class BitrixAuthResponse(val user: BitrixUser)
data class BitrixUser(
    val id: Int,
    val email: String,
    val full_name: String,
    val is_teacher: Boolean,
    val is_student: Boolean
)
data class SecretaryRequest(
    val surname: String,
    val name: String,
    val patronymic: String
)
data class SecretaryIdResponse(
    val id: Int,
    val full_name: String
)



