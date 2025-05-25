package com.example.myapplication.Models.Requests
data class BitrixAuthRequest(
    val code: String,
    val clientId: String,
    val clientSecret: String, val redirectUri: String
)
