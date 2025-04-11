package com.example.myapplication.Models

data class Protocol(
    val ID: Int,
    val Year: Int,
    val Grade: Int,
    val Id_question: Int,
    val Id_defenseschedule: Int = 2,
    val Id_student: Int
)