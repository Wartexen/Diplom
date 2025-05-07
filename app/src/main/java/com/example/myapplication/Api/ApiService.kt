package com.example.myapplication.Api
import com.example.myapplication.Models.Commission
import com.example.myapplication.Models.Requests.CommissionScheduleRequest
import com.example.myapplication.Models.DefenseSchedule
import com.example.myapplication.Models.Requests.GradeRequest
import com.example.myapplication.Models.Requests.GradeResponse
import com.example.myapplication.Models.Project
import com.example.myapplication.Models.Requests.ProjectStatusResponse
import com.example.myapplication.Models.Question
import com.example.myapplication.Models.Requests.QuestionRequest
import com.example.myapplication.Models.Auth.SecretaryIdResponse
import com.example.myapplication.Models.Auth.SecretaryResponse
import com.example.myapplication.Models.Requests.ProjectStatusUpdateRequest
import com.example.myapplication.Models.Requests.ProjectTimeRequest
import com.example.myapplication.Models.Response.SecretarySpecializationResponse
import com.example.myapplication.Models.Student
import com.example.myapplication.Models.Requests.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @GET("/api/projects/by_defense_schedule/")
    fun getProjectsBydefense_schedule_id(@Query("defense_schedule_id") defense_schedule_id: Int): Call<List<Project>>

    @POST("api/defenses/add_commission/")
    fun addCommissionToSchedule(@Body request: CommissionScheduleRequest): Call<Void>

    @GET("api/projects/students/")
    fun getStudentsByProject(@Query("project_id") projectId: Int): Call<List<Student>>

    @Multipart
    @POST("api/upload-audio/")
    fun uploadAudio(
        @Part audio: MultipartBody.Part,
        @Part("project_id") projectId: RequestBody
    ): Call<UploadResponse>

    @GET("api/questions/by_project/")
    fun getQuestionsByProject(@Query("project_id") projectId: Int): Call<List<Question>>


    @GET("/api/secretary/")//+
    fun getSecretaryId(
        @Query("name") name: String,
        @Query("patronymic") patronymic: String,
        @Query("surname") surname: String
    ): Call<SecretaryIdResponse>

    @POST("api/users/login/")
    fun authenticateUser(@Body requestBody: Map<String, String>): Call<SecretaryResponse>

    @POST("api/questions/create_question/")
    fun createQuestion(@Body request: QuestionRequest): Call<Question>

    @PUT("api/questions/update_question/")
    fun updateQuestion(
        @Query("question_id") questionId: Int,
        @Query("text") text: String
    ): Call<Void>

    @DELETE("api/questions/delete/")
    fun deleteQuestion(
        @Query("question_id") questionId: Int
    ): Call<Void>

    @GET("/api/secretary_specialization/")//+
    fun getSecretarySpecializations(
        @Query("ID_Secretary") secretaryId: Int
    ): Call<List<SecretarySpecializationResponse>>

    @GET("api/users/commissions/")
    fun getCommissionsBySecretary(@Query("secretary_id") secretaryId: Int): Call<List<Commission>>

    @GET("api/defenses/today/")
    fun getTodayDefensesBySpecialization(@Query("specialization_id") commissionId: Int, @Query("date") date: String): Call<List<DefenseSchedule>>

    @GET("api/projects/by_defense_schedule/")
    fun getProjectsByDefenseSchedule(@Query("defense_schedule_id") defenseScheduleId: Int): Call<List<Project>>

    @POST("api/projects/grade/")
    fun gradeStudent(@Body gradeRequest: GradeRequest): Call<GradeResponse>

    @GET("api/projects/project_status/")
    fun getProjectStatus(@Query("project_id") projectId: Int): Call<ProjectStatusResponse>

    @POST("api/projects/project_time/")
    fun setProjectTime(@Body request: ProjectTimeRequest): Call<Void>

    @POST("api/projects/update_status/")
    fun updateProjectStatus(@Body request: ProjectStatusUpdateRequest): Call<ProjectStatusResponse>
}
