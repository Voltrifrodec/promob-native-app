package sk.umb.fpv.promob.pokornymath

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExamCompletedView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exam_completed_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ziskanie ID testu, vysledkov a pouzivatelovych moznosti
        val examId = intent.getIntExtra("exam_id", -1)
        val score = intent.getStringExtra("score")
        val answers = intent.getStringExtra("answers")

        Log.i("TEST_TAG", "Exam ID: $examId")
        Log.i("TEST_TAG", "Score: $score")
        Log.i("TEST_TAG", "Answers: ${answers.toString()}")


    }
}