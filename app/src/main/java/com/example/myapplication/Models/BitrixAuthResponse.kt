package com.example.myapplication.Models

// Модель для ответа от Bitrix
data class BitrixAuthResponse(
    val user: BitrixUser,
    val access_token: String
)