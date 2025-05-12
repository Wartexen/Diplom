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
import com.example.myapplication.Models.Db.Project
import com.example.myapplication.Models.Db.Protocol
import com.example.myapplication.Models.Db.Question
import com.example.myapplication.Models.Db.Student
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.example.myapplication.Adapter.QuestionAdapter
import com.example.myapplication.R
import com.example.myapplication.Adapter.StudentAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.EditText
import com.example.myapplication.Models.Requests.ProjectTimeEndRequest
import com.example.myapplication.Models.Requests.QuestionRequest
import com.example.myapplication.Models.Requests.ProjectTimeRequest
import com.example.myapplication.Models.Requests.QuestionUpdateRequest
import com.example.myapplication.Models.Response.UploadResponse
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var micButton: ImageView
    private lateinit var uploadAudioButton: ImageView
    private lateinit var recordingTimeTextView: TextView
    private lateinit var savedAudioInfoTextView: TextView
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

    private lateinit var startDefenseContainer: View
    private lateinit var mainContentContainer: View
    private lateinit var btnStartDefense: Button
    private lateinit var actionMenuButton: ImageView
    private lateinit var defenseStartTimeTextView: TextView

    private var projectStatus = false

    private val savedAudioFiles = mutableListOf<String>()

    private var studentsList = mutableListOf<Student>()

    private companion object {

        private const val SAVED_AUDIO_FILES_PREFIX = "saved_audio_files_project_"
        private const val TAG = "ProjectDetailsActivity"
    }

    private val getAudioContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val audioFile = copyUriToFile(it)
            if (audioFile != null) {
                uploadAudioFile(audioFile.absolutePath)
            } else {
                Toast.makeText(this, "Не удалось загрузить аудиофайл", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        micButton = findViewById(R.id.micButton)
        uploadAudioButton = findViewById(R.id.uploadAudioButton)
        recordingTimeTextView = findViewById(R.id.recordingTime)
        savedAudioInfoTextView = findViewById(R.id.savedAudioInfo)
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView)
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView)
        startDefenseContainer = findViewById(R.id.startDefenseContainer)
        mainContentContainer = findViewById(R.id.mainContentContainer)
        btnStartDefense = findViewById(R.id.btnStartDefense)
        actionMenuButton = findViewById(R.id.actionMenuButton)
        defenseStartTimeTextView = findViewById(R.id.defenseStartTimeTextView)

        sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        toolbar = findViewById(R.id.toolbar)
        userName = findViewById(R.id.userName)
        profileIcon = findViewById(R.id.profileIcon)

        checkAuthStatus()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        project = intent.getParcelableExtra<Project>("project") ?: return

        findViewById<TextView>(R.id.projectNameTextView).text = project.Title
        findViewById<TextView>(R.id.projectLeaderTextView).text = project.Supervisor

        val projectId = project.ID
        loadSavedAudioFiles()

        if (projectId != null) {
            getStudentsByProject(projectId)
            getProjectStatus(projectId)
        } else {
            Toast.makeText(this, "Неверный ID проекта", Toast.LENGTH_SHORT).show()
        }

        micButton.setOnClickListener {
            if (isRecording) {
                showStopRecordingDialog()
            } else {
                showStartRecordingDialog()
            }
        }

        uploadAudioButton.setOnClickListener {
            showAudioFilePickerDialog()
        }

        val fabAddQuestion = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddQuestion)
        fabAddQuestion.setOnClickListener {
            showAddQuestionDialog()
        }
        btnStartDefense.setOnClickListener {
            startDefense()
        }
        actionMenuButton.setOnClickListener {
            showActionMenu(it)
        }
    }

    private fun loadSavedAudioFiles() {
        try {
            val projectId = project.ID
            val savedFilesKey = SAVED_AUDIO_FILES_PREFIX + projectId
            val savedFilesSet = sharedPref.getStringSet(savedFilesKey, setOf()) ?: setOf()
            savedAudioFiles.clear()
            savedAudioFiles.addAll(savedFilesSet.filter { File(it).exists() })

            if (savedAudioFiles.isNotEmpty()) {
                savedAudioInfoTextView.visibility = View.VISIBLE
                savedAudioInfoTextView.text = "Сохранено аудиозаписей: ${savedAudioFiles.size}"
            } else {
                savedAudioInfoTextView.visibility = View.GONE
            }
        } catch (e: Exception) {
            savedAudioInfoTextView.visibility = View.GONE
        }
    }
    private fun saveSavedAudioFilesList() {
        try {
            val projectId = project.ID
            val savedFilesKey = SAVED_AUDIO_FILES_PREFIX + projectId
            sharedPref.edit().putStringSet(savedFilesKey, savedAudioFiles.toSet()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving audio files list: ${e.message}")
        }
    }

    private fun showAudioFilePickerDialog() {
        loadSavedAudioFiles()

        if (savedAudioFiles.isEmpty()) {
            getAudioContent.launch("audio/*")
        } else {
            val options = arrayOf("Выбрать новый файл", "Использовать сохраненную запись проекта")

            MaterialAlertDialogBuilder(this)
                .setTitle("Загрузка аудиофайла")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> getAudioContent.launch("audio/*")
                        1 -> showSavedAudioFilesDialog()
                    }
                }
                .show()
        }
    }

    private fun copyUriToFile(uri: Uri): File? {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val fileName = "uploaded_audio_${System.currentTimeMillis()}.3gp"
                val outputFile = File(externalCacheDir, fileName)

                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }

                inputStream.close()
                return outputFile
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error copying audio file: ${e.message}")
        }
        return null
    }

    private fun showStartRecordingDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Начать запись")
            .setMessage("Вы хотите начать запись аудио?")
            .setPositiveButton("Да") { _, _ ->
                startRecording()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showStopRecordingDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Остановить запись")
            .setMessage("Что вы хотите сделать с текущей записью?")
            .setPositiveButton("Сохранить и отправить") { _, _ ->
                stopRecording()
                uploadAudioFile(audioFilePath, true)
                setDefenseEndTime()
            }
            .setNeutralButton("Отменить запись") { _, _ ->
                cancelRecording()
            }
            .setNegativeButton("Продолжить запись", null)
            .show()
    }

    private fun saveAudioFileToDevice(): String? {
        try {
            val sourceFile = File(audioFilePath)
            if (!sourceFile.exists()) {
                Toast.makeText(this, "Файл записи не найден", Toast.LENGTH_SHORT).show()
                return null
            }

            val projectId = project.ID
            val audioDir = File(filesDir, "saved_audio/project_$projectId")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }


            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "audio_${project.ID}_$timestamp.3gp"
            val destinationFile = File(audioDir, fileName)

            sourceFile.copyTo(destinationFile, overwrite = true)
            return destinationFile.absolutePath

        } catch (e: IOException) {
            Toast.makeText(this, "Ошибка при сохранении аудиозаписи", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            val file = File(audioFilePath)
            if (file.exists()) {
                file.delete()
            }

            isRecording = false
            handler.removeCallbacks(recordingRunnable)
            recordingTimeTextView.text = "00:00"
            micButton.setImageResource(R.drawable.ic_mic_off)

            Toast.makeText(this, "Запись отменена", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при отмене записи", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getProjectStatus(projectId: Int) {
        apiService.getProjectStatus(projectId).enqueue(object : Callback<Project> {
            override fun onResponse(call: Call<Project>, response: Response<Project>) {
                if (response.isSuccessful) {
                    val projectResponse = response.body()
                    if (projectResponse != null) {
                        project = projectResponse
                        projectStatus = projectResponse.Status
                    }
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при получении статуса проекта", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Project>, t: Throwable) {
                Log.e(TAG, "Network error getting project status: ${t.message}")
                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при получении статуса проекта", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getStudentsByProject(projectId: Int) {
        apiService.getStudentsByProject(projectId).enqueue(object : Callback<List<Student>> {
            override fun onResponse(call: Call<List<Student>>, response: Response<List<Student>>) {
                if (response.isSuccessful) {
                    val students = response.body() ?: emptyList()
                    studentsList.clear()
                    studentsList.addAll(students)

                    studentsRecyclerView.layoutManager = LinearLayoutManager(this@ProjectDetailsActivity)
                    studentsRecyclerView.adapter = StudentAdapter(students)

                    if (students.isNotEmpty()) {
                        val firstStudentId = students[0].ID
                        checkDefenseStartTime(firstStudentId)
                    } else {
                        startDefenseContainer.visibility = View.VISIBLE
                        mainContentContainer.visibility = View.GONE
                        actionMenuButton.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при получении списка студентов", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Student>>, t: Throwable) {
                Log.e("Network Error", t.message ?: "Неизвестная ошибка")
                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при получении списка студентов", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkDefenseStartTime(studentId: Int) {
        apiService.getProtocolsByStudentId(studentId).enqueue(object : Callback<List<Protocol>> {
            override fun onResponse(call: Call<List<Protocol>>, response: Response<List<Protocol>>) {
                if (response.isSuccessful) {
                    val protocols = response.body() ?: emptyList()

                    if (protocols.isNotEmpty()) {
                        val protocol = protocols[0]
                        val defenseStartTime = protocol.DefenseStartTime
                        if (defenseStartTime.isNullOrEmpty()) {
                            startDefenseContainer.visibility = View.VISIBLE
                            mainContentContainer.visibility = View.GONE
                            actionMenuButton.visibility = View.GONE
                            defenseStartTimeTextView.text = "Не начата"
                        } else {
                            startDefenseContainer.visibility = View.GONE
                            mainContentContainer.visibility = View.VISIBLE
                            actionMenuButton.visibility = View.VISIBLE

                            defenseStartTimeTextView.text = defenseStartTime

                            sendQuestionsRequest(project.ID)
                        }
                    } else {
                        startDefenseContainer.visibility = View.VISIBLE
                        mainContentContainer.visibility = View.GONE
                        actionMenuButton.visibility = View.GONE
                        defenseStartTimeTextView.text = "Не начата"
                    }
                } else {

                    startDefenseContainer.visibility = View.VISIBLE
                    mainContentContainer.visibility = View.GONE
                    actionMenuButton.visibility = View.GONE
                    defenseStartTimeTextView.text = "Ошибка"
                }
            }
            override fun onFailure(call: Call<List<Protocol>>, t: Throwable) {
                startDefenseContainer.visibility = View.VISIBLE
                mainContentContainer.visibility = View.GONE
                actionMenuButton.visibility = View.GONE
                defenseStartTimeTextView.text = "Ошибка сети"
            }
        })
    }

    private fun startDefense() {
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        MaterialAlertDialogBuilder(this)
            .setTitle("Начать защиту")
            .setMessage("Вы уверены, что хотите начать защиту проекта? Время начала: $currentTime")
            .setPositiveButton("Да") { _, _ ->
                val projectTimeRequest = ProjectTimeRequest(ID_Project = project.ID, DefenseStartTime = currentTime)
                apiService.setProjectStartTime(projectTimeRequest).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            startDefenseContainer.visibility = View.GONE
                            mainContentContainer.visibility = View.VISIBLE
                            actionMenuButton.visibility = View.VISIBLE
                            defenseStartTimeTextView.text = currentTime

                            sendQuestionsRequest(project.ID)
                            Toast.makeText(this@ProjectDetailsActivity, "Защита началась в $currentTime", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ProjectDetailsActivity,
                                "Ошибка при установке времени начала защиты: ${response.code()}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@ProjectDetailsActivity,
                            "Ошибка сети при установке времени начала защиты: ${t.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun setDefenseEndTime() {
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val projectTimeEndRequest = ProjectTimeEndRequest(ID_Project = project.ID, DefenseEndTime = currentTime)
        apiService.setProjectEndTime(projectTimeEndRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ProjectDetailsActivity,
                        "Защита завершена в $currentTime",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Toast.makeText(
                        this@ProjectDetailsActivity,
                        "Ошибка при установке времени окончания защиты: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    this@ProjectDetailsActivity,
                    "Ошибка сети при установке времени окончания защиты: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showActionMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_defense, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_change_time -> {
                    showChangeTimeDialog()
                    true
                }
                R.id.action_cancel_defense -> {
                    showCancelDefenseConfirmation()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showCancelDefenseConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Отменить защиту")
            .setMessage("Вы уверены, что хотите отменить защиту проекта?")
            .setPositiveButton("Да") { _, _ ->
                val projectTimeRequest = ProjectTimeRequest(ID_Project = project.ID, DefenseStartTime = null)
                apiService.setProjectStartTime(projectTimeRequest).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {

                            mainContentContainer.visibility = View.GONE
                            startDefenseContainer.visibility = View.VISIBLE
                            actionMenuButton.visibility = View.GONE
                            defenseStartTimeTextView.text = "Не начата"

                            Toast.makeText(this@ProjectDetailsActivity, "Защита отменена", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ProjectDetailsActivity, "Ошибка при отмене защиты", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при отмене защиты", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Нет", null)
            .show()
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

    private fun showSavedAudioFilesDialog() {
        if (savedAudioFiles.isEmpty()) {
            Toast.makeText(this, "Нет сохраненных аудиозаписей для этого проекта", Toast.LENGTH_SHORT).show()
            return
        }

        val fileNames = savedAudioFiles.map {
            val file = File(it)
            val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(Date(file.lastModified()))
            "Запись от $date (${file.length() / 1024} KB)"
        }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Аудиозаписи проекта \"${project.Title}\"")
            .setItems(fileNames) { _, which ->
                val selectedFile = savedAudioFiles[which]
                uploadAudioFile(selectedFile)
            }
            .setNegativeButton("Отмена", null)
            .show()
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
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при получении вопросов", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Question>>, t: Throwable) {
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
            Toast.makeText(this, "Запись началась", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
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
                showStartRecordingDialog()
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
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            handler.removeCallbacks(recordingRunnable)
            micButton.setImageResource(R.drawable.ic_mic_off)
            Toast.makeText(this, "Запись остановлена", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при остановке записи", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateRecordingTime() {
        recordingRunnable = Runnable {
            if (isRecording) {
                recordingTime++
                val minutes = recordingTime / 60
                val seconds = recordingTime % 60
                recordingTimeTextView.text = String.format("%02d:%02d", minutes, seconds)
                handler.postDelayed(recordingRunnable, 1000)
            }
        }
        handler.post(recordingRunnable)
    }

    private fun uploadAudioFile(filePath: String, saveOnError: Boolean = false) {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
            return
        }

        val requestFile = RequestBody.create("audio/3gp".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("audio", file.name, requestFile)

        val projectId = project.ID
        val projectIdRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), projectId.toString())

        apiService.uploadAudio(body, projectIdRequestBody).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProjectDetailsActivity, "Аудио успешно отправлено", Toast.LENGTH_SHORT).show()
                    if (filePath == audioFilePath) {
                        try {
                            File(filePath).delete()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error deleting temp file: ${e.message}")
                        }
                    }
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при отправке аудио", Toast.LENGTH_SHORT).show()
                    if (saveOnError && filePath == audioFilePath) {
                        val savedFilePath = saveAudioFileToDevice()
                        if (savedFilePath != null) {
                            savedAudioFiles.add(savedFilePath)
                            saveSavedAudioFilesList()
                            savedAudioInfoTextView.visibility = View.VISIBLE
                            savedAudioInfoTextView.text = "Сохранено аудиозаписей: ${savedAudioFiles.size}"

                            Toast.makeText(this@ProjectDetailsActivity,
                                "Аудио сохранено локально из-за ошибки отправки",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                Log.e("Upload Error", t.message ?: "Неизвестная ошибка")
                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при отправке аудио", Toast.LENGTH_SHORT).show()
                if (saveOnError && filePath == audioFilePath) {
                    val savedFilePath = saveAudioFileToDevice()
                    if (savedFilePath != null) {
                        savedAudioFiles.add(savedFilePath)
                        saveSavedAudioFilesList()

                        savedAudioInfoTextView.visibility = View.VISIBLE
                        savedAudioInfoTextView.text = "Сохранено аудиозаписей: ${savedAudioFiles.size}"

                        Toast.makeText(this@ProjectDetailsActivity,
                            "Аудио сохранено локально из-за ошибки сети",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun updateQuestionOnServer(questionId: Int, newText: String) {
        val updateRequest = QuestionUpdateRequest(newText)
        apiService.updateQuestion(questionId, updateRequest).enqueue(object : Callback<Question> {
            override fun onResponse(call: Call<Question>, response: Response<Question>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProjectDetailsActivity, "Вопрос успешно обновлен", Toast.LENGTH_SHORT).show()
                    sendQuestionsRequest(project.ID)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при обновлении вопроса: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Question>, t: Throwable) {
                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при обновлении вопроса: ${t.message}", Toast.LENGTH_SHORT).show()
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
                val questionRequest = QuestionRequest(questionText, projectId)
                addQuestion(questionRequest)
            }.setNegativeButton("Отмена", null).create()
        dialog.show()
    }

    private fun addQuestion(questionRequest: QuestionRequest) {
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

    private fun showChangeTimeDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = android.app.TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d:00", selectedHour, selectedMinute)

                MaterialAlertDialogBuilder(this)
                    .setTitle("Изменить время начала")
                    .setMessage("Вы уверены, что хотите изменить время начала защиты на $selectedTime?")
                    .setPositiveButton("Да") { _, _ ->
                        val projectTimeRequest = ProjectTimeRequest(ID_Project = project.ID, DefenseStartTime = selectedTime)
                        apiService.setProjectStartTime(projectTimeRequest).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    defenseStartTimeTextView.text = selectedTime
                                    Toast.makeText(this@ProjectDetailsActivity,
                                        "Время начала защиты изменено на $selectedTime",
                                        Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@ProjectDetailsActivity,
                                        "Ошибка при изменении времени начала защиты: ${response.code()}",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(this@ProjectDetailsActivity,
                                    "Ошибка сети при изменении времени начала защиты: ${t.message}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    override fun onResume() {
        super.onResume()
        try {
            if (::project.isInitialized) {
                loadSavedAudioFiles()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume: ${e.message}")
        }
    }
}
