package com.example.myapplication.Models.Response

import com.example.myapplication.Models.Commission

data class CommissionResponse(
    val ID: Int,
    val ID_Commission: Commission,
    val Role: String,
    val Id_member: Int
)

