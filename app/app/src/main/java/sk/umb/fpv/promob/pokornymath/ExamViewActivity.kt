package sk.umb.fpv.promob.pokornymath

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text
import sk.umb.fpv.promob.pokornymath.database.DatabaseService
import sk.umb.fpv.promob.pokornymath.database.ExamEntity
import sk.umb.fpv.promob.pokornymath.database.QuestionEntity

class ExamViewActivity : AppCompatActivity() {

    private val databaseService = DatabaseService(this)
    private var currentQuestionIndex: Int = 0
    private var currentScore: Int = 0
    private var questionsAmount: Int = 0

    private lateinit var questionTextView: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var questions: List<QuestionEntity>

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

            // Nacitanie otazok, textoveho objekta pre otazku a kontajnera, do ktoreho budeme vkladat moznosti
            this.questions = loadQuestionsByIdList(exam.questions)
            this.questionsAmount = this.questions.size
            this.optionsContainer = findViewById<LinearLayout>(R.id.optionsContainer)
            this.questionTextView = findViewById<TextView>(R.id.questionText)

            // Nacitanie prvej otazky (spustenie testu)
            loadQuestion(this.currentQuestionIndex)
        }

        // Tlacidlo pre navrat do testov / ukoncenie testu
        val closeExamButton = findViewById<TextView>(R.id.closeExamButton)
        closeExamButton.setOnClickListener {
            val intent = Intent(this, ExamListActivity::class.java)
            this.startActivity(intent)
        }


        // Tlacidla pre predchadzajucu/nasledujucu otazku
        var previousQuestionButton = findViewById<Button>(R.id.prevQuestionButton).setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                loadQuestion(currentQuestionIndex)
            }
        }
        var nextQuestionButton = findViewById<Button>(R.id.nextQuestionButton).setOnClickListener {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                loadQuestion(currentQuestionIndex)
            }
        }


    }

    // Ziskanie otazok z databazy podla zoznamu s ID otazok
    private fun loadQuestionsByIdList(questionsIdList: List<Int>) : List<QuestionEntity> {

        // Prejdeme zoznam s ID a nacitame otazky do premennej
        val questions = questionsIdList.mapNotNull { questionId ->
            Log.i("TEST_TAG", "Looking for question with ID=$questionId...")
            val question = databaseService.getQuestionById(questionId)
            Log.i("TEST_TAG", "Found it.")
            question
        }
        Log.i("TEST_TAG", "questions found: $questions from $questionsIdList")

        return questions
    }

    // Nacitanie otazky a moznosti
    // Workflow:
    //  1. Vycisti predosle objekty.
    //  2. Zobraz text otazky.
    //  3. Vytvor nanovo objekty podla typu otazky (1 - a/b/c, 2 - True/False, 3 - Input).
    //  4. Ak pouzivatel zvolil moznost/zadal hodnotu, odblokuj tlacidlo (update indexu, znovu sa
    //     zavola loadQuestion()).
    private fun loadQuestion(questionIndex: Int) {

        // 0. Premenna pre ukladanie pouzivatelovej odpovede
        val answer: String? = null

        // 1. Vycistenie predoslych objektov
        this.optionsContainer.removeAllViews()

        // 2. Zobrazenie novej otazky
        val question = questions[questionIndex]
        this.questionTextView.text = question.question
        // 2.1 Zobrazenie tlacidla 'Predchadzajuca otazka' ak sme na druhej otazke (a vyssie)
        if (questionIndex > 0) {
            findViewById<Button>(R.id.prevQuestionButton).visibility = View.VISIBLE
        }
        else {
            findViewById<Button>(R.id.prevQuestionButton).visibility = View.INVISIBLE
        }
        // 2.2 Ak sa nachadzame na poslednej otazke, tak zmenime text a funkciu pre tlacidlo 'Dalsie'
        if (questionIndex == questions.size - 1) {
            val button = findViewById<Button>(R.id.nextQuestionButton)
            button.text = "Dokončiť"
            button.setOnClickListener {
                finishExam()
            }
        }

        // 3. Vytvorenie novych objektov podla typu otazky (1 - a/b/c, 2 - True/False, 3 - Input)
        // https://kotlinlang.org/docs/control-flow.html#when-expressions-and-statements
        when (question.type) {
            // Moznost 1 (a/b/c) a moznost 2 (True/False).
            // Generuju ten isty typ objektu, takze to mozeme spojit dokopy ako jeden stav
            1, 2 -> {
                val radioGroup = RadioGroup(this).apply {
                    orientation = RadioGroup.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                question.options.forEachIndexed { index, option ->
                    val radioButton = RadioButton(this).apply {
                        text = option
                        id = index
                        setTextColor(resources.getColor(R.color.white, null))
                    }
                    radioGroup.addView(radioButton)
                }
                optionsContainer.addView(radioGroup)
            }

            // Moznost 3: Vlastny vstup
            3 -> { // Input-based question
                val editText = EditText(this).apply {
                    hint = "Zadajte vasu odpoved"
                    setHintTextColor(resources.getColor(R.color.darkGray, null))
                    setTextColor(resources.getColor(R.color.white, null))
                }
                val submitButton = Button(this).apply {
                    text = "Potvrdiť"
                    setOnClickListener {
                        val answer = editText.text.toString()
                        Toast.makeText(context, "Answer: $answer", Toast.LENGTH_SHORT).show()
                    }
                }
                optionsContainer.addView(editText)
                optionsContainer.addView(submitButton)
            }
        }


    }

    private fun finishExam() {
        val intent = Intent(this, ExamCompletedView::class.java)
        this.startActivity(intent)
    }

}