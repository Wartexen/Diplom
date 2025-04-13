package com.example.myapplication.Api
import com.example.myapplication.Models.BitrixAuthRequest
import com.example.myapplication.Models.BitrixAuthResponse
import com.example.myapplication.Models.Commission
import com.example.myapplication.Models.CommissionScheduleRequest
import com.example.myapplication.Models.DefenseSchedule
import com.example.myapplication.Models.Project
import com.example.myapplication.Models.Protocol // ИЗМЕНИТЬ ПОТОМ
import com.example.myapplication.Models.Question
import com.example.myapplication.Models.SecretaryIdResponse
import com.example.myapplication.Models.SecretaryRequest
import com.example.myapplication.Models.SecretaryResponse
import com.example.myapplication.Models.Specialization
import com.example.myapplication.Models.Student
import com.example.myapplication.Models.UpdateQuestionRequest
import com.example.myapplication.Models.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/projects/by_defense_schedule/")//+
    fun getProjectsBydefense_schedule_id(@Query("defense_schedule_id") defense_schedule_id: Int): Call<List<Project>>
    @POST("api/defenses/add_commission/")//+
    fun addCommissionToSchedule(@Body request: CommissionScheduleRequest): Call<Void>
    @GET("api/projects/students/")//+
    fun getStudentsByProject(@Query("project_id") projectId: Int): Call<List<Student>>
    @Multipart
    @POST("api/upload-audio/")//+
    fun uploadAudio(
        @Part audio: MultipartBody.Part,
        @Part("project_id") projectId: RequestBody
    ): Call<UploadResponse>

    @GET("api/questions/by_project/")//+
    fun getQuestionsByProject(@Query("project_id") projectId: Int): Call<List<Question>>

    @POST("api/users/authorize_member/")//+
    fun getSecretaryId(@Body requestBody: Map<String, String>): Call<SecretaryIdResponse>


    @POST("api/users/login/")//+
    fun authenticateUser(@Body requestBody: Map<String, String>): Call<SecretaryResponse>

    @PUT("api/questions/update_question/")//+
    fun updateQuestion(
        @Query("question_id") questionId: Int,
        @Query("text") text: String
    ): Call<Void>

    @DELETE("api/questions/delete/")//+
    fun deleteQuestion(
        @Query("question_id") questionId: Int
    ): Call<Void>

    @GET("api/users/specializations/")//+
    fun getSpecializationsBySecretary(@Query("secretary_id") secretaryId: Int): Call<List<Specialization>>

    @GET("api/users/commissions/")//+
    fun getCommissionsBySecretary(@Query("secretary_id") secretaryId: Int): Call<List<Commission>>

    @GET("api/defenses/today/")//+
    fun getTodayDefensesBySpecialization(@Query("specialization_id") commissionId: Int, @Query("date") date: String): Call<List<DefenseSchedule>>

}
