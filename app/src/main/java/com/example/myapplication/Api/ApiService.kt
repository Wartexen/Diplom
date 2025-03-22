package com.example.myapplication.Api
import com.example.myapplication.Models.Commission
import com.example.myapplication.Models.CommissionScheduleRequest
import com.example.myapplication.Models.DefenseSchedule
import com.example.myapplication.Models.Project
import com.example.myapplication.Models.Protocol // ИЗМЕНИТЬ ПОТОМ
import com.example.myapplication.Models.Question
import com.example.myapplication.Models.SecretaryIdResponse
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
    @GET("audio/specializations/")
    fun getSpecializations(): Call<List<Specialization>>
    @GET("audio/commissions/")
    fun getCommissions(): Call<List<Commission>>
    @GET("audio/projects/")
    fun getProjects(): Call<List<Project>>
    @GET("audio/project_list_by_defense_schedule_id/")
    fun getProjectsBydefense_schedule_id(@Query("defense_schedule_id") defense_schedule_id: Int): Call<List<Project>>
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

    @POST("audio/authorize/")
    fun getSecretaryId(@Body requestBody: Map<String, String>): Call<SecretaryIdResponse>


    @POST("audio/authenticate_user/")
    fun authenticateUser(@Body requestBody: Map<String, String>): Call<SecretaryResponse>
    @PUT("audio/update_question/")
    fun updateQuestion(
        @Query("question_id") questionId: Int,
        @Query("text") text: String
    ): Call<Void>

    @DELETE("/audio/delete_question/")
    fun deleteQuestion(
        @Query("question_id") questionId: Int
    ): Call<Void>

    @POST("audio/update_grade/")
    fun updateGrade(
        @Query("student_id ") questionId: Int,
        @Body Text: Text // IZMENTь!!!
    ): Call<Void>

    @GET("audio/specializations_by_secretary/")
    fun getSpecializationsBySecretary(@Query("secretary_id") secretaryId: Int): Call<List<Specialization>>

    @GET("audio/commission_list_by_member/")
    fun getCommissionsBySecretary(@Query("secretary_id") secretaryId: Int): Call<List<Commission>>

    @GET("audio/get_today_defenses_by_specialization/")
    fun getTodayDefensesBySpecialization(@Query("specialization_id") commissionId: Int, @Query("today") date: String): Call<List<DefenseSchedule>>

}
