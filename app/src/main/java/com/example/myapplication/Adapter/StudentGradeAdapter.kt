package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.StudentGrade
import com.example.myapplication.R

class StudentGradeAdapter(private val students: List<StudentGrade>) :
    RecyclerView.Adapter<StudentGradeAdapter.StudentGradeViewHolder>() {

    class StudentGradeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentNameTextView: TextView = view.findViewById(R.id.tvStudentName)
        val gradeSpinner: Spinner = view.findViewById(R.id.spinnerGrade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentGradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_grade, parent, false)
        return StudentGradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentGradeViewHolder, position: Int) {
        val student = students[position]


        holder.studentNameTextView.text = student.name


        val grades = arrayOf("Выберите оценку", "3", "4", "5")
        val adapter = ArrayAdapter(holder.itemView.context,
            android.R.layout.simple_spinner_item, grades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.gradeSpinner.adapter = adapter


        if (student.grade > 0) {
            val index = grades.indexOf(student.grade.toString())
            if (index > 0) {
                holder.gradeSpinner.setSelection(index)
            }
        }

        holder.gradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos > 0) {
                    student.grade = grades[pos].toInt()
                } else {
                    student.grade = 0
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    override fun getItemCount() = students.size
    fun getGrades(): List<StudentGrade> {
        return students
    }

    fun areAllStudentsGraded(): Boolean {
        return students.all { it.grade > 0 }
    }
}