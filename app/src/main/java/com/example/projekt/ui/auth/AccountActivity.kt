package com.example.projekt.ui.auth

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountActivity : AppCompatActivity() {

    private lateinit var textViewEmail: TextView
    private lateinit var spinnerGender: Spinner
    private lateinit var editTextWeight: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var editTextAge: EditText
    private lateinit var editTextNotes: EditText
    private lateinit var buttonEditSave: Button
    private lateinit var buttonResetPassword: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val genderOptions = listOf("Kobieta", "Mężczyzna", "Inne")

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        textViewEmail = findViewById(R.id.textViewEmail)
        spinnerGender = findViewById(R.id.spinnerGender)
        editTextWeight = findViewById(R.id.editTextWeight)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextAge = findViewById(R.id.editTextAge)
        editTextNotes = findViewById(R.id.editTextNotes)
        buttonEditSave = findViewById(R.id.buttonEditSave)
        buttonResetPassword = findViewById(R.id.buttonResetPassword)

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Nie jesteś zalogowany", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        textViewEmail.text = user.email ?: ""

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = adapter

        loadUserData(user.uid)

        setEditingEnabled(false)
        buttonEditSave.text = getString(R.string.edit_button)

        buttonEditSave.setOnClickListener {
            if (isEditing) {
                saveUserData(user.uid)
            } else {
                setEditingEnabled(true)
                buttonEditSave.text = getString(R.string.save_button)
                isEditing = true
            }
        }

        buttonResetPassword.setOnClickListener {
            val email = user.email
            if (email != null) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.password_reset_sent, email), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, getString(R.string.password_reset_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setEditingEnabled(enabled: Boolean) {
        spinnerGender.isEnabled = enabled
        editTextWeight.isEnabled = enabled
        editTextHeight.isEnabled = enabled
        editTextAge.isEnabled = enabled
        editTextNotes.isEnabled = enabled
    }

    private fun loadUserData(userId: String) {
        firestore.collection("users").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                spinnerGender.setSelection(genderOptions.indexOf(doc.getString("gender") ?: "Inne"))
                editTextWeight.setText(doc.getDouble("weight")?.toString() ?: "")
                editTextHeight.setText(doc.getLong("height")?.toString() ?: "")
                editTextAge.setText(doc.getLong("age")?.toString() ?: "")
                editTextNotes.setText(doc.getString("notes") ?: "")
            }
        }.addOnFailureListener {
            Toast.makeText(this, getString(R.string.data_load_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData(userId: String) {
        val gender = spinnerGender.selectedItem.toString()
        val weight = editTextWeight.text.toString().toDoubleOrNull()
        val height = editTextHeight.text.toString().toIntOrNull()
        val age = editTextAge.text.toString().toIntOrNull()
        val notes = editTextNotes.text.toString()

        if (weight == null || height == null || age == null) {
            Toast.makeText(this, "Wprowadź poprawne dane liczbowe", Toast.LENGTH_SHORT).show()
            return
        }

        if (weight !in 1.0..300.0 || height !in 30..300 || age !in 1..100) {
            Toast.makeText(
                this,
                "Wprowadź realistyczne dane:\n- Waga: < 300 kg\n- Wzrost: < 300 cm\n- Wiek: < 100 lat",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val data = hashMapOf(
            "gender" to gender,
            "weight" to weight,
            "height" to height,
            "age" to age,
            "notes" to notes
        )

        firestore.collection("users").document(userId).set(data).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.data_saved), Toast.LENGTH_SHORT).show()
            setEditingEnabled(false)
            buttonEditSave.text = getString(R.string.edit_button)
            isEditing = false
        }.addOnFailureListener {
            Toast.makeText(this, getString(R.string.data_save_error), Toast.LENGTH_SHORT).show()
        }
    }
}
