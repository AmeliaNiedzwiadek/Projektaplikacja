// app/src/main/java/com/example/projekt/ui/screens/AddMeasurementActivity.kt
package com.example.projekt.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.R
import com.example.projekt.ui.model.Pomiar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

class AddMeasurementActivity : AppCompatActivity() {

    private lateinit var dataInput: EditText
    private lateinit var skurczoweInput: EditText
    private lateinit var rozkurczoweInput: EditText
    private lateinit var pulsInput: EditText
    private lateinit var saveButton: Button

    private val calendar = Calendar.getInstance()
    private val TAG = "AddMeasurement"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_measurement)

        dataInput = findViewById(R.id.inputData)
        skurczoweInput = findViewById(R.id.inputSkurczowe)
        rozkurczoweInput = findViewById(R.id.inputRozkurczowe)
        pulsInput = findViewById(R.id.inputPuls)
        saveButton = findViewById(R.id.buttonSaveMeasurement)

        updateDateTimeInput()
        dataInput.setOnClickListener { showDateTimePicker() }

        saveButton.setOnClickListener { saveMeasurement() }
    }

    private fun updateDateTimeInput() {
        val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        dataInput.setText(format.format(calendar.time))
    }

    private fun showDateTimePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this, { _, y, m, d ->
            calendar.set(Calendar.YEAR, y)
            calendar.set(Calendar.MONTH, m)
            calendar.set(Calendar.DAY_OF_MONTH, d)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, h, min ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, min)
                updateDateTimeInput()
            }, hour, minute, true).show()
        }, year, month, day).show()
    }

    private fun saveMeasurement() {
        val data = dataInput.text.toString().trim()
        val skurczowe = skurczoweInput.text.toString().toIntOrNull()
        val rozkurczowe = rozkurczoweInput.text.toString().toIntOrNull()
        val puls = pulsInput.text.toString().toIntOrNull()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Użytkownik nie jest zalogowany!", Toast.LENGTH_SHORT).show()
            return
        }
        if (data.isBlank() || skurczowe == null || rozkurczowe == null || puls == null) {
            Toast.makeText(this, "Uzupełnij wszystkie dane!", Toast.LENGTH_SHORT).show()
            return
        }

        val pomiar = Pomiar(
            id = null,
            data = data,
            cisnienieSkurczowe = skurczowe,
            cisnienieRozkurczowe = rozkurczowe,
            puls = puls,
            userId = userId
        )

        val db = FirebaseFirestore.getInstance()
        val documentRef = db.collection("pomiary").document()
        pomiar.id = documentRef.id

        documentRef.set(pomiar)
            .addOnSuccessListener {
                Toast.makeText(this, "Pomiar zapisany!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Błąd zapisu pomiaru: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Błąd zapisu pomiaru", e)
            }
    }
}
