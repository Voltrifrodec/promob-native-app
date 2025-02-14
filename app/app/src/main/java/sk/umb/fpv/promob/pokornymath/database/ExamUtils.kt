package sk.umb.fpv.promob.pokornymath.database

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import sk.umb.fpv.promob.pokornymath.ExamViewActivity
import sk.umb.fpv.promob.pokornymath.R
import sk.umb.fpv.promob.pokornymath.entities.ExamEntity

class ExamUtils(context: Context, values: List<ExamEntity>) : ArrayAdapter<ExamEntity>(context, 0, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var viewObject = convertView
        if (viewObject == null) {
            viewObject = LayoutInflater.from(context).inflate(R.layout.exam_list_item, parent, false)
        }

        val exam = getItem(position)

        val examFinsihed = viewObject!!.findViewById<CheckBox>(R.id.finished)
        examFinsihed.isChecked = exam!!.isFinished

        val examName = viewObject!!.findViewById<TextView>(R.id.name)
        examName.text = exam!!.title

        val examQuestionsCount = viewObject!!.findViewById<TextView>(R.id.questionsCount)
        examQuestionsCount.text = String.format(exam!!.questions.size.toString())

        // Pridelenie akcie pre jednotlivy test v zozname
        // ID testu sa ulozi do seriazable (?)
        // https://stackoverflow.com/questions/5265913/how-to-use-putextra-and-getextra-for-string-data
        val startButton = viewObject.findViewById<Button>(R.id.startQuizButton)
        startButton.setOnClickListener {
            val intent = Intent(context, ExamViewActivity::class.java).apply {
                putExtra("exam_id", exam.id)
            }
            context.startActivity(intent)
        }


        return viewObject
    }

}
