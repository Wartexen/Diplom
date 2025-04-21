package com.example.myapplication.Models
data class BitrixUser(
    val id: Int,
    val email: String,
    val full_name: String,
    val is_teacher: Boolean,
    val is_student: Boolean
)