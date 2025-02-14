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
import sk.umb.fpv.promob.pokornymath.database.CompletedExamEntity
import sk.umb.fpv.promob.pokornymath.database.DatabaseService
import sk.umb.fpv.promob.pokornymath.database.ExamEntity
import sk.umb.fpv.promob.pokornymath.database.QuestionEntity
import java.util.Date
import kotlin.reflect.typeOf

class ExamViewActivity : AppCompatActivity() {

    private var examId: Int = -1
    private val databaseService = DatabaseService(this)
    private var currentQuestionIndex: Int = 0
    private var currentScore: Int = 0
    private var questionsAmount: Int = 0
    private var answers : HashMap<Int, String> = HashMap<Int, String> ()
    // private var answers: MutableList<String?> = mutableListOf() // Super vec: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-mutable-list/
    private lateinit var selectedAnswer: String

    private lateinit var questionTextView: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var questions: List<QuestionEntity>
    private var examEntity: ExamEntity? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exam_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.examId = intent.getIntExtra("exam_id", -1)
        if (examId != -1) {
            Log.i("TEST_TAG", "Loaded exam with ID $examId")
            // Nacitanie testu podla ID. Ak neexistuje, tak znovu nacitaj zoznam testov.
            this.examEntity = databaseService.getExamById(examId)
            if (this.examEntity == null) {
                val intent = Intent(this, ExamListActivity::class.java)
                this.startActivity(intent)
                return
            }

            val examTitle = findViewById<TextView>(R.id.examTitle)
            examTitle.text = this.examEntity!!.title

            // Nacitanie otazok, textoveho objekta pre otazku a kontajnera, do ktoreho budeme vkladat moznosti
            this.questions = loadQuestionsByIdList(this.examEntity!!.questions)
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
        var selectedAnswer: String? = null

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
                    radioButton.setOnClickListener {
                        this.selectedAnswer = option
                        saveAnswer(currentQuestionIndex, index.toString())
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
                        selectedAnswer = text.toString()
                        saveAnswer(currentQuestionIndex, selectedAnswer)
                    }
                }
                optionsContainer.addView(editText)
                optionsContainer.addView(submitButton)
            }
        }

        // Tlacidla pre predchadzajucu/nasledujucu otazku
        var previousQuestionButton = findViewById<Button>(R.id.prevQuestionButton).setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                loadQuestion(currentQuestionIndex)
            }
        }
        val nextQuestionButton = findViewById<Button>(R.id.nextQuestionButton)
        nextQuestionButton.setOnClickListener {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                loadQuestion(currentQuestionIndex)
                saveAnswer(currentQuestionIndex, selectedAnswer)
            }
        }
        // Ak sa nachadzame na poslednej otazke, tak zmenime text a funkciu pre tlacidlo 'Dalsie'
        if (currentQuestionIndex == questions.size - 1) {
            nextQuestionButton.text = "Dokončiť"
            nextQuestionButton.setOnClickListener {
                finishExam()
            }
        }

    }

    private fun finishExam() {

        // Vypocet skore podla zvolenych moznosti
        // Lepsie nez to pocitat pocas testu -- takto sa to vypocita naraz
        calculateScore()

        val intent = Intent(this, ExamListActivity::class.java).apply {
            putExtra("exam_id", examId)
            putExtra("score", currentScore.toString())
            putExtra("answers", answers.values.toString())
        }

        var completedExam = ExamEntity(examId, examEntity!!.title, examEntity!!.questions, false)
        if (currentScore == questions.size) {
            completedExam.isFinished = true
        }
        databaseService.updateExam(completedExam)

        val finishTime = Date().time
        val completedExamRequest = CompletedExamEntity(0, currentScore, examId, finishTime.toString())
        databaseService.saveCompletedExam(completedExamRequest)

        Log.i("TEST_TAG", "Values in answers: ${answers.values}")
        this.startActivity(intent)
    }

    private fun saveAnswer(index: Int, value: String?) {
        if(value == null) {
            return
        }
        Log.i("TEST_TAG", "Saving value=$value on for question number $index")
        answers[index] = value
        Log.i("TEST_TAG", "Saved value=${answers[index]}")
    }

    private fun calculateScore() {

        questions.map { question ->
            val index = questions.indexOf(question)
            if (answers[index] != null && answers[index]?.toInt() == question.correctAnswer) {
                currentScore++;
            }
        }

    }


    /*private fun validateAnswer(index: Int, answer: String?) {


        if (option == null) {
            return
        }

        val correctAnswer = questions[question].correctAnswer
        val selectedOption: Int
        when (option) {
            "True" -> {
                selectedOption = 1
            }
            "False" -> {
                selectedOption = 0
            }
            else -> {
                selectedOption = option.toIntOrNull() ?: -1
            }

        }
        Log.i("TEST_TAG", "Validating: $selectedOption and ${correctAnswer}")

        if (selectedOption == correctAnswer) {
            this.currentScore++;
        }
        this.answers.add(option)

    }*/

}