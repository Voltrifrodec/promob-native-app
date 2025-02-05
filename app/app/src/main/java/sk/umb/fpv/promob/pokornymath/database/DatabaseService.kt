package sk.umb.fpv.promob.pokornymath.database

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import sk.umb.fpv.promob.pokornymath.R

import sk.umb.fpv.promob.pokornymath.database.FeedReaderContract.FeedExams
import sk.umb.fpv.promob.pokornymath.database.FeedReaderContract.FeedCompletedExams
import sk.umb.fpv.promob.pokornymath.database.FeedReaderContract.FeedQuestions
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader

// https://developer.android.com/training/data-storage/sqlite
class DatabaseService(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val context = context

    companion object {
        // Konfiguracne premenne pre nastavenie databazy
        private const val DATABASE_NAME = "pokornymath.db"
        private const val DATABASE_VERSION = 1  // Inkrementujeme ked menime schemu databazy

        // Konfiguracne premenne pre vlastnosti databazy - vyuzivane pri vytvarani databazy
        private const val PREF_NAME = "DatabasePreferences"
        private const val PREF_DB_INITIALIZED = "isDatabaseInitialized"

        // Nacitaj vsetky ulozene testy
        private const val SQL_GET_ALL_COMPLETED_EXAMS = "SELECT * FROM ${FeedExams.TABLE_NAME}$"

        private const val CREATE_EXAMS_TABLE =
            "CREATE TABLE IF NOT EXISTS ${FeedExams.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${FeedExams.COLUMN_NAME_TITLE} TEXT," +
                    "${FeedExams.COLUMN_NAME_QUESTIONS} TEXT," +
                    "${FeedExams.COLUMN_NAME_FINISHED} INTEGER" +
                    ")"

        private const val CREATE_COMPLETED_EXAMS_TABLE =
            "CREATE TABLE IF NOT EXISTS ${FeedCompletedExams.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${FeedCompletedExams.COLUMN_NAME_EXAM_ID} TEXT," +
                    "${FeedCompletedExams.COLUMN_NAME_SCORE} INTEGER," +
                    "${FeedCompletedExams.COLUMN_NAME_FINISHED_AT} INTEGER" +
                    ")"

        private const val CREATE_QUESTIONS_TABLE =
            "CREATE TABLE IF NOT EXISTS ${FeedQuestions.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${FeedQuestions.COLUMN_NAME_TYPE} INTEGER," +
                    "${FeedQuestions.COLUMN_NAME_QUESTION} TEXT," +
                    "${FeedQuestions.COLUMN_NAME_ASSETS} TEXT," +
                    "${FeedQuestions.COLUMN_NAME_OPTIONS} TEXT," +
                    "${FeedQuestions.COLUMN_NAME_CORRECT} INTEGER" +
                    ")"

    }

    // Inicializacia databazy a vlozenie jednoducheho testu
    fun initializeSQL() {

        // Ziskanie stavu o inicializacii databazy z Preferences
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isDatabaseInitialized = sharedPreferences.getBoolean(PREF_DB_INITIALIZED, false)    // Druhy parameter je co to ma vratit ak Pref neexistuje!

        // Ak databaza uz bola inicializovana, tak rovno ukoncime inicializaciu
        if (isDatabaseInitialized) {
            Log.i("DatabaseInitializationStatus", "Database already exist, skipping initialization...")
            return
        }

        // Ak neexistuje, tak neexistuju ani tabulky v nej -- vytvorenie premennej pre pracu s db a vytvorenie tabuliek
        val db = writableDatabase

        try {
            db.execSQL(CREATE_EXAMS_TABLE)
            db.execSQL(CREATE_QUESTIONS_TABLE)
            db.execSQL(CREATE_COMPLETED_EXAMS_TABLE)
            Log.i("TablesInitializationStatus", "Tables initiated successfully.")

            // Vlozenie ukazkoveho testu s otazkami

            // Aktualizovanie Preferences
            sharedPreferences.edit().putBoolean(PREF_DB_INITIALIZED, true).apply()

        } catch (e: Exception) {
            Log.e("TablesInitializationStatus", "Error during database initialization", e)
        } finally {
            db.close()
        }

        this.executeSQLScript(context, R.raw.insert_example_questions)
        this.executeSQLScript(context, R.raw.insert_example_exam)
    }

    fun executeSQLScript(context: Context, file: Int) {
        // Kontrola  stavu o inicializacii databazy z Preferences -- ak neexistuje, tak nic nevytvaraj
        if (!isDatabaseInitialized(context)) {
            Log.e("DatabaseScriptInsertion", "Failed to run script: database is not initialized properly!")
            return
        }

        // Otvorenie a nacitanie suboru so skriptom
        val db = this.writableDatabase
        try {
            val inputStream = context.resources.openRawResource(file)
            val fileReader = BufferedReader(InputStreamReader(inputStream))
            val commandBuilder = StringBuilder()

            // Oprava chyby: db.execSQL nepodporuje viacero prikazov naraz (zastavi sa po ;)... oprava:
            fileReader.forEachLine { line ->
                Log.i("DEBUG_FILE_READER", "Line: $line\t commandBuilder: $commandBuilder")
                val trimmedLine = line.trim()
                // Odignorujeme prazdne riadky a komentare, pre zrychlenie
                if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("--")) {
                    commandBuilder.append(trimmedLine).append(" ")
                    // Ak narazime na bodkociarku, tak zavolame execSQL a spustime prikazy, ktory sme zatial precitali
                    if (trimmedLine.endsWith(";")) {
                        val sql = commandBuilder.toString()
                        Log.i("DEBUG_FILE_READER", "Found ; -> executing script: \n$sql")
                        db.execSQL(sql)
                        commandBuilder.clear() // Restartovanie premennej pre dalsi prikaz
                    }
                }
            }

            Log.i("DatabaseScriptInsertion", "Script finished successfully.")

        } catch (e: Exception) {
            Log.e("DatabaseScriptInsertion", "Error while executing script", e)
        } finally {
            db.close()
        }

    }

    // Funkcia pre kontrolu inicializacie databazy
    fun isDatabaseInitialized(context: Context) : Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(PREF_DB_INITIALIZED, false) // ak je PREF_DB_INITIALIZED == false => false == true => false
    }

    fun getAllExams(): List<ExamEntity> {
        val db = this.readableDatabase
        val result = mutableListOf<ExamEntity>()

        db.query(
            FeedExams.TABLE_NAME,
            arrayOf(BaseColumns._ID, FeedExams.COLUMN_NAME_TITLE, FeedExams.COLUMN_NAME_QUESTIONS, FeedExams.COLUMN_NAME_FINISHED),
            null, null, null, null, null
        ).use { value ->
            while (value.moveToNext()) {
                val id = value.getString(value.getColumnIndexOrThrow(BaseColumns._ID)).toInt()
                val title = value.getString(value.getColumnIndexOrThrow(FeedExams.COLUMN_NAME_TITLE))
                val questions = value.getString(value.getColumnIndexOrThrow(FeedExams.COLUMN_NAME_QUESTIONS)).split(",").map { it.toInt() }
                val isFinished = value.getInt(value.getColumnIndexOrThrow(FeedExams.COLUMN_NAME_FINISHED)) == 1

                result.add(ExamEntity(id, title, questions, isFinished))
            }
        }
        /*val result = arrayOf<ExamEntity>()
        while (values.moveToNext()) {

            val id = values.getString(values.getColumnIndexOrThrow(BaseColumns._ID)).toInt()
            val title = values.getString(values.getColumnIndexOrThrow(FeedExams.COLUMN_NAME_TITLE))
            val questions = values.getString(values.getColumnIndexOrThrow(FeedExams.COLUMN_NAME_QUESTIONS)).split(",").map { it.toInt() }
            val isFinished = values.getInt(values.getColumnIndexOrThrow(FeedExams.COLUMN_NAME_FINISHED)) == 1

            result.fill(ExamEntity(id, title, questions, isFinished))
        }
        */

        Log.i("TEST_TAG", "Retrieved amount of exams: ${result.size}")
        return result
    }

    fun getExamById(examId: Int): ExamEntity? {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${FeedExams.TABLE_NAME} WHERE ${BaseColumns._ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(examId.toString()))

        return if (cursor.moveToNext()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(FeedExams.COLUMN_NAME_TITLE))
            val questions = cursor.getString(cursor.getColumnIndexOrThrow(FeedExams.COLUMN_NAME_QUESTIONS)).split(",").map { it.toInt() }
            val isFinished = cursor.getInt(cursor.getColumnIndexOrThrow(FeedExams.COLUMN_NAME_FINISHED)) == 1

            ExamEntity(examId, title, questions, isFinished)
        } else {
            Log.e("QUERY_ERR", "Could not retrieve exam with ID=$examId")
            null // return null = 'null' ?
        }.also {
            cursor.close()
        }
    }

    fun getAllQuestions(): List<QuestionEntity> {
        val db = this.readableDatabase
        val result = mutableListOf<QuestionEntity>()

        db.query(
            FeedQuestions.TABLE_NAME,
            arrayOf(BaseColumns._ID, FeedQuestions.COLUMN_NAME_QUESTION, FeedQuestions.COLUMN_NAME_TYPE, FeedQuestions.COLUMN_NAME_ASSETS, FeedQuestions.COLUMN_NAME_OPTIONS, FeedQuestions.COLUMN_NAME_CORRECT),
            null, null, null, null, null
        ).use { value ->
            while (value.moveToNext()) {
                val id = value.getInt(value.getColumnIndexOrThrow(BaseColumns._ID))
                val type = value.getInt(value.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_TYPE))
                val question = value.getString(value.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_QUESTION))
                val assets = value.getString(value.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_ASSETS)) ?: ""
                val options = value.getString(value.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_OPTIONS)).split(",")
                val correctAnswer = value.getInt(value.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_CORRECT))

                result.add(QuestionEntity(id, type, question, assets, options, correctAnswer))
            }
        }


        /*while (values.moveToNext()) {

            val id = values.getString(values.getColumnIndexOrThrow(BaseColumns._ID)).toInt()
            val type = values.getString(values.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_TYPE)).toInt()
            val question = values.getString(values.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_QUESTION))
            val assets = values.getString(values.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_ASSETS))
            val options = values.getString(values.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_OPTIONS)).split(",")
            val correctAnswer = values.getInt(values.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_CORRECT)).toInt()

            result.add(QuestionEntity(id, type, question, assets, options, correctAnswer))
        }*/

        Log.i("TEST_TAG", "Retrieved amount of questions: ${result.size}")
        return result
    }

    fun getQuestionById(questionId: Int): QuestionEntity? {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${FeedQuestions.TABLE_NAME} WHERE ${BaseColumns._ID} = $questionId"
        val cursor = db.rawQuery(query, arrayOf(questionId.toString()))

        return if (cursor.moveToNext()) {
            val type = cursor.getString(cursor.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_TYPE)).toInt()
            val question = cursor.getString(cursor.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_QUESTION))
            val assets = cursor.getString(cursor.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_ASSETS))
            val options = cursor.getString(cursor.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_OPTIONS)).split(",")
            val correctAnswer = cursor.getInt(cursor.getColumnIndexOrThrow(FeedQuestions.COLUMN_NAME_CORRECT)).toInt()

            QuestionEntity(questionId, type, question, assets, options, correctAnswer)
        } else {
            Log.e("QUERY_ERR", "Could not retrieve exam with ID=$questionId")
            null
        }.also {
            cursor.close()
        }
    }

    fun getAllCompletedExams(): Array<CompletedExamEntity> {
        val db = this.readableDatabase
        val values: Cursor = db.query(
            FeedCompletedExams.TABLE_NAME,
            arrayOf(BaseColumns._ID, FeedCompletedExams.COLUMN_NAME_SCORE, FeedCompletedExams.COLUMN_NAME_EXAM_ID, FeedCompletedExams.COLUMN_NAME_FINISHED_AT),
            null, null, null, null, null)

        val result = arrayOf<CompletedExamEntity>()
        while (values.moveToNext()) {

            val id = values.getString(values.getColumnIndexOrThrow(BaseColumns._ID)).toInt()
            val score = values.getString(values.getColumnIndexOrThrow(FeedCompletedExams.COLUMN_NAME_SCORE)).toInt()
            val examId = values.getString(values.getColumnIndexOrThrow(FeedCompletedExams.COLUMN_NAME_EXAM_ID)).toInt()
            val finishedAt = values.getString(values.getColumnIndexOrThrow(FeedCompletedExams.COLUMN_NAME_FINISHED_AT))

            result.fill(CompletedExamEntity(id, score, examId, finishedAt))
        }

        Log.i("TEST_TAG", "Retrieved amount of completed exams: ${result.size}")
        values.close()
        return result
    }

    // Vytvaranie databazy
    override fun onCreate(db: SQLiteDatabase) {}

    // "Upgrade" databazy
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val dropExamsTable = "DROP TABLE IF EXISTS ${FeedExams.TABLE_NAME}"
        val dropCompletedExamsTable = "DROP TABLE IF EXISTS ${FeedCompletedExams.TABLE_NAME}"
        val dropQuestionsTable = "DROP TABLE IF EXISTS ${FeedQuestions.TABLE_NAME}"
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(PREF_DB_INITIALIZED, false).apply()

        db.execSQL(dropExamsTable)
        db.execSQL(dropCompletedExamsTable)
        db.execSQL(dropQuestionsTable)

        this.onCreate(db)
    }
}