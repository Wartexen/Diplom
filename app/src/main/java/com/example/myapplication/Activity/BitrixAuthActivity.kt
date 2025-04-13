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
import com.example.myapplication.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class BitrixAuthActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BitrixAuth"
        private const val BITRIX_AUTH_URL = "https://int.istu.edu/oauth/authorize/"
        private const val BITRIX_TOKEN_URL = "https://int.istu.edu/oauth/token/"
        private const val CLIENT_ID = "local.65581f0597f2b3.73164583"
        private const val CLIENT_SECRET = "9FTLONYzoMlenvlQBm1TUTfRf1x7ZAUtJK948jeyM2mGmvH0z7"
        private const val REDIRECT_URI = "http://localhost:9000/api/accounts/bitrix-auth/"

        fun createStartIntent(context: Context): Intent {
            return Intent(context, BitrixAuthActivity::class.java)
        }
    }

    private lateinit var webView: WebView
    private val client = OkHttpClient()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bitrix_auth)

        // Включаем отладку WebView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView = findViewById(R.id.webView)
        setupWebView()

        // Формируем URL для авторизации
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
                // Только для тестирования! В продакшене обрабатывайте ошибки правильно
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
            exchangeCodeForToken(authCode)
        } else {
            val error = uri.getQueryParameter("error") ?: "no code returned"
            Log.e(TAG, "Authorization failed: $error")
            finishWithError("Authorization failed: $error")
        }
    }

    private fun exchangeCodeForToken(authCode: String) {
        Log.d(TAG, "Exchanging code for token...")

        val requestBody = FormBody.Builder()
            .addEncoded("grant_type", "authorization_code")
            .addEncoded("code", authCode)
            .addEncoded("client_id", CLIENT_ID)
            .addEncoded("client_secret", CLIENT_SECRET)
            .addEncoded("redirect_uri", REDIRECT_URI)
            .build()

        val request = Request.Builder()
            .url(BITRIX_TOKEN_URL)
            .header("Accept", "application/json")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Token exchange failed", e)
                runOnUiThread {
                    finishWithError("Network error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Token response: $responseBody")

                    if (!response.isSuccessful) {
                        val errorMsg = try {
                            JSONObject(responseBody).getString("error_description")
                        } catch (e: Exception) {
                            "Server error (code ${response.code})"
                        }
                        throw IOException(errorMsg)
                    }

                    val json = JSONObject(responseBody)
                    val accessToken = json.getString("access_token")
                    val clientEndpoint = json.getString("client_endpoint")

                    Log.d(TAG, "Access token received, fetching user info...")
                    fetchUserInfo(accessToken, clientEndpoint)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing token response", e)
                    runOnUiThread {
                        finishWithError("Error: ${e.message}")
                    }
                }
            }
        })
    }

    private fun fetchUserInfo(accessToken: String, clientEndpoint: String) {
        val userInfoUrl = "$clientEndpoint/user.info.json?auth=$accessToken"
        Log.d(TAG, "Fetching user info from: $userInfoUrl")

        val request = Request.Builder()
            .url(userInfoUrl)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch user info", e)
                runOnUiThread {
                    finishWithError("Network error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "User info response: $responseBody")

                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }

                    val json = JSONObject(responseBody)
                    val result = json.getJSONObject("result")

                    Log.d(TAG, "User info received: ${result.toString()}")
                    saveUserData(result)
                    runOnUiThread {
                        finishWithSuccess()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing user info", e)
                    runOnUiThread {
                        finishWithError("Error: ${e.message}")
                    }
                }
            }
        })
    }

    private fun saveUserData(userJson: JSONObject) {
        try {
            val sharedPref = getSharedPreferences("BitrixAuth", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("bitrix_user_id", userJson.getInt("id"))
                putString("email", userJson.getString("email"))
                putString("first_name", userJson.optString("name", ""))
                putString("last_name", userJson.optString("last_name", ""))
                putString("second_name", userJson.optString("second_name", ""))
                putBoolean("is_teacher", userJson.optBoolean("is_teacher"))
                putBoolean("is_student", userJson.optBoolean("is_student"))
                apply()
            }
            Log.d(TAG, "User data saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user data", e)
            throw e
        }
    }

    private fun finishWithSuccess() {
        Log.d(TAG, "Auth completed successfully")
        setResult(RESULT_OK)
        finish()
    }

    private fun finishWithError(message: String) {
        Log.e(TAG, "Auth failed: $message")
        val resultIntent = Intent()
        resultIntent.putExtra("error", message)
        setResult(RESULT_CANCELED, resultIntent)
        finish()
    }
}