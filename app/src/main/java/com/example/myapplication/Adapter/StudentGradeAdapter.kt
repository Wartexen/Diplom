package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.ProjectWithStudents
import com.example.myapplication.Models.StudentGrade
import com.example.myapplication.R

class StudentGradeAdapter(private val projects: List<ProjectWithStudents>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_PROJECT_HEADER = 0
        private const val VIEW_TYPE_STUDENT = 1
    }

    private val items: List<Any> = flattenProjects(projects)

    class ProjectHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectTitle: TextView = view.findViewById(R.id.tvProjectTitle)
    }
    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentNameTextView: TextView = view.findViewById(R.id.StudentName)
        val groupTextView: TextView = view.findViewById(R.id.StudentGroup)
        val gradeSpinner: Spinner = view.findViewById(R.id.spinnerGrade)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is String -> VIEW_TYPE_PROJECT_HEADER
            is StudentGrade -> VIEW_TYPE_STUDENT
            else -> throw IllegalArgumentException(" $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PROJECT_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_project_header, parent, false)
                ProjectHeaderViewHolder(view)
            }
            VIEW_TYPE_STUDENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_student_grade, parent, false)
                StudentViewHolder(view)
            }
            else -> throw IllegalArgumentException(": $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProjectHeaderViewHolder -> {
                val projectTitle = items[position] as String
                holder.projectTitle.text = projectTitle
            }
            is StudentViewHolder -> {
                val student = items[position] as StudentGrade

                holder.studentNameTextView.text = student.name

                holder.groupTextView.text = student.groupName

                val grades = listOf(
                    "",
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
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                holder.gradeSpinner.adapter = adapter

                if (student.grade.isNotEmpty()) {
                    val index = grades.indexOf(student.grade)
                    if (index > 0) {
                        holder.gradeSpinner.setSelection(index)
                    }
                }

                holder.gradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        if (pos > 0) {
                            student.grade = grades[pos]
                        } else {
                            student.grade = ""
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size
    private fun flattenProjects(projects: List<ProjectWithStudents>): List<Any> {
        val result = mutableListOf<Any>()

        for (project in projects) {
            result.add(project.projectTitle)
            result.addAll(project.students)
        }

        return result
    }

    fun getGrades(): List<StudentGrade> {
        return items.filterIsInstance<StudentGrade>()
    }

    fun areAllStudentsGraded(): Boolean {
        return getGrades().all { it.grade.isNotEmpty() && it.grade != "Выберите оценку" }
    }
}
