package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.Question

class QuestionAdapter(val questions: MutableList<Question>) :
    RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {
    private var onQuestionSave: ((Question, String) -> Unit)? = null
    //private var onQuestionDelete: ((Question) -> Unit)? = null
    private var onQuestionDelete: ((Int) -> Unit)? = null
    fun setOnQuestionSaveListener(listener: (Question, String) -> Unit) {
        this.onQuestionSave = listener
    }

    fun setOnQuestionDeleteListener(listener: (Int) -> Unit) {
        this.onQuestionDelete = listener
    }
    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionEditText: EditText = itemView.findViewById(R.id.questionEditText)
        val saveButton: ImageView = itemView.findViewById(R.id.saveButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        holder.questionEditText.setText(question.Text)
        holder.questionEditText.setOnFocusChangeListener { _, hasFocus ->
            holder.saveButton.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }

        holder.saveButton.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Сохранение...", Toast.LENGTH_SHORT).show()
            val newText = holder.questionEditText.text.toString().trim()
            Log.d("QUESTION_ADAPTER", "Old text: ${question.Text}, New text: $newText")
            if (newText != question.Text) {
                Log.d("QUESTION_ADAPTER", "Text changed, triggering callback")
                val updatedQuestion = question.copy(Text = newText)
                questions[position] = updatedQuestion
                Log.d("QUESTION_ADAPTER", "Invoking onQuestionSave with text: $newText")
                onQuestionSave?.invoke(updatedQuestion, newText)

            }
            holder.saveButton.visibility = View.GONE
            holder.questionEditText.clearFocus()
        }

        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(holder.itemView.context, question.ID, position)
        }

        holder.questionEditText.setOnFocusChangeListener { _, hasFocus ->
            holder.saveButton.visibility = if (hasFocus) View.VISIBLE else View.GONE
            holder.deleteButton.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
    }
    private fun showDeleteConfirmationDialog(context: Context, questionId: Int, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Удалить вопрос?")
            .setMessage("Вопрос будет удалён безвозвратно.")
            .setPositiveButton("Удалить") { _, _ ->
                // Передаём ID вопроса, а не объект
                onQuestionDelete?.invoke(questionId)
                questions.removeAt(position)
                notifyItemRemoved(position)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }



    override fun getItemCount(): Int = questions.size
}

