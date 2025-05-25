package com.example.myapplication.Models.Db
data class CommissionMember(
    val ID: Int,
    val ID_Member: Member,
    val Role: String,
    val ID_Commission: Int
)
