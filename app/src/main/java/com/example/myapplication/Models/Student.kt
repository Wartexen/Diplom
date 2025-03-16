package com.example.myapplication.Models

import android.os.Parcelable


data class Student(
    val ID: Int,
    val Surname: String,
    val Name: String,
    val Patronymic: String,
    val ID_Group: Int,
    val ID_Project_id: Int
)

