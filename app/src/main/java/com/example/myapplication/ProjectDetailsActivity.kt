package com.example.myapplication
import android.Manifest
import android.os.Handler
import android.os.Bundle
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity




    class ProjectDetailsActivity : AppCompatActivity() {

        private lateinit var micButton: ImageView
        private var isRecording = false
        private val handler = Handler(Looper.getMainLooper())

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_project_details)

            micButton = findViewById(R.id.micButton)


            val project = intent.getParcelableExtra<Project>("project") ?: return

            findViewById<TextView>(R.id.projectNameTextView).text = project.name
            findViewById<TextView>(R.id.projectLeaderTextView).text = project.leader
            findViewById<TextView>(R.id.projectDirectionTextView).text = project.direction
            findViewById<TextView>(R.id.projectStudentsTextView).text = project.students//.joinToString(", ")


            micButton.setOnClickListener {
                if (isRecording) {
                    stopRecording()
                } else {
                    startRecording()
                }
            }
        }


        private fun startRecording() {
            isRecording = true
            handler.post {
                micButton.setImageResource(R.drawable.ic_mic_on)
                // логика
            }

        }


        private fun stopRecording() {
            isRecording = false
            handler.post {
                micButton.setImageResource(R.drawable.ic_mic_off)
            }
            // логикаа
        }
    }