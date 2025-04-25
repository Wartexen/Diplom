package com.example.myapplication.Models

data class StudentGrade(
    val id: Int,
    val name: String,
    val projectTitle: String,
    var grade: String = "",
    val groupName: String = ""
)
