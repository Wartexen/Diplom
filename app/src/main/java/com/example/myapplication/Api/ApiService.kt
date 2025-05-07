package com.example.myapplication.Api
import com.example.myapplication.Models.Requests.CommissionScheduleRequest
import com.example.myapplication.Models.DefenseSchedule
import com.example.myapplication.Models.Requests.GradeRequest
import com.example.myapplication.Models.Response.GradeResponse
import com.example.myapplication.Models.Project
import com.example.myapplication.Models.Response.ProjectStatusResponse
import com.example.myapplication.Models.Question
import com.example.myapplication.Models.Requests.QuestionRequest
import com.example.myapplication.Models.Auth.SecretaryIdResponse
import com.example.myapplication.Models.Requests.ProjectStatusUpdateRequest
import com.example.myapplication.Models.Requests.ProjectTimeRequest
import com.example.myapplication.Models.Requests.QuestionUpdateRequest
import com.example.myapplication.Models.Response.CommissionResponse
import com.example.myapplication.Models.Response.SecretarySpecializationResponse
import com.example.myapplication.Models.Student
import com.example.myapplication.Models.Response.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/projects/by_defense_schedule/")
    fun getProjectsBydefense_schedule_id(@Query("defense_schedule_id") defense_schedule_id: Int): Call<List<Project>>
    @PATCH("api/defenses/{ID}/")//++
    fun updateDefenseSchedule(
        @Path("ID") id: Int,
        @Body request: CommissionScheduleRequest
    ): Call<DefenseSchedule>

    @GET("api/students/")//+
    fun getStudentsByProject(@Query("ID_Project") projectId: Int): Call<List<Student>>

    @Multipart
    @POST("api/upload-audio/")
    fun uploadAudio(
        @Part audio: MultipartBody.Part,
        @Part("project_id") projectId: RequestBody
    ): Call<UploadResponse>

    @GET("api/questions/")//+
    fun getQuestionsByProject(@Query("ID_Project") projectId: Int): Call<List<Question>>


    @GET("/api/secretary/")//+
    fun getSecretaryId(
        @Query("name") name: String,
        @Query("patronymic") patronymic: String,
        @Query("surname") surname: String
    ): Call<SecretaryIdResponse>

    @POST("api/questions/")//++
    fun createQuestion(@Body request: QuestionRequest): Call<Question>

    @PATCH("api/questions/{questionId}/")//+
    fun updateQuestion(
        @Path("questionId") questionId: Int,
        @Body request: QuestionUpdateRequest
    ): Call<Question>

    @DELETE("api/questions/{questionId}/")//+
    fun deleteQuestion(@Path("questionId") questionId: Int): Call<Void>

    @GET("/api/secretary_specialization/")//+
    fun getSecretarySpecializations(
        @Query("ID_Secretary") secretaryId: Int
    ): Call<List<SecretarySpecializationResponse>>

    @GET("api/commissions/")//+
    fun getCommissionsBySecretary(
        @Query("ID_Member") secretaryId: Int,
        @Query("Role") role: String
    ): Call<List<CommissionResponse>>

    @GET("api/defenses/")//+
    fun getDefensesBySpecialization(
        @Query("specialization_id") specializationId: Int
    ): Call<List<DefenseSchedule>>

    @GET("api/projects/")//+
    fun getProjectsByDefenseSchedule(@Query("defense_schedule_id") defenseScheduleId: Int): Call<List<Project>>

    @POST("api/projects/grade/")
    fun gradeStudent(@Body gradeRequest: GradeRequest): Call<GradeResponse>
    @GET("api/projects/{projectId}/")//+
    fun getProjectStatus(@Path("projectId") projectId: Int): Call<Project>


    @POST("api/projects/project_time/")
    fun setProjectTime(@Body request: ProjectTimeRequest): Call<Void>

    @POST("api/projects/update_status/")
    fun updateProjectStatus(@Body request: ProjectStatusUpdateRequest): Call<ProjectStatusResponse>
}
