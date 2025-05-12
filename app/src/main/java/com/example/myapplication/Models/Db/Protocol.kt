package com.example.myapplication.Models.Db

data class Protocol(
    val ID: Int,
    val Year: Int,
    val Grade: String?,
    val DefenseStartTime: String?,
    val DefenseEndTime: String?,
    val Number: String?,
    val ID_Question: Int,
    val ID_Student: Int,
    val ID_DefenseSchedule: Int
)