package sk.umb.fpv.promob.pokornymath

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import sk.umb.fpv.promob.pokornymath.database.DatabaseService
import sk.umb.fpv.promob.pokornymath.database.ExamEntity

class ExamViewActivity : AppCompatActivity() {

    private val databaseService = DatabaseService(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exam_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val examId = intent.getIntExtra("exam_id", -1)
        if (examId != -1) {
            Log.i("TEST_TAG", "Loaded exam with ID $examId")
            // Nacitanie testu podla ID. Ak neexistuje, tak znovu nacitaj zoznam testov.
            val exam: ExamEntity? = databaseService.getExamById(examId)
            if (exam == null) {
                val intent = Intent(this, ExamListActivity::class.java)
                this.startActivity(intent)
                return
            }

            val examTitle = findViewById<TextView>(R.id.examTitle)
            examTitle.text = exam.title

        }

        // Tlacidlo pre navrat do testov / ukoncenie testu
        val closeExamButton = findViewById<TextView>(R.id.closeExamButton)
        closeExamButton.setOnClickListener {
            val intent = Intent(this, ExamListActivity::class.java)
            this.startActivity(intent)
        }
    }
}