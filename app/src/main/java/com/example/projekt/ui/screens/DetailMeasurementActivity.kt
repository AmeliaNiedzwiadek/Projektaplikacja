// app/src/main/java/com/example/projekt/ui/screens/DetailMeasurementActivity.kt
package com.example.projekt.ui.screens

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.R
import com.example.projekt.ui.model.Pomiar
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.widget.Toast

class DetailMeasurementActivity : AppCompatActivity() {

    private lateinit var dataText: TextView
    private lateinit var cisnienieText: TextView
    private lateinit var pulsText: TextView
    private val TAG = "DetailMeasurement"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_measurement)

        dataText = findViewById(R.id.textDataDetail)
        cisnienieText = findViewById(R.id.textCisnienieDetail)
        pulsText = findViewById(R.id.textPulsDetail)

        val pomiarId = intent.getStringExtra("pomiar_id")
        if (pomiarId != null) {
            FirebaseFirestore.getInstance().collection("pomiary").document(pomiarId)
                .get()
                .addOnSuccessListener { doc ->
                    val pomiar = doc.toObject(Pomiar::class.java)
                    if (pomiar != null) {
                        dataText.text = "Data: ${pomiar.data}"
                        cisnienieText.text = "Ciśnienie: ${pomiar.cisnienieSkurczowe}/${pomiar.cisnienieRozkurczowe}"
                        pulsText.text = "Puls: ${pomiar.puls}"
                    } else {
                        Toast.makeText(this, "Nie znaleziono pomiaru", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Błąd wczytywania: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Błąd DetailMeasurement", e)
                }
        } else {
            Toast.makeText(this, "Brak ID pomiaru", Toast.LENGTH_SHORT).show()
        }
    }
}
