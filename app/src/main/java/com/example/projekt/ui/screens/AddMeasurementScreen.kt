package com.example.projekt.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.projekt.ui.model.Pomiar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddMeasurementScreen(
    onMeasurementSaved: () -> Unit,
    onCancelClick: () -> Unit
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var skurczowe by remember { mutableStateOf("") }
    var rozkurczowe by remember { mutableStateOf("") }
    var puls by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Dodaj pomiar", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            DatePickerDialog(context, { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }) {
            Text(if (selectedDate.isEmpty()) "Wybierz datę" else selectedDate)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            TimePickerDialog(context, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                selectedTime = String.format("%02d:%02d", hour, minute)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }) {
            Text(if (selectedTime.isEmpty()) "Wybierz godzinę" else selectedTime)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = skurczowe,
            onValueChange = { skurczowe = it },
            label = { Text("Ciśnienie skurczowe") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = rozkurczowe,
            onValueChange = { rozkurczowe = it },
            label = { Text("Ciśnienie rozkurczowe") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = puls,
            onValueChange = { puls = it },
            label = { Text("Puls") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (userId == null) {
                    Toast.makeText(context, "Brak użytkownika", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val pomiar = Pomiar(
                    id = UUID.randomUUID().toString(),
                    data = "$selectedDate $selectedTime",
                    cisnienieSkurczowe = skurczowe.toIntOrNull() ?: 0,
                    cisnienieRozkurczowe = rozkurczowe.toIntOrNull() ?: 0,
                    puls = puls.toIntOrNull() ?: 0,
                    userId = userId
                )

                firestore.collection("pomiary").document(pomiar.id)
                    .set(pomiar)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Zapisano pomiar", Toast.LENGTH_SHORT).show()
                        onMeasurementSaved()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Błąd zapisu", Toast.LENGTH_SHORT).show()
                    }
            }) {
                Text("Zapisz")
            }

            OutlinedButton(onClick = onCancelClick) {
                Text("Anuluj")
            }
        }
    }
}
