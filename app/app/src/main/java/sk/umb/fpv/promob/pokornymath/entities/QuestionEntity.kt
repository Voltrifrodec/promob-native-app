package sk.umb.fpv.promob.pokornymath.entities

data class QuestionEntity(
    val id: Int,
    val type: Int,
    val question: String,
    val assets: String,
    val options: List<String>,
    val correctAnswer: Int
)
