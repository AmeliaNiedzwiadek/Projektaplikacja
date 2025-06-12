// app/src/main/java/com/example/projekt/ui/screens/EditMeasurementActivity.kt
package com.example.projekt.ui.screens

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.R
import com.example.projekt.ui.model.Pomiar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class EditMeasurementActivity : AppCompatActivity() {

    private lateinit var dataEdit: EditText
    private lateinit var skurczoweEdit: EditText
    private lateinit var rozkurczoweEdit: EditText
    private lateinit var pulsEdit: EditText
    private lateinit var saveButton: Button

    private val firestore = FirebaseFirestore.getInstance()
    private var pomiarId: String? = null
    private val TAG = "EditMeasurement"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_measurement)

        dataEdit = findViewById(R.id.editData)
        skurczoweEdit = findViewById(R.id.editSkurczowe)
        rozkurczoweEdit = findViewById(R.id.editRozkurczowe)
        pulsEdit = findViewById(R.id.editPuls)
        saveButton = findViewById(R.id.buttonSave)

        pomiarId = intent.getStringExtra("pomiar_id")
        loadPomiar()

        saveButton.setOnClickListener { updatePomiar() }
    }

    private fun loadPomiar() {
        val id = pomiarId ?: return
        firestore.collection("pomiary").document(id).get()
            .addOnSuccessListener { doc ->
                val pomiar = doc.toObject(Pomiar::class.java) ?: return@addOnSuccessListener
                dataEdit.setText(pomiar.data)
                skurczoweEdit.setText(pomiar.cisnienieSkurczowe.toString())
                rozkurczoweEdit.setText(pomiar.cisnienieRozkurczowe.toString())
                pulsEdit.setText(pomiar.puls.toString())
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Błąd wczytywania pomiaru: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Błąd loadPomiar", e)
            }
    }

    private fun updatePomiar() {
        val id = pomiarId ?: return
        val newData = dataEdit.text.toString().trim()
        val newSk = skurczoweEdit.text.toString().toIntOrNull()
        val newRoz = rozkurczoweEdit.text.toString().toIntOrNull()
        val newPuls = pulsEdit.text.toString().toIntOrNull()

        if (newData.isBlank() || newSk == null || newRoz == null || newPuls == null) {
            Toast.makeText(this, "Uzupełnij wszystkie pola!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedMap = mapOf(
            "data" to newData,
            "cisnienieSkurczowe" to newSk,
            "cisnienieRozkurczowe" to newRoz,
            "puls" to newPuls
        )

        firestore.collection("pomiary").document(id)
            .update(updatedMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Błąd zapisu: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Błąd updatePomiar", e)
            }
    }
}
