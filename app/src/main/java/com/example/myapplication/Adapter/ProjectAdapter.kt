package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.Db.Project
import com.example.myapplication.R

class ProjectAdapter(private val projects: List<Project>) :
    RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    var onProjectClickListener: ((Project) -> Unit)? = null

    inner class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectName: TextView = view.findViewById(R.id.projectName)
        val projectDetails: TextView = view.findViewById(R.id.projectDetails)
        val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        val statusText: TextView = itemView.findViewById(R.id.statusText)
        val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
        val cardView: CardView = view as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.project_item, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.projectName.text = project.Title
        holder.projectDetails.text = "Руководитель: ${project.Supervisor}"

        when (project.Status) {
            "Защита начата" -> {
                holder.statusIndicator.setBackgroundResource(R.drawable.status_indicator_in_progress)
                holder.statusText.text = "Защита начата"
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.status_in_progress))
            }
            "Вопросы расшифровываются" -> {
                holder.statusIndicator.setBackgroundResource(R.drawable.status_indicator_processing)
                holder.statusText.text = "Вопросы расшифровываются"
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.status_processing))
            }
            "Ошибка загрузки аудио.." -> {
                holder.statusIndicator.setBackgroundResource(R.drawable.status_indicator_error)
                holder.statusText.text = "Ошибка загрузки аудио"
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.status_error))
                holder.statusIcon.visibility = View.VISIBLE
            }
            "Готов" -> {
                holder.statusIndicator.setBackgroundResource(R.drawable.status_indicator_ready)
                holder.statusText.text = "Готов"
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.status_ready))
                holder.statusIcon.visibility = View.VISIBLE
            }
            "Защита не начата", null -> {
                holder.statusIndicator.setBackgroundResource(R.drawable.status_indicator_not_started)
                holder.statusText.text = "Защита не начата"
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.status_not_started))
                holder.statusIcon.visibility = View.GONE
            }
            else -> {
                holder.statusIndicator.setBackgroundResource(R.drawable.status_indicator_not_ready)
                holder.statusText.text = project.Status ?: "Неизвестный статус"
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.status_error))
                holder.statusIcon.visibility = View.GONE
            }
        }
        holder.cardView.setOnClickListener {
            onProjectClickListener?.invoke(project)
        }
    }

    override fun getItemCount(): Int = projects.size
}