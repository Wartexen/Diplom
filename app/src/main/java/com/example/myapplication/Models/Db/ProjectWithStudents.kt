package com.example.myapplication.Models.Db

data class ProjectWithStudents(
    val projectTitle: String,
    val students: MutableList<StudentGrade> = mutableListOf()
)
