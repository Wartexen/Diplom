package com.example.myapplication.Api
import com.example.myapplication.Models.Requests.CommissionScheduleRequest
import com.example.myapplication.Models.Db.DefenseSchedule
import com.example.myapplication.Models.Requests.GradeRequest
import com.example.myapplication.Models.Response.GradeResponse
import com.example.myapplication.Models.Db.Project
import com.example.myapplication.Models.Db.Question
import com.example.myapplication.Models.Requests.QuestionRequest
import com.example.myapplication.Models.Auth.SecretaryIdResponse
import com.example.myapplication.Models.Db.Protocol
import com.example.myapplication.Models.Requests.ProjectTimeRequest
import com.example.myapplication.Models.Requests.QuestionUpdateRequest
import com.example.myapplication.Models.Response.CommissionResponse
import com.example.myapplication.Models.Response.SecretarySpecializationResponse
import com.example.myapplication.Models.Db.Student
import com.example.myapplication.Models.Requests.ProjectTimeEndRequest
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
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @PATCH("api/defenses/{ID}/")
    fun updateDefenseSchedule(
        @Path("ID") id: Int,
        @Body request: CommissionScheduleRequest
    ): Call<DefenseSchedule>

    @PATCH("api/questions/{questionId}/")
    fun updateQuestion(
        @Path("questionId") questionId: Int,
        @Body request: QuestionUpdateRequest
    ): Call<Question>

    @PATCH("api/students/update_grade/")
    fun gradeStudent(@Body gradeRequest: GradeRequest): Call<GradeResponse>

    @PATCH("api/projects/project_time_start/")
    fun setProjectStartTime(@Body request: ProjectTimeRequest): Call<Void>

    @PATCH("api/projects/project_time_end/")
    fun setProjectEndTime(@Body request: ProjectTimeEndRequest): Call<Void>




    @GET("api/secretary_specialization/")
    fun getSecretarySpecializations(
        @Query("ID_Secretary") secretaryId: Int
    ): Call<List<SecretarySpecializationResponse>>

    @GET("api/students/")
    fun getStudentsByProject(@Query("ID_Project") projectId: Int): Call<List<Student>>

    @GET("api/questions/")
    fun getQuestionsByProject(@Query("ID_Project") projectId: Int): Call<List<Question>>

    @GET("api/projects/")
    fun getProjectsByDefenseSchedule(@Query("defense_schedule_id") defenseScheduleId: Int): Call<List<Project>>

    @GET("api/projects/{projectId}/")
    fun getProjectStatus(@Path("projectId") projectId: Int): Call<Project>

    @GET("api/defenses/")
    fun getDefensesBySpecialization(
        @Query("specialization_id") specializationId: Int
    ): Call<List<DefenseSchedule>>

    @GET("api/commissions/")
    fun getCommissionsBySecretary(
        @Query("ID_Member") secretaryId: Int,
        @Query("Role") role: String
    ): Call<List<CommissionResponse>>

    @GET("api/secretary/")
    fun getSecretaryId(
        @Query("Name") name: String,
        @Query("Patronymic") patronymic: String,
        @Query("Surname") surname: String
    ): Call<List<SecretaryIdResponse>>

    @GET("api/protocols/")
    fun getProtocolsByStudentId(@Query("ID_Student") studentId: Int): Call<List<Protocol>>


    @Multipart
    @POST("api/upload-audio/")
    fun uploadAudio(
        @Part audio: MultipartBody.Part,
        @Part("project_id") projectId: RequestBody
    ): Call<UploadResponse>

    @POST("api/questions/")
    fun createQuestion(@Body request: QuestionRequest): Call<Question>


    @DELETE("api/questions/{questionId}/")
    fun deleteQuestion(@Path("questionId") questionId: Int): Call<Void>
}
