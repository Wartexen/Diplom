package com.example.myapplication.Models.Requests

data class ProjectTimeEndRequest(
    val ID_Project: Int,
    val DefenseEndTime: String? = null
)
