package sk.umb.fpv.promob.pokornymath

import android.os.Bundle
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import sk.umb.fpv.promob.pokornymath.database.DatabaseService
import sk.umb.fpv.promob.pokornymath.database.ExamUtils

class ExamListActivity : AppCompatActivity() {
    private val databaseService = DatabaseService(this)

    // Vytvorenie zoznamu testov z databazy
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exam_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Nacitanie dostupnych testov z databazy
        val examListView = findViewById<ListView>(R.id.exam_list_view)
        val exams = databaseService.getAllExams()

        if (exams.isNotEmpty()) {
            val adapter = ExamUtils(this, exams)
            examListView.adapter = adapter
        }
    }
}