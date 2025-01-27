package sk.umb.fpv.promob.pokornymath.database

import android.provider.BaseColumns

// https://developer.android.com/training/data-storage/sqlite#kotlin
object FeedReaderContract {

    // Table contents are grouped together in an anonymous object.

    // Struktura tabulky exams
    // Ukladanie boolean hodnot v SQLLite: https://sqlite.org/datatype3.html 2.1
    object FeedExams : BaseColumns {
        const val TABLE_NAME = "exams"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_QUESTIONS = "questions"
        const val COLUMN_NAME_FINISHED = "is_finished"
    }

    // Struktura tabulky questions
    object FeedQuestions : BaseColumns {
        const val TABLE_NAME = "questions"
        const val COLUMN_NAME_TYPE = "type"
        const val COLUMN_NAME_QUESTION = "question"
        const val COLUMN_NAME_ASSETS = "assets"
        const val COLUMN_NAME_OPTIONS = "options"
        const val COLUMN_NAME_CORRECT = "correct_answer"
    }

    object FeedCompletedExams : BaseColumns {
        const val TABLE_NAME = "completed_exams"
        const val COLUMN_NAME_EXAM_ID = "exam_id"
        const val COLUMN_NAME_SCORE = "score"
        const val COLUMN_NAME_FINISHED_AT = "finished_at"
    }


}