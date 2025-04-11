package com.example.myapplication.Api

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val status: String, val message: String)