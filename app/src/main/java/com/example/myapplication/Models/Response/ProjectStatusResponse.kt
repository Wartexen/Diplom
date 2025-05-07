package com.example.myapplication.Models.Response

data class ProjectStatusResponse(
    val project_id: Int,
    val title: String,
    val status: Boolean
)
