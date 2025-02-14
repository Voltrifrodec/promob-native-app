package sk.umb.fpv.promob.pokornymath.entities

data class CompletedExamEntity(
    val id: Int,
    val score: Int,
    val examId: Int,
    val finishedAt: String
)
