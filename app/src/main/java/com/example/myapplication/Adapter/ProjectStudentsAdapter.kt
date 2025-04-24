package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.ProjectWithStudents
import com.example.myapplication.Models.StudentGrade
import com.example.myapplication.R

class ProjectStudentsAdapter(private val projects: List<ProjectWithStudents>) :
    RecyclerView.Adapter<ProjectStudentsAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectTitle: TextView = view.findViewById(R.id.tvProjectTitle)
        val studentsRecyclerView: RecyclerView = view.findViewById(R.id.recyclerViewProjectStudents)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project_with_students, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.projectTitle.text = project.projectTitle

        // Set up nested RecyclerView for students
        val studentAdapter = StudentGradeAdapter(project.students)
        holder.studentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = studentAdapter
        }
    }

    override fun getItemCount() = projects.size

    fun areAllStudentsGraded(): Boolean {
        return projects.all { project ->
            project.students.all { student -> student.grade > 0 }
        }
    }

    fun getGrades(): List<StudentGrade> {
        val allGrades = mutableListOf<StudentGrade>()
        projects.forEach { project ->
            allGrades.addAll(project.students)
        }
        return allGrades
    }
}
