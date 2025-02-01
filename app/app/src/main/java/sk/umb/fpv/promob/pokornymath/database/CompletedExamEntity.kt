package sk.umb.fpv.promob.pokornymath.database

data class CompletedExamEntity(
    val id: Int,
    val score: Int,
    val examId: Int,
    val finishedAt: String
)
