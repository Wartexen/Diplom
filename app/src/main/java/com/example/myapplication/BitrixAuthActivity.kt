package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Api.BitrixApiService
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
/*
class BitrixAuthActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bitrix_auth)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        setupWebView()
        testWebView()
    }

    private fun setupWebView() {
        // Включение базовых настроек
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
        }

        // Обработка ошибок и загрузки
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
                Log.d("WebView", "Start loading: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                Log.d("WebView", "Finished loading: $url")
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Log.e("WebView", "Error $errorCode: $description\nURL: $failingUrl")
                showError("Ошибка загрузки: $description")
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Log.e("WebView", "Error ${error?.errorCode}: ${error?.description}")
                showError("Ошибка: ${error?.description}")
            }
        }

        // Разрешение Mixed Content для API 21+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    private fun testWebView() {
        // Тестовые URL с приоритетами
        val testUrls = listOf(
            "https://google.com",         // Самый простой
            "https://httpbin.org/get",     // API-ответ
            "https://int.istu.edu"         // Ваш домен
        )

        // Пробуем загружать по очереди до первого успешного
        for (url in testUrls) {
            Log.d("WebViewTest", "Trying to load: $url")
            try {
                webView.loadUrl(url)
                break
            } catch (e: Exception) {
                Log.e("WebViewTest", "Failed to load $url: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            progressBar.visibility = View.GONE
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            // Показать альтернативный контент
            webView.loadDataWithBaseURL(
                null,
                """<html><body><h1>Ошибка загрузки</h1><p>$message</p></body></html>""",
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

data class AccessTokenResponse(
    @SerializedName("access_token") val access_token: String,
    @SerializedName("client_endpoint") val client_endpoint: String,
    // Другие поля по необходимости
)

data class UserInfoResponse(
    @SerializedName("result") val result: UserInfo
)

data class UserInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("last_name") val last_name: String,
    @SerializedName("name") val name: String,
    @SerializedName("second_name") val second_name: String,
    @SerializedName("is_teacher") val is_teacher: Boolean,
    @SerializedName("is_student") val is_student: Boolean,
    @SerializedName("mira_id") val mira_id: List<String>?
)*/

import androidx.browser.customtabs.CustomTabsIntent

class BitrixAuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // URL авторизации Bitrix
        val authUrl = buildAuthUrl()

        // Пытаемся открыть в Custom Tabs
        if (!openCustomTab(authUrl)) {
            // Если не получилось, показываем ошибку
            Toast.makeText(this, "Не удалось открыть браузер", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun buildAuthUrl(): String {
        return "https://int.istu.edu/oauth/authorize/?" +
                "client_id=local.65581f0597f2b3.73164583&" +
                "response_type=code&" +
                "redirect_uri=yourapp://auth&" +
                "scope=user_info"
    }

    private fun openCustomTab(url: String): Boolean {
        return try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(this, Uri.parse(url))
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleAuthResponse(intent)
    }

    override fun onResume() {
        super.onResume()
        handleAuthResponse(intent)
    }

    private fun handleAuthResponse(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "yourapp" && uri.host == "auth") {
                val code = uri.getQueryParameter("code")

                if (!code.isNullOrEmpty()) {
                    // Получаем токен
                    fetchAccessToken(code)
                } else {
                    // Обработка ошибки
                    Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun fetchAccessToken(code: String) {
        // Здесь реализация получения токена
        Toast.makeText(this, "Получен код: $code", Toast.LENGTH_SHORT).show()

        // После успешной авторизации переходим в основное Activity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

data class AccessTokenResponse(
    @SerializedName("access_token") val access_token: String,
    @SerializedName("client_endpoint") val client_endpoint: String,
    // Другие поля по необходимости
)

data class UserInfoResponse(
    @SerializedName("result") val result: UserInfo
)

data class UserInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("last_name") val last_name: String,
    @SerializedName("name") val name: String,
    @SerializedName("second_name") val second_name: String,
    @SerializedName("is_teacher") val is_teacher: Boolean,
    @SerializedName("is_student") val is_student: Boolean,
    @SerializedName("mira_id") val mira_id: List<String>?
)