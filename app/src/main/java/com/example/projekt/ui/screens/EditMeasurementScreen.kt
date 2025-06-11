package com.example.projekt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projekt.ui.model.Pomiar

@Composable
fun EditMeasurementScreen(
    pomiar: Pomiar,
    onSaveClick: (updatedPomiar: Pomiar) -> Unit,
    onCancelClick: () -> Unit
) {
    var data by remember { mutableStateOf(pomiar.data) }
    var skurczowe by remember { mutableStateOf(pomiar.cisnienieSkurczowe.toString()) }
    var rozkurczowe by remember { mutableStateOf(pomiar.cisnienieRozkurczowe.toString()) }
    var puls by remember { mutableStateOf(pomiar.puls.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Edytuj pomiar", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = data,
            onValueChange = { data = it },
            label = { Text("Data") },
            modifier = Modifier.fillMaxWidth()
        )
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                val updatedPomiar = pomiar.copy(
                    data = data,
                    cisnienieSkurczowe = skurczowe.toIntOrNull() ?: 0,
                    cisnienieRozkurczowe = rozkurczowe.toIntOrNull() ?: 0,
                    puls = puls.toIntOrNull() ?: 0
                )
                onSaveClick(updatedPomiar)
            }) {
                Text("Zapisz")
            }
            OutlinedButton(onClick = onCancelClick) {
                Text("Anuluj")
            }
        }
    }
}
