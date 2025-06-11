package com.example.projekt

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projekt.ui.auth.LoginScreen
import com.example.projekt.ui.auth.RegisterScreen
import com.example.projekt.ui.model.Pomiar
import com.example.projekt.ui.navigation.Routes
import com.example.projekt.ui.screens.*
import com.example.projekt.ui.theme.ProjektTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProjektTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()

                fun loadPomiary(userId: String) {
                    firestore.collection("pomiary")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { documents ->
                            Log.d("Firestore", "Pobrano ${documents.size()} pomiarów.")
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Błąd pobierania danych", Toast.LENGTH_SHORT).show()
                        }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.LOGIN
                    ) {
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                onLoginClick = { email, password ->
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val userId = auth.currentUser?.uid
                                                if (userId != null) {
                                                    loadPomiary(userId)
                                                }
                                                Toast.makeText(context, "Zalogowano!", Toast.LENGTH_SHORT).show()
                                                navController.navigate(Routes.HOME) {
                                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Błąd logowania: ${task.exception?.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                },
                                onRegisterClick = {
                                    navController.navigate(Routes.REGISTER)
                                }
                            )
                        }

                        composable(Routes.REGISTER) {
                            RegisterScreen(
                                onRegisterClick = { email, password, confirmPassword ->
                                    val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
                                    if (password != confirmPassword) {
                                        Toast.makeText(context, "Hasła nie są takie same", Toast.LENGTH_SHORT).show()
                                        return@RegisterScreen
                                    }

                                    if (!passwordPattern.matches(password)) {
                                        Toast.makeText(
                                            context,
                                            "Hasło musi mieć min. 8 znaków, wielką literę i cyfrę",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@RegisterScreen
                                    }

                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(context, "Rejestracja udana!", Toast.LENGTH_SHORT).show()
                                                navController.navigate(Routes.LOGIN) {
                                                    popUpTo(Routes.REGISTER) { inclusive = true }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Błąd rejestracji: ${task.exception?.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                },
                                onBackToLoginClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Routes.HOME) {
                            val userEmail = auth.currentUser?.email ?: "Nieznany"

                            HomeScreen(
                                userEmail = userEmail,
                                onPomiarClick = { pomiar ->
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("pomiar", pomiar)
                                    navController.navigate(Routes.EDIT_MEASUREMENT)
                                },
                                onAddClick = {
                                    navController.navigate(Routes.ADD_MEASUREMENT)
                                },
                                onLogoutClick = {
                                    auth.signOut()
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(Routes.HOME) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Routes.ADD_MEASUREMENT) {
                            AddMeasurementScreen(
                                onCancelClick = {
                                    navController.popBackStack()
                                },
                                onMeasurementSaved = {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        loadPomiary(userId)
                                    }
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Routes.EDIT_MEASUREMENT) {
                            val pomiar = navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.get<Pomiar>("pomiar")

                            pomiar?.let {
                                EditMeasurementScreen(
                                    pomiar = it,
                                    onSaveClick = { updated ->
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            loadPomiary(userId)
                                        }
                                        navController.popBackStack()
                                    },
                                    onCancelClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
