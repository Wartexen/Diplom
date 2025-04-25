package com.example.myapplication.Models.Auth

// Модель для ответа от Bitrix
data class BitrixAuthResponse(
    val user: BitrixUser,
    val access_token: String
)