package sk.umb.fpv.promob.pokornymath.entities

data class ExamEntity(
    val id: Int,
    val title: String,
    val questions: List<Int>,
    var isFinished: Boolean
)
