package com.example.myapplication.Api
import com.example.myapplication.Models.Commission
import com.example.myapplication.Models.CommissionScheduleRequest
import com.example.myapplication.Models.DefenseSchedule
import com.example.myapplication.Models.Project
import com.example.myapplication.Models.Protocol // ИЗМЕНИТЬ ПОТОМ
import com.example.myapplication.Models.Question
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
    @GET("audio/specializations/")
    fun getSpecializations(): Call<List<Specialization>>
    @GET("audio/commissions/")
    fun getCommissions(): Call<List<Commission>>
    @GET("audio/projects/")
    fun getProjects(): Call<List<Project>>
    @GET("audio/defense_schedules/")
    fun getDefenseSchedules(): Call<List<DefenseSchedule>>
    @POST("audio/defense_schedules/addComission/")
    fun addCommissionToSchedule(@Body request: CommissionScheduleRequest): Call<Void>
    @GET("audio/students_by_project/")
    fun getStudentsByProject(@Query("project_id") projectId: Int): Call<List<Student>>
    @Multipart
    @POST("audio/upload/")
    fun uploadAudio(
        @Part audio: MultipartBody.Part,
        @Part("project_id") projectId: RequestBody
    ): Call<UploadResponse>

    @GET("audio/questions_by_project/")
    fun getQuestionsByProject(@Query("project_id") projectId: Int): Call<List<Question>>
//
//    @PUT("projects/questions/{id}/")
//    fun updateQuestion(
//        @Path("id") questionId: Int,
//        @Body Text: UpdateQuestionRequest
//    ): Call<Void>

    @PUT("audio/projects/update_question/{id}/")
    fun updateQuestion(
        questionId: Int,
        @Body request: UpdateQuestionRequest
    ): Call<Void>

    @DELETE("projects/audio/delete_question/")
    fun deleteQuestion(
        @Query("question_id") questionId: Int
    ): Call<Void>

    @POST("audio/update_grade/")
    fun updateGrade(
        @Query("student_id ") questionId: Int,
        @Body Text: Text // IZMENTь!!!
    ): Call<Void>

    @POST("android_login/")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    abstract fun createProtocol(protocol: Protocol): Any // УРАТЬ ПОТОМ

}
