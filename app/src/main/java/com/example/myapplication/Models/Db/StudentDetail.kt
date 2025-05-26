package com.example.myapplication.Models.Db

data class StudentDetail(
    val ID: Int,
    val ID_Group: Group,
    val grade: String?,
    val Surname: String,
    val Name: String,
    val Patronymic: String?,
    val ID_Specialization: Int,
    val ID_Project: Int
)