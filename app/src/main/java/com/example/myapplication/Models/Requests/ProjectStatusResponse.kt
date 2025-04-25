package com.example.myapplication.Models.Requests

data class ProjectStatusResponse(
    val project_id: Int,
    val title: String,
    val status: Boolean
)
