package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.Question
import com.example.myapplication.R

class QuestionAdapter(private val questions: MutableList<Question>) :
    RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    private var onQuestionDeleteListener: ((Int) -> Unit)? = null
    private var onQuestionSaveListener: ((Question, String) -> Unit)? = null

    fun setOnQuestionDeleteListener(listener: (Int) -> Unit) {
        onQuestionDeleteListener = listener
    }

    fun setOnQuestionSaveListener(listener: (Question, String) -> Unit) {
        onQuestionSaveListener = listener
    }

    class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionText: TextView = view.findViewById(R.id.questionText)
        val editButton: ImageView = view.findViewById(R.id.editButton)
        val deleteButton: ImageView = view.findViewById(R.id.deleteButton)
        val editQuestionText: EditText = view.findViewById(R.id.editQuestionText)
        val editButtonsLayout: LinearLayout = view.findViewById(R.id.editButtonsLayout)
        val cancelButton: Button = view.findViewById(R.id.cancelButton)
        val saveButton: Button = view.findViewById(R.id.saveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        holder.questionText.text = question.Text

        holder.editButton.setOnClickListener {
            holder.questionText.visibility = View.GONE
            holder.editQuestionText.visibility = View.VISIBLE
            holder.editButtonsLayout.visibility = View.VISIBLE
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE

            holder.editQuestionText.setText(question.Text)
            holder.editQuestionText.requestFocus()
        }

        holder.deleteButton.setOnClickListener {
            onQuestionDeleteListener?.invoke(question.ID)
        }

        holder.cancelButton.setOnClickListener {
            holder.questionText.visibility = View.VISIBLE
            holder.editQuestionText.visibility = View.GONE
            holder.editButtonsLayout.visibility = View.GONE
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE
        }

        holder.saveButton.setOnClickListener {
            val newText = holder.editQuestionText.text.toString().trim()
            if (newText.isNotEmpty()) {
                onQuestionSaveListener?.invoke(question, newText)

                holder.questionText.visibility = View.VISIBLE
                holder.editQuestionText.visibility = View.GONE
                holder.editQuestionText.setText(newText)
                holder.editButtonsLayout.visibility = View.GONE
                holder.editButton.visibility = View.VISIBLE
                holder.deleteButton.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = questions.size
}
