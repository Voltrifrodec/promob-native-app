package sk.umb.fpv.promob.pokornymath.database

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

import sk.umb.fpv.promob.pokornymath.database.FeedReaderContract.FeedExams
import sk.umb.fpv.promob.pokornymath.database.FeedReaderContract.FeedCompletedExams
import sk.umb.fpv.promob.pokornymath.database.FeedReaderContract.FeedQuestions
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader

// https://developer.android.com/training/data-storage/sqlite
class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        // Konfiguracne premenne pre nastavenie databazy
        private const val DATABASE_NAME = "pokornymath.db"
        private const val DATABASE_VERSION = 1  // Inkrementujeme ked menime schemu databazy

        // Konfiguracne premenne pre vlastnosti databazy - vyuzivane pri vytvarani databazy
        private const val PREF_NAME = "DatabasePreferences"
        private const val PREF_DB_INITIALIZED = "isDatabaseInitialized"

        // Vytvorenie tabuliek
        private const val SQL_CREATE_EXAMS =
            "CREATE TABLE IF NOT EXISTS ${FeedExams.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${FeedExams.COLUMN_NAME_TITLE} TEXT," +
                    "${FeedExams.COLUMN_NAME_QUESTIONS} TEXT," +
                    "${FeedExams.COLUMN_NAME_FINISHED} INTEGER" +
            ")"

        private const val SQL_CREATE_COMPLETED_EXAMS =
            "CREATE TABLE IF NOT EXISTS ${FeedCompletedExams.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${FeedCompletedExams.COLUMN_NAME_EXAM_ID} TEXT," +
                    "${FeedCompletedExams.COLUMN_NAME_SCORE} INTEGER," +
                    "${FeedCompletedExams.COLUMN_NAME_FINISHED_AT} INTEGER" +
           ")"

        private const val SQL_CREATE_QUESTIONS =
            "CREATE TABLE IF NOT EXISTS ${FeedQuestions.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${FeedQuestions.COLUMN_NAME_TYPE} INTEGER," +
                    "${FeedQuestions.COLUMN_NAME_QUESTION} TEXT," +
                    "${FeedQuestions.COLUMN_NAME_ASSETS} TEXT," +
                    "${FeedQuestions.COLUMN_NAME_OPTIONS} TEXT," +
                    "${FeedQuestions.COLUMN_NAME_CORRECT} INTEGER" +
            ")"

        // Vymazanie databazy 'exams'
        private const val SQL_DELETE_EXAMS = "DROP TABLE IF EXISTS ${FeedExams.TABLE_NAME}"

        // Vymazanie databazy 'completed_exams'
        private const val SQL_DELETE_COMPLETED_EXAMS = "DROP TABLE IF EXISTS ${FeedCompletedExams.TABLE_NAME}"

        // Vymazanie databazy 'questions'
        private const val SQL_DELETE_QUESTIONS = "DROP TABLE IF EXISTS ${FeedQuestions.TABLE_NAME}"

        // Nacitaj vsetky ulozene testy
        private const val SQL_GET_ALL_COMPLETED_EXAMS = "SELECT * FROM ${FeedExams.TABLE_NAME}$"

        // Inicializacia databazy a tabuliek
        fun initializeSQL(context: Context) {

            // Ziskanie stavu o inicializacii databazy z Preferences
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val isDatabaseInitialized = sharedPreferences.getBoolean(PREF_DB_INITIALIZED, false)    // Druhy parameter je co to ma vratit ak Pref neexistuje!

            // Ak databaza uz bola inicializovana, tak rovno ukoncime inicializaciu
            if (isDatabaseInitialized) {
                Log.i("DatabaseInitializationStatus", "Database already exist, skipping initialization...")
                return
            }

            // Ak neexistuje, tak neexistuju ani tabulky v nej -- vytvorenie premennej pre pracu s db a vytvorenie tabuliek
            val dbHelper = FeedReaderDbHelper(context)
            val db = dbHelper.writableDatabase

            try {
                db.execSQL(SQL_CREATE_EXAMS)
                db.execSQL(SQL_CREATE_QUESTIONS)
                db.execSQL(SQL_CREATE_COMPLETED_EXAMS)
                Log.i("TablesInitializationStatus", "Tables initiated successfully.")

                // Vlozenie ukazkoveho testu s otazkami

                // Aktualizovanie Preferences
                sharedPreferences.edit().putBoolean(PREF_DB_INITIALIZED, true).apply()

            } catch (e: Exception) {
                Log.e("TablesInitializationStatus", "Error during database initialization", e)
            } finally {
                db.close()
            }
        }

        fun initializeSQLScript(context: Context, file: Int) {
            // Kontrola  stavu o inicializacii databazy z Preferences -- ak neexistuje, tak nic nevytvaraj
            if (!isDatabaseInitialized(context)) {
                Log.e("DatabaseScriptInsertion", "Failed to run script: database is not initialized properly!")
                return
            }

            val db = FeedReaderDbHelper(context).writableDatabase

            // Otvorenie a nacitanie suboru so skriptom
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
    }

    // Vytvaranie databazy
    override fun onCreate(db: SQLiteDatabase?) {

    }

    // "Upgrade" databazy
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { }




}