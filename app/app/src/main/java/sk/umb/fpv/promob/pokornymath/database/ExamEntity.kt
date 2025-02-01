package sk.umb.fpv.promob.pokornymath.database

data class ExamEntity(
    val id: Int,
    val title: String,
    val questions: List<Int>,
    val isFinished: Boolean
)
