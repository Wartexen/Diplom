package com.example.myapplication.Models.Requests

data class ProjectStatusUpdateRequest(
    val project_id: Int,
    val status: Boolean
)
