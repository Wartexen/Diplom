package com.example.myapplication.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Api.ApiService
import com.example.myapplication.Models.Db.ProjectWithStudents
import com.example.myapplication.Models.Db.StudentGrade
import com.example.myapplication.Models.Requests.GradeRequest
import com.example.myapplication.Models.Response.GradeResponse
import com.example.myapplication.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentGradeAdapter(
    private val projects: List<ProjectWithStudents>,
    private val apiService: ApiService
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_PROJECT = 0
        private const val VIEW_TYPE_STUDENT = 1
    }

    private val items = mutableListOf<Any>()

    init {
        updateItems()
    }

    // ViewHolder для заголовка проекта
    class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectTitle: TextView = view.findViewById(R.id.tvProjectTitle)
        val expandIcon: ImageView = view.findViewById(R.id.expandIcon)
    }

    // ViewHolder для студента
    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentName: TextView = view.findViewById(R.id.StudentName)
        val groupName: TextView = view.findViewById(R.id.StudentGroup)
        val gradeSpinner: Spinner = view.findViewById(R.id.spinnerGrade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PROJECT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_project_header, parent, false)
                ProjectViewHolder(view)
            }
            VIEW_TYPE_STUDENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_student_grade, parent, false)
                StudentViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProjectViewHolder -> bindProject(holder, position)
            is StudentViewHolder -> bindStudent(holder, position)
        }
    }

    private fun bindProject(holder: ProjectViewHolder, position: Int) {
        val project = items[position] as ProjectWithStudents
        holder.projectTitle.text = project.projectTitle

        // Устанавливаем иконку в зависимости от состояния
        val iconRes = if (project.isExpanded) {
            R.drawable.ic_expand_less
        } else {
            R.drawable.ic_expand_more
        }
        holder.expandIcon.setImageResource(iconRes)

        // Обработка клика по заголовку проекта
        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                val projectItem = items[adapterPosition] as ProjectWithStudents
                projectItem.isExpanded = !projectItem.isExpanded
                updateItems()

                if (projectItem.isExpanded) {
                    notifyItemRangeInserted(adapterPosition + 1, projectItem.students.size)
                } else {
                    notifyItemRangeRemoved(adapterPosition + 1, projectItem.students.size)
                }
                notifyItemChanged(adapterPosition)
            }
        }
    }

    private fun bindStudent(holder: StudentViewHolder, position: Int) {
        val student = items[position] as StudentGrade
        holder.studentName.text = student.name
        holder.groupName.text = student.groupName

        // Настройка Spinner с оценками
        val grades = listOf(
            "Выберите оценку",
            "Отлично",
            "Хорошо",
            "Удовлетворительно",
            "Неудовлетворительно",
            "Пересдача"
        )

        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            grades
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        holder.gradeSpinner.adapter = adapter

        // Установка текущей оценки
        val selectedPosition = grades.indexOfFirst { it == student.grade }.coerceAtLeast(0)
        holder.gradeSpinner.setSelection(selectedPosition)

        // Обработка выбора оценки
        holder.gradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (pos > 0) {
                    student.grade = grades[pos]
                    submitGrade(student, holder.itemView.context)
                } else {
                    student.grade = ""
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun submitGrade(student: StudentGrade, context: Context) {
        val request = GradeRequest(ID_Student = student.id, Grade = student.grade)
        apiService.gradeStudent(request).enqueue(object : Callback<GradeResponse> {
            override fun onResponse(call: Call<GradeResponse>, response: Response<GradeResponse>) {
                if (!response.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Ошибка сохранения оценки: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<GradeResponse>, t: Throwable) {
                Toast.makeText(
                    context,
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProjectWithStudents -> VIEW_TYPE_PROJECT
            is StudentGrade -> VIEW_TYPE_STUDENT
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    private fun updateItems() {
        items.clear()
        projects.forEach { project ->
            items.add(project)
            if (project.isExpanded) {
                items.addAll(project.students)
            }
        }
    }

    fun areAllStudentsGraded(): Boolean {
        return items.filterIsInstance<StudentGrade>()
            .all { it.grade.isNotEmpty() && it.grade != "Выберите оценку" }
    }

    fun getGrades(): List<StudentGrade> {
        return items.filterIsInstance<StudentGrade>()
    }
}
