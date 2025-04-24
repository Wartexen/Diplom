package com.example.myapplication.Models

data class StudentGrade(
    val id: Int,
    val name: String,
    val projectTitle: String,
    var grade: Int = 0,
    val groupName: String = ""
)
