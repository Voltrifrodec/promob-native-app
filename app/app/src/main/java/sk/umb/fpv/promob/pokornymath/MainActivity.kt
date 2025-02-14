package sk.umb.fpv.promob.pokornymath

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import sk.umb.fpv.promob.pokornymath.database.DatabaseService

class MainActivity : AppCompatActivity() {
    private val databaseService = DatabaseService(this)

    // Akonahle sa nacita aplikacia, tak skontrolujem, ci je vytvorena databaza a ak nie,
    // tak zavolam funkciu na jej vytvorenie
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_menu) // Ktory fragment sa ma zobrazit

        // Kontrola inicializacie databazy po spusteni aplikacie
        Log.i("TEST_TAG", "Je databaza inicializovana? " + databaseService.isDatabaseInitialized(this).toString())
        if (!databaseService.isDatabaseInitialized(this)) {
            Log.i("TEST_TAG", "Inicializujem databazu...")
            databaseService.initializeSQL()
            Log.i("TEST_TAG", "Databaza inicializovana")
        }

        val examsListMenuButton = findViewById<Button>(R.id.button_quizes)
        examsListMenuButton.setOnClickListener {
            val intent = Intent(this, ExamListActivity::class.java)
            startActivity(intent)
        }
        // val exitButton = findViewById<Button>(R.id.button_exit) // Nenastavujem, pretoze nie to odporucane vypinat takto aplikaciu -- nechat to na app lifecycle
    }
}