package com.example.projekt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projekt.ui.model.Pomiar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(
    userEmail: String,
    onLogoutClick: () -> Unit,
    onPomiarClick: (Pomiar) -> Unit,
    onAddClick: () -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()
    val pomiary = remember { mutableStateListOf<Pomiar>() }

    // 🚀 Pobieranie danych z Firestore
    LaunchedEffect(userId) {
        userId?.let {
            firestore.collection("pomiary")
                .whereEqualTo("userId", it)
                .get()
                .addOnSuccessListener { result ->
                    pomiary.clear()
                    for (doc in result) {
                        val pomiar = doc.toObject(Pomiar::class.java)
                        pomiary.add(pomiar)
                    }
                }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Witaj, $userEmail!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(pomiary) { pomiar ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onPomiarClick(pomiar) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Data: ${pomiar.data}")
                            Text("Ciśnienie: ${pomiar.cisnienieSkurczowe}/${pomiar.cisnienieRozkurczowe}")
                            Text("Puls: ${pomiar.puls}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Wyloguj się")
            }
        }
    }
}

