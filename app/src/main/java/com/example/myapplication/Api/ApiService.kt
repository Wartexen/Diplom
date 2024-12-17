package com.example.myapplication.Api
import com.example.myapplication.Models.Commission
import com.example.myapplication.Models.DefenseSchedule
import com.example.myapplication.Models.Institute
import com.example.myapplication.Models.Specialization
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("audio/specializations/")
    fun getSpecializations(): Call<List<Specialization>>
    @GET("audio/commissions/")
    fun getCommissions(): Call<List<Commission>>
    @GET("audio/defense_schedules/")
    fun getDefenseSchedules(): Call<List<DefenseSchedule>>
}
