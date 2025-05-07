package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.Student
import com.example.myapplication.R

class StudentAdapter(private val students: List<Student>) :
    RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentName: TextView = view.findViewById(R.id.studentName)
        val studentGroup: TextView = view.findViewById(R.id.studentGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]

        val fullName = "${student.Surname ?: ""} ${student.Name ?: ""} ${student.Patronymic ?: ""}".trim()
        holder.studentName.text = fullName
        holder.studentGroup.text = "Группа: ${student.ID_Group.Name}"
    }

    override fun getItemCount() = students.size
}
