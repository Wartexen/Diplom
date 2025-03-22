package com.example.myapplication
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Handler
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
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
import com.example.myapplication.Models.UpdateQuestionRequest
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
/*

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
    private var audioFilePath: String = ""
    private var recordingTime: Int = 0 // Время записи в секундах
    private lateinit var recordingRunnable: Runnable
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        micButton = findViewById(R.id.micButton)

        recordingTimeTextView = findViewById(R.id.recordingTime)
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView)
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView)

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        project = intent.getParcelableExtra<Project>("project") ?: return


        findViewById<TextView>(R.id.projectNameTextView).text = project.Title
        findViewById<TextView>(R.id.projectLeaderTextView).text = project.Supervisor

        val projectIdString = project.ID
        val projectId = projectIdString.toIntOrNull()

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

        // Обработчик нажатия на кнопку questions
       */
/* questions.setOnClickListener {
            if (projectId != null) {
                sendQuestionsRequest(projectId)
            } else {
                Toast.makeText(this, "Неверный ID проекта", Toast.LENGTH_SHORT).show()
            *}
        }*//*


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
                    val questionAdapter = QuestionAdapter(questions.toMutableList())

                    // Колбэк для удаления
                    questionAdapter.setOnQuestionDeleteListener { question ->
                        deleteQuestionOnServer(question)
                    }

                    questionAdapter.setOnQuestionSaveListener { question, newText ->
                        Log.d("PROJECT_DETAILS", "Callback triggered in activity")
                        // Здесь вызываем API для обновления вопроса
                        updateQuestionOnServer(question.ID, newText)
                    }
                    questionsRecyclerView.layoutManager = LinearLayoutManager(this@ProjectDetailsActivity)
                    questionsRecyclerView.adapter = questionAdapter

                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Log.e("API Error", errorMessage)
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

    */
/*private fun updateQuestionOnServer(questionId: Int, newText: String) {
        Log.d("PROJECT_DETAILS", "Updating question with ID: $questionId, New Text: $newText")
        val request = UpdateQuestionRequest(newText) // Создаем объект запроса
        apiService.updateQuestion(questionId, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("PROJECT_DETAILS", "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    Log.d("PROJECT_DETAILS", "Question updated successfully")
                    Toast.makeText(this@ProjectDetailsActivity, "Вопрос успешно обновлен", Toast.LENGTH_SHORT).show()


                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при обновлении вопроса: $errorMessage", Toast.LENGTH_SHORT).show()
                    Log.e("API Error", "Failed to update question: $errorMessage")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при обновлении вопроса", Toast.LENGTH_SHORT).show()
                Log.e("API Error", "Network error during question update: ${t.message}")
            }
        })
    }*//*

    private fun updateQuestionOnServer(questionId: Int, newText: String) {
        Log.d("PROJECT_DETAILS", "Updating question with ID: $questionId, New Text: $newText")
        apiService.updateQuestion(questionId, newText).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("PROJECT_DETAILS", "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    Log.d("PROJECT_DETAILS", "Question updated successfully")
                    Toast.makeText(this@ProjectDetailsActivity, "Вопрос успешно обновлен", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при обновлении вопроса: $errorMessage", Toast.LENGTH_SHORT).show()
                    Log.e("API Error", "Failed to update question: $errorMessage")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при обновлении вопроса", Toast.LENGTH_SHORT).show()
                Log.e("API Error", "Network error during question update: ${t.message}")
            }
        })
    }

    private fun deleteQuestionOnServer(questionId: Int) {
        Log.d("PROJECT_DETAILS", "Updating question with ID: $questionId")

        apiService.deleteQuestion(questionId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProjectDetailsActivity, "Вопрос удалён", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProjectDetailsActivity, "Сетевая ошибка", Toast.LENGTH_SHORT).show()
            }
        })
    }



}
*/
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
    private var audioFilePath: String = ""
    private var recordingTime: Int = 0 // Время записи в секундах
    private lateinit var recordingRunnable: Runnable
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private lateinit var questionAdapter: QuestionAdapter // Добавлено для адаптера вопросов

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        micButton = findViewById(R.id.micButton)
        recordingTimeTextView = findViewById(R.id.recordingTime)
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView)
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView)

        // Инициализация Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        project = intent.getParcelableExtra<Project>("project") ?: return

        findViewById<TextView>(R.id.projectNameTextView).text = project.Title
        findViewById<TextView>(R.id.projectLeaderTextView).text = project.Supervisor

        val projectIdString = project.ID
        val projectId = projectIdString.toIntOrNull()

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

                    // Колбэк для удаления
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
                    Log.e("API Error", errorMessage)
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
        Log.d("PROJECT_DETAILS", "Updating question with ID: $questionId, New Text: $newText")
        apiService.updateQuestion(questionId, newText).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("PROJECT_DETAILS", "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    Log.d("PROJECT_DETAILS", "Question updated successfully")
                    Toast.makeText(this@ProjectDetailsActivity, "Вопрос успешно обновлен", Toast.LENGTH_SHORT).show()
                    // Обновляем список вопросов
                    sendQuestionsRequest(project.ID.toInt())
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка при обновлении вопроса: $errorMessage", Toast.LENGTH_SHORT).show()
                    Log.e("API Error", "Failed to update question: $errorMessage")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProjectDetailsActivity, "Ошибка сети при обновлении вопроса", Toast.LENGTH_SHORT).show()
                Log.e("API Error", "Network error during question update: ${t.message}")
            }
        })
    }

    private fun deleteQuestionOnServer(questionId: Int) {
        Log.d("PROJECT_DETAILS", "Deleting question with ID: $questionId")

        apiService.deleteQuestion(questionId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProjectDetailsActivity, "Вопрос удалён", Toast.LENGTH_SHORT).show()
                    // Обновляем список вопросов
                    sendQuestionsRequest(project.ID.toInt())
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@ProjectDetailsActivity, "Сетевая ошибка", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
