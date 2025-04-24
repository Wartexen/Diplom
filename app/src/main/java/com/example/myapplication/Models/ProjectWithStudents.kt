package com.example.myapplication.Models

data class ProjectWithStudents(
    val projectTitle: String,
    val students: MutableList<StudentGrade> = mutableListOf()
)
