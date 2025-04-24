//package com.example.myapplication.Activity
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.content.pm.PackageManager
//import android.media.MediaRecorder
//import android.net.Uri
//import android.os.Bundle
//import android.os.Environment
//import android.os.Handler
//import android.os.Looper
//import android.provider.Settings
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.view.Menu
//import android.view.MenuItem
//import android.view.View
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.Toolbar
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.myapplication.Adapter.QuestionAdapter
//import com.example.myapplication.Adapter.StudentAdapter
//import com.example.myapplication.Api.ApiService
//
//import com.example.myapplication.Models.Project
//import com.example.myapplication.Models.Question
//import com.example.myapplication.Models.Student
//import com.example.myapplication.R
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.io.File
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class ProjectDetailsActivity : AppCompatActivity() {
//
//    private lateinit var micButton: FloatingActionButton
//    private lateinit var recordingTimeTextView: TextView
//    private lateinit var studentsRecyclerView: RecyclerView
//    private lateinit var questionsRecyclerView: RecyclerView
//    private lateinit var apiService: ApiService
//    private lateinit var project: Project
//    private var mediaRecorder: MediaRecorder? = null
//    private var audioFilePath: String? = null
//    private var isRecording = false
//    private var recordingStartTime: Long = 0
//    private val handler = Handler(Looper.getMainLooper())
//    private lateinit var sharedPref: SharedPreferences
//    private lateinit var toolbar: Toolbar
//    private lateinit var userName: TextView
//    private lateinit var profileIcon: ImageView
//    private val RECORD_AUDIO_PERMISSION_CODE = 123
//    private var studentList: List<Student> = emptyList()
//    private var questionList: List<Question> = emptyList()
//    private lateinit var studentAdapter: StudentAdapter
//    private lateinit var questionAdapter: QuestionAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_project_details)
//
//        micButton = findViewById(R.id.micButton)
//        recordingTimeTextView = findViewById(R.id.recordingTime)
//        studentsRecyclerView = findViewById(R.id.studentsRecyclerView)
//        questionsRecyclerView = findViewById(R.id.questionsRecyclerView)
//
//        sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//        toolbar = findViewById(R.id.toolbar)
//        userName = findViewById(R.id.userName)
//        profileIcon = findViewById(R.id.profileIcon)
//        checkAuthStatus()
//        // Инициализация Retrofit
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8000/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        apiService = retrofit.create(ApiService::class.java)
//        project = intent.getParcelableExtra<Project>("project") ?: return
//
//        findViewById<TextView>(R.id.projectNameTextView).text = project.Title
//        findViewById<TextView>(R.id.projectLeaderTextView).text = project.Supervisor
//
//        val projectIdString = project.ID
//        val projectId = projectIdString.toIntOrNull()
//
//        if (projectId != null) {
//            getStudentsByProject(projectId)
//            sendQuestionsRequest(projectId)
//        } else {
//            Toast.makeText(this, "Неверный ID проекта", Toast.LENGTH_SHORT).show()
//        }
//
//        micButton.setOnClickListener {
//            if (isRecording) {
//                stopRecording()
//            } else {
//                startRecording()
//            }
//        }
//
//        // Add click listener for fabAddQuestion
//        val fabAddQuestion = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddQuestion)
//        fabAddQuestion.setOnClickListener {
//            showAddQuestionDialog()
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_logout -> {
//                logout()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//
//    private fun logout() {
//        val editor = sharedPref.edit()
//        editor.remove("token")
//        editor.apply()
//
//        val intent = Intent(this, LoginActivity::class.java)
//        startActivity(intent)
//        finish()
//    }
//
//    private fun checkAuthStatus() {
//        val token = sharedPref.getString("token", null)
//        if (token == null) {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//        } else {
//            loadUserProfile()
//        }
//    }
//
//    private fun loadUserProfile() {
//        val token = sharedPref.getString("token", null)
//        if (token != null) {
//            val retrofit = Retrofit.Builder()
//                .baseUrl("http://10.0.2.2:8000/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//
//            val apiService = retrofit.create(ApiService::class.java)
//            val call = apiService.getUserProfile("Bearer $token")
//
//            call.enqueue(object : Callback<com.example.myapplication.Models.User> {
//                override fun onResponse(call: Call<com.example.myapplication.Models.User>, response: Response<com.example.myapplication.Models.User>) {
//                    if (response.isSuccessful) {
//                        val user = response.body()
//                        userName.text = user?.username ?: "Гость"
//                        val profileImageUrl = user?.profile_image
//                        if (profileImageUrl != null) {
//                            Glide.with(this@ProjectDetailsActivity)
//                                .load("http://10.0.2.2:8000${profileImageUrl}")
//                                .placeholder(R.drawable.ic_profile)
//                                .error(R.drawable.ic_profile)
//                                .into(profileIcon)
//                        } else {
//                            profileIcon.setImageResource(R.drawable.ic_profile)
//                        }
//                    } else {
//                        Toast.makeText(this@ProjectDetailsActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<com.example.myapplication.Models.User>, t: Throwable) {
//                    Toast.makeText(this@ProjectDetailsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
//                }
//            })
//        }
//    }
//
//    private fun getStudentsByProject(projectId: Int) {
//        apiService.getStudentsByProject(projectId).enqueue(object : Callback<List<Student>> {
//            override fun onResponse(call: Call<List<Student>>, response: Response<List<Student>>) {
//                if (response.isSuccessful) {
//                    studentList = response.body() ?: emptyList()
//                    setupStudentsRecyclerView(studentList)
//                } else {
//                    Toast.makeText(this@ProjectDetailsActivity, "Failed to load students", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<List<Student>>, t: Throwable) {
//                Toast.makeText(this@ProjectDetailsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun sendQuestionsRequest(projectId: Int) {
//        apiService.getQuestionsByProject(projectId).enqueue(object : Callback<List<Question>> {
//            override fun onResponse(call: Call<List<Question>>, response: Response<List<Question>>) {
//                if (response.isSuccessful) {
//                    questionList = response.body() ?: emptyList()
//                    setupQuestionsRecyclerView(questionList)
//                } else {
//                    Toast.makeText(this@ProjectDetailsActivity, "Failed to load questions", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<List<Question>>, t: Throwable) {
//                Toast.makeText(this@ProjectDetailsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun setupStudentsRecyclerView(students: List<Student>) {
//        studentAdapter = StudentAdapter(students)
//        studentsRecyclerView.apply {
//            layoutManager = LinearLayoutManager(this@ProjectDetailsActivity)
//            adapter = studentAdapter
//        }
//    }
//
//    private fun setupQuestionsRecyclerView(questions: List<Question>) {
//        questionAdapter = QuestionAdapter(questions)
//        questionsRecyclerView.apply {
//            layoutManager = LinearLayoutManager(this@ProjectDetailsActivity)
//            adapter = questionAdapter
//        }
//    }
//
//    private fun startRecording() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
//        } else {
//            startRecordingProcess()
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startRecordingProcess()
//            } else {
//                showPermissionDeniedDialog()
//            }
//        }
//    }
//
//    private fun showPermissionDeniedDialog() {
//        androidx.appcompat.app.AlertDialog.Builder(this)
//            .setTitle("Требуется разрешение")
//            .setMessage("Для записи аудио необходимо разрешение на использование микрофона. Пожалуйста, предоставьте разрешение в настройках приложения.")
//            .setPositiveButton("Перейти в настройки") { _, _ ->
//                openAppSettings()
//            }
//            .setNegativeButton("Отмена", null)
//            .show()
//    }
//
//    private fun openAppSettings() {
//        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//        val uri: Uri = Uri.fromParts("package", packageName, null)
//        intent.data = uri
//        startActivity(intent)
//    }
//
//    @SuppressLint("SimpleDateFormat")
//    private fun startRecordingProcess() {
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//        val audioFileName = "audio_$timeStamp.3gp"
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
//        val audioFile = File(storageDir, audioFileName)
//
//        audioFilePath = audioFile.absolutePath
//
//        mediaRecorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//            setOutputFile(audioFilePath)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//
//            try {
//                prepare()
//            } catch (e: IOException) {
//                Log.e("AudioRecord", "prepare() failed: ${e.message}")
//                Toast.makeText(this@ProjectDetailsActivity, "Ошибка при подготовке записи", Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            start()
//            isRecording = true
//            recordingStartTime = System.currentTimeMillis()
//            updateRecordingTime()
//            //micButton.setImageResource(R.drawable.ic_stop)
//            Toast.makeText(this@ProjectDetailsActivity, "Начало записи", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun stopRecording() {
//        mediaRecorder?.apply {
//            try {
//                stop()
//                release()
//                Toast.makeText(this@ProjectDetailsActivity, "Запись остановлена. Файл сохранен", Toast.LENGTH_SHORT).show()
//            } catch (e: RuntimeException) {
//                Log.e("AudioRecord", "stop() failed: ${e.message}")
//                Toast.makeText(this@ProjectDetailsActivity, "Ошибка при остановке записи", Toast.LENGTH_SHORT).show()
//                audioFilePath?.let { File(it).delete() }
//            } finally {
//                mediaRecorder = null
//                isRecording = false
//                handler.removeCallbacks(updateRecordingTimeRunnable)
//                //micButton.setImageResource(R.drawable.ic_mic)
//                recordingTimeTextView.text = "00:00"
//            }
//        }
//    }
//
//    private val updateRecordingTimeRunnable = object : Runnable {
//        override fun run() {
//            updateRecordingTime()
//            handler.postDelayed(this, 1000)
//        }
//    }
//
//    private fun updateRecordingTime() {
//        val elapsedTime = System.currentTimeMillis() - recordingStartTime
//        val seconds = (elapsedTime / 1000).toInt()
//        val minutes = seconds / 60
//        val displaySeconds = seconds % 60
//        recordingTimeTextView.text = String.format("%02d:%02d", minutes, displaySeconds)
//        handler.post(updateRecordingTimeRunnable)
//    }
//
//    // Add method to show dialog for adding a new question
//    private fun showAddQuestionDialog() {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_add_question, null)
//        val etQuestionText = dialogView.findViewById<EditText>(R.id.etQuestionText)
//
//        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
//            .setTitle("Добавить вопрос")
//            .setView(dialogView)
//            .setPositiveButton("Добавить") { _, _ ->
//                val questionText = etQuestionText.text.toString().trim()
//
//                if (questionText.isEmpty()) {
//                    Toast.makeText(this, "Текст вопроса не может быть пустым", Toast.LENGTH_SHORT).show()
//                    return@setPositiveButton
//                }
//
//                val projectId = project.ID.toInt()
//                val questionRequest = com.example.myapplication.Models.QuestionRequest(questionText, projectId)
//
//                addQuestion(questionRequest)
//            }
//            .setNegativeButton("Отмена", null)
//            .create()
//
//        dialog.show()
//    }
//
//    // Add method to send the new question to the server
//    private fun addQuestion(questionRequest: com.example.myapplication.Models.QuestionRequest) {
//        apiService.createQuestion(questionRequest).enqueue(object : Callback<Question> {
//            override fun onResponse(call: Call<Question>, response: Response<Question>) {
//                if (response.isSuccessful) {
//                    val newQuestion = response.body()
//                    if (newQuestion != null) {
//                        Toast.makeText(this@ProjectDetailsActivity, "Вопрос успешно добавлен", Toast.LENGTH_SHORT).show()
//                        // Refresh the questions list to include the new question
//                        sendQuestionsRequest(project.ID.toInt())
//                    }
//                } else {
//                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
//                    Log.e("API Error", "Error adding question: $errorMessage")
//                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при добавлении вопроса", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<Question>, t: Throwable) {
//                Log.e("Network Error", "Failed to add question: ${t.message}")
//                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при добавлении вопроса", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//}

package com.example.myapplication.Activity
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Handler
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import retrofit2.Response
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Project
import com.example.myapplication.Models.Question
import com.example.myapplication.Models.Student
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.example.myapplication.Models.UploadResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.example.myapplication.Adapter.QuestionAdapter
import com.example.myapplication.R
import com.example.myapplication.Adapter.StudentAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.EditText

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var micButton: ImageView
    private lateinit var recordingTimeTextView: TextView
    private lateinit var studentsRecyclerView: RecyclerView
    private lateinit var questionsRecyclerView: RecyclerView
    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var apiService: ApiService
    private lateinit var project: Project
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var userName: TextView
    private lateinit var profileIcon: ImageView
    private lateinit var sharedPref: SharedPreferences
    private var audioFilePath: String = ""
    private var recordingTime: Int = 0
    private lateinit var recordingRunnable: Runnable
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private lateinit var questionAdapter: QuestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        micButton = findViewById(R.id.micButton)
        recordingTimeTextView = findViewById(R.id.recordingTime)
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView)
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView)

        sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        toolbar = findViewById(R.id.toolbar)
        userName = findViewById(R.id.userName)
        profileIcon = findViewById(R.id.profileIcon)
        checkAuthStatus()
        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        project = intent.getParcelableExtra<Project>("project") ?: return

        findViewById<TextView>(R.id.projectNameTextView).text = project.Title
        findViewById<TextView>(R.id.projectLeaderTextView).text = project.Supervisor

        val projectId = project.ID
        //val projectId = projectIdString.toIntOrNull()

        if (projectId != null) {
            getStudentsByProject(projectId)
            sendQuestionsRequest(projectId)
        } else {
            Toast.makeText(this, "Неверный ID проекта", Toast.LENGTH_SHORT).show()
        }

        micButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
        val fabAddQuestion = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddQuestion)
        fabAddQuestion.setOnClickListener {
            showAddQuestionDialog()
        }

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

    private fun getStudentsByProject(projectId: Int) {
        apiService.getStudentsByProject(projectId).enqueue(object : Callback<List<Student>> {
            override fun onResponse(call: Call<List<Student>>, response: Response<List<Student>>) {
                if (response.isSuccessful) {
                    val students = response.body() ?: emptyList()
                    studentsRecyclerView.layoutManager = LinearLayoutManager(this@ProjectDetailsActivity)
                    studentsRecyclerView.adapter = StudentAdapter(students)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Log.e("API Error", errorMessage)
                }
            }

            override fun onFailure(call: Call<List<Student>>, t: Throwable) {
                Log.e("Network Error", t.message ?: "Неизвестная ошибка")
            }
        })
    }
    private fun sendQuestionsRequest(projectId: Int) {
        apiService.getQuestionsByProject(projectId).enqueue(object : Callback<List<Question>> {
            override fun onResponse(call: Call<List<Question>>, response: Response<List<Question>>) {
                if (response.isSuccessful) {
                    val questions = response.body() ?: emptyList()
                    questionAdapter = QuestionAdapter(questions.toMutableList())
                    questionAdapter.setOnQuestionDeleteListener { question ->
                        deleteQuestionOnServer(question)
                    }
                    questionAdapter.setOnQuestionSaveListener { question, newText ->
                        updateQuestionOnServer(question.ID, newText)
                    }
                    questionsRecyclerView.layoutManager = LinearLayoutManager(this@ProjectDetailsActivity)
                    questionsRecyclerView.adapter = questionAdapter
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при получении вопросов", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Question>>, t: Throwable) {
                Log.e("Network Error", t.message ?: "Неизвестная ошибка")
                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            return
        }
        try {
            audioFilePath = "${externalCacheDir?.absolutePath}/audio_record.3gp"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            isRecording = true
            recordingTime = 0
            updateRecordingTime()
            micButton.setImageResource(R.drawable.ic_mic_on)
        } catch (e: Exception) {
            Log.e("Recording", "Ошибка записи: ${e.message}")
            Toast.makeText(this, "Ошибка записи", Toast.LENGTH_SHORT).show()
            releaseMediaRecorder()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            } else {
                Toast.makeText(this, "Доступ к микрофону запрещён", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun releaseMediaRecorder() {
        mediaRecorder?.release()
        mediaRecorder = null
    }
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        isRecording = false
        handler.post {
            micButton.setImageResource(R.drawable.ic_mic_off)
        }
        handler.removeCallbacks(recordingRunnable) // Остановите обновление времени
        uploadAudioFile()
    }
    private fun updateRecordingTime() {
        recordingRunnable = Runnable {
            if (isRecording) {
                recordingTime++
                val minutes = recordingTime / 60
                val seconds = recordingTime % 60
                recordingTimeTextView.text = String.format("%02d:%02d", minutes, seconds) // Форматирование времени
                handler.postDelayed(recordingRunnable, 1000) // Обновление каждую секунду
            }
        }
        handler.post(recordingRunnable) // Запускаем Runnable
    }
    private fun uploadAudioFile() {
        val file = File(audioFilePath)
        if (!file.exists()) {
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
            return
        }

        val requestFile = RequestBody.create("audio/3gp".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("audio", file.name, requestFile)

        // Получите ID проекта
        val projectId = project.ID
        val projectIdRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), projectId.toString())

        apiService.uploadAudio(body, projectIdRequestBody).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProjectDetailsActivity, "Аудио успешно отправлено", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при отправке аудио", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                Log.e("Upload Error", t.message ?: "Неизвестная ошибка")
            }
        })
    }

    private fun updateQuestionOnServer(questionId: Int, newText: String) {
        apiService.updateQuestion(questionId, newText).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProjectDetailsActivity, "Вопрос успешно обновлен", Toast.LENGTH_SHORT).show()
                    sendQuestionsRequest(project.ID)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при обновлении вопроса: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при обновлении вопроса", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun deleteQuestionOnServer(questionId: Int) {
        apiService.deleteQuestion(questionId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProjectDetailsActivity, "Вопрос удалён", Toast.LENGTH_SHORT).show()
                    sendQuestionsRequest(project.ID)
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProjectDetailsActivity, "Сетевая ошибка", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddQuestionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_question, null)
        val etQuestionText = dialogView.findViewById<EditText>(R.id.etQuestionText)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Добавить вопрос").setView(dialogView).setPositiveButton("Добавить") { _, _ ->
                val questionText = etQuestionText.text.toString().trim()
                if (questionText.isEmpty()) {
                    Toast.makeText(this, "Текст вопроса не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val projectId = project.ID
                val questionRequest = com.example.myapplication.Models.QuestionRequest(questionText, projectId)
                addQuestion(questionRequest)
            }.setNegativeButton("Отмена", null).create()
        dialog.show()
    }

    private fun addQuestion(questionRequest: com.example.myapplication.Models.QuestionRequest) {
        apiService.createQuestion(questionRequest).enqueue(object : Callback<Question> {
            override fun onResponse(call: Call<Question>, response: Response<Question>) {
                if (response.isSuccessful) {
                    val newQuestion = response.body()
                    if (newQuestion != null) {
                        Toast.makeText(this@ProjectDetailsActivity, "Вопрос успешно добавлен", Toast.LENGTH_SHORT).show()
                        sendQuestionsRequest(project.ID)
                    }
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при добавлении вопроса", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Question>, t: Throwable) {
                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при добавлении вопроса", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
