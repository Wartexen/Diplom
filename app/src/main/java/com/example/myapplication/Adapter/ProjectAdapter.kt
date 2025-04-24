package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.Project
import com.example.myapplication.R

class ProjectAdapter(private val projects: List<Project>) :
    RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    var onProjectClickListener: ((Project) -> Unit)? = null

    inner class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectName: TextView = view.findViewById(R.id.projectName)
        val projectDetails: TextView = view.findViewById(R.id.projectDetails)
        val statusIndicator: View = view.findViewById(R.id.statusIndicator)
        val statusText: TextView = view.findViewById(R.id.statusText)
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

        // Set status indicator and text
        if (project.Status) {
            holder.statusIndicator.setBackgroundResource(R.drawable.status_indicator_ready)
            holder.statusText.text = "Готов"
            holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.status_ready))
        } else {
            holder.statusIndicator.setBackgroundResource(R.drawable.status_indicator_not_ready)
            holder.statusText.text = "Не готов"
            holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.status_not_ready))
        }

        holder.cardView.setOnClickListener {
            onProjectClickListener?.invoke(project)
        }
    }

    override fun getItemCount(): Int = projects.size
}