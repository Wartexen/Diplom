package com.example.myapplication.Activity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Auth.SecretaryResponse
import com.example.myapplication.Models.Requests.BitrixAuthRequest
import com.example.myapplication.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.net.URLEncoder

class BitrixAuthActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BitrixAuth"
        private const val BITRIX_AUTH_URL = "https://int.istu.edu/oauth/authorize/"
        private const val CLIENT_ID = "local.65581f0597f2b3.73164583"
        private const val REDIRECT_URI = "http://localhost:9000/api/accounts/bitrix-auth/"

        fun createStartIntent(context: Context): Intent {
            return Intent(context, BitrixAuthActivity::class.java)
        }
    }

    private lateinit var webView: WebView
    private lateinit var apiService: ApiService

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bitrix_auth)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView = findViewById(R.id.webView)
        setupWebView()

        val authUrl = buildAuthUrl()
        Log.d(TAG, "Starting auth with URL: $authUrl")
        webView.loadUrl(authUrl)
    }

    private fun buildAuthUrl(): String {
        return "$BITRIX_AUTH_URL?" +
                "client_id=$CLIENT_ID&" +
                "redirect_uri=${URLEncoder.encode(REDIRECT_URI, "UTF-8")}&" +
                "response_type=code"
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                Log.d(TAG, "Processing URL (new): $url")
                return handleUrl(url)
            }

            @Deprecated("Deprecated in API 24")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.d(TAG, "Processing URL (old): $url")
                return handleUrl(url)
            }

            private fun handleUrl(url: String): Boolean {
                return if (url.startsWith(REDIRECT_URI)) {
                    Log.d(TAG, "Redirect URI matched: $url")
                    handleRedirectUrl(url)
                    true
                } else {
                    false
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Log.d(TAG, "Page started: $url")
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d(TAG, "Page finished: $url")
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                val errorMsg = "Error: ${error?.description} (${request?.url})"
                Log.e(TAG, errorMsg)
                runOnUiThread {
                    Toast.makeText(this@BitrixAuthActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(view, request, errorResponse)
                val errorMsg = "HTTP Error: ${errorResponse?.statusCode} (${request?.url})"
                Log.e(TAG, errorMsg)
                runOnUiThread {
                    Toast.makeText(this@BitrixAuthActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError?) {
                Log.w(TAG, "SSL Error: ${error?.toString()}")
                handler.proceed()
            }
        }
    }

    private fun handleRedirectUrl(url: String) {
        Log.d(TAG, "Handling redirect URL: $url")
        val uri = Uri.parse(url)
        val authCode = uri.getQueryParameter("code")

        if (authCode != null) {
            Log.d(TAG, "Authorization code received: $authCode")
            exchangeCodeOnServer(authCode)
        } else {
            val error = uri.getQueryParameter("error") ?: "No code returned"
            Log.e(TAG, "Authorization failed: $error")
            finishWithError("Authorization failed: $error")
        }
    }

    private fun exchangeCodeOnServer(authCode: String) {
        Log.d(TAG, "Exchanging code on server...")
        val requestBody = BitrixAuthRequest(
            code = authCode,
            clientId = CLIENT_ID,
            clientSecret = "9FTLONYzoMlenvlQBm1TUTfRf1x7ZAUtJK948jeyM2mGmvH0z7",
            redirectUri = REDIRECT_URI
        )

        apiService.exchangeBitrixCode(requestBody).enqueue(object : Callback<List<SecretaryResponse>> {
            override fun onResponse(call: Call<List<SecretaryResponse>>, response: Response<List<SecretaryResponse>>) {
                try {
                    if (response.isSuccessful) {
                        response.body()?.let { secretaryList ->
                            if (secretaryList.isNotEmpty()) {
                                val secretary = secretaryList[0]
                                val fullName = "${secretary.Surname} ${secretary.Name} ${secretary.Patronymic}"
                                saveUserData(secretary.ID, fullName)

                                val intent = Intent(this@BitrixAuthActivity, MainActivity::class.java).apply {
                                    putExtra("secretaryId", secretary.ID)
                                    putExtra("fullName", fullName)
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                showToast("Секретарь не найден.")
                                finishWithError("Secretary not found")
                            }
                        } ?: run {
                            showToast("Пустой ответ сервера")
                            finishWithError("Empty response from server")
                        }
                    } else {
                        val errorMsg = "Server error: ${response.code()}"
                        showToast(errorMsg)
                        finishWithError(errorMsg)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing server response", e)
                    showToast("Ошибка: ${e.message}")
                    finishWithError("Error: ${e.message}")
                }
            }

            override fun onFailure(call: Call<List<SecretaryResponse>>, t: Throwable) {
                Log.e(TAG, "Server request failed", t)
                showToast("Ошибка сети: ${t.message}")
                finishWithError("Network error: ${t.message}")
            }
        })
    }

    private fun saveUserData(secretaryId: Int, fullName: String) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", true)
            putInt("secretaryId", secretaryId)
            putString("fullName", fullName)
            apply()
        }
        Log.d(TAG, "User data saved successfully")
    }

    private fun showToast(message: String?) {
        runOnUiThread {
            Toast.makeText(this@BitrixAuthActivity, message ?: "Unknown error", Toast.LENGTH_LONG).show()
        }
    }

    private fun finishWithError(message: String?) {
        Log.e(TAG, "Auth failed: $message")

    }
}
