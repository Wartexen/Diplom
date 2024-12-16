package com.example.myapplication
import ProjectAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class ProjectListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_list)


        val selectedDpp = intent.getStringExtra("selectedDpp")
        val selectedCommission = intent.getStringExtra("selectedCommission")
        val selectedDate = intent.getStringExtra("selectedDate")
        val selectedDppTextView = findViewById<TextView>(R.id.selectedDpp)
        val selectedCommissionTextView = findViewById<TextView>(R.id.selectedCommission)
        val selectedDateTextView = findViewById<TextView>(R.id.selectedDate)

        selectedDppTextView.text = "$selectedDpp"
        selectedCommissionTextView.text = "$selectedCommission"
        selectedDateTextView.text = "$selectedDate"


        val projects = getProjects()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProjects)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ProjectAdapter(projects)
        recyclerView.adapter = adapter

        adapter.onProjectClickListener = { project ->
            val intent = Intent(this, ProjectDetailsActivity::class.java)
            intent.putExtra("project", project)
            startActivity(intent)
        }
    }


    private fun getProjects(): List<Project> {
        return listOf(
            Project(
                "Project 1",
                //"Details 1. Руководитель: Иванов Иван Иванович, Направление: Разработка приложений, Студенты: Петров Петр Петрович, Сидоров Сидор Сидорович",
                "Details 1.",
                "Иванов Иван Иванович",
                "Разработка приложений",
                listOf("Петров Петр Петрович", "Сидоров Сидор Сидорович").toString()
            ),
            Project(
                "Project 2",
                "Details 2.",
                "Петрова Петровна Петровна",
                "Веб-разработка",
                listOf("Кузнецов Кузьма Кузьмич").toString()
            ),
            Project(
                "Project 3",
                "Details 3.",
                "Сидоров Сидор Сидорович",
                "Анализ данных",
                listOf("Иванова Инна Ивановна", "Петров Петр Петрович").toString()
            )
        )
    }
}


//class ProjectListActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_project_list)
//
//        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewProjects)
//        val buttonNext = findViewById<Button>(R.id.buttonNext)
//
//
//        val projects = listOf(
//            ProjectListAdapter.Project("Название проекта 1", "миниатюры.ru"),
//            ProjectListAdapter.Project("Название проекта 2", "неминиатюры.ru"),
//            ProjectListAdapter.Project("Название проекта 3", "миниатюрыминиатюры.ru"),
//
//        )
//        val adapter = ProjectListAdapter(projects)
//
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        fun getProjects(dpp: String, commission: String, date: String): List<Project> {
//            // Заглушка для получения данных (на время тестирования)
//            return listOf(
//                Project(
//                    "Разработка UX-стратегии для сайта miniatures.ru",
//                    "Руководитель: Аршинский Вадим Леонидович\n" +
//                            "Направление: Продвижение и дизайн web-ресурсов\n" +
//                            "Студенты: Иванов Иван Иванович, Петров Петр Петрович",
//                    "Аршинский Вадим Леонидович",
//                    "Продвижение и дизайн web-ресурсов",
//                    listOf("Иванов Иван Иванович", "Петров Петр Петрович")
//                ),
//                Project(
//                    "Другой проект",
//                    "Руководитель: Другой руководитель\n" +
//                            "Направление: Другое направление\n" +
//                            "Студенты: Сидоров Сидор Сидорович",
//                    "Другой руководитель",
//                    "Другое направление",
//                    listOf("Сидоров Сидор Сидорович")
//                ))
//
//        }
//
//        buttonNext.setOnClickListener {
//
//        }
//    }
//}