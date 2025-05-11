package com.example.myapplication.Models.Response

import com.example.myapplication.Models.Db.Specialization

data class SecretarySpecializationResponse(
    val ID: Int,
    val ID_Specialization: Specialization?,
)