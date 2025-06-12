package com.example.projekt.ui.home

import com.example.projekt.ui.model.Pomiar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt.R
import com.example.projekt.ui.auth.AccountActivity
import com.example.projekt.ui.notifications.NotificationsSettingsActivity
import com.example.projekt.ui.notifications.ReminderReceiver
import com.example.projekt.ui.screens.AddMeasurementActivity
import com.example.projekt.ui.screens.DetailMeasurementActivity
import com.example.projekt.ui.screens.EditMeasurementActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.activity.result.contract.ActivityResultContracts

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MeasurementAdapter
    private lateinit var addButton: Button

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val addMeasurementLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadMeasurements()
            }
        }

    private val editMeasurementLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadMeasurements()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerView)
        addButton = findViewById(R.id.buttonAdd)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_account -> {
                    startActivity(Intent(this, AccountActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_measurements -> {
                    Toast.makeText(this, "Pomiary", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_export_csv -> {
                    Toast.makeText(this, "Eksport CSV", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsSettingsActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, AccountActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        adapter = MeasurementAdapter(
            items = mutableListOf(),
            onItemClick = { pomiar: Pomiar ->
                val intent = Intent(this, DetailMeasurementActivity::class.java)
                intent.putExtra("pomiar_id", pomiar.id)
                startActivity(intent)
            },
            onEditClick = { pomiar: Pomiar, _position: Int ->
                val intent = Intent(this, EditMeasurementActivity::class.java)
                intent.putExtra("pomiar_id", pomiar.id)
                editMeasurementLauncher.launch(intent)
            },
            onDeleteClick = { pomiar: Pomiar, _position: Int ->
                val docId = pomiar.id
                if (docId.isNullOrEmpty()) {
                    Toast.makeText(this, "Niepoprawny ID pomiaru", Toast.LENGTH_SHORT).show()
                    return@MeasurementAdapter
                }
                firestore.collection("pomiary").document(docId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usunięto pomiar", Toast.LENGTH_SHORT).show()
                        loadMeasurements()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Błąd przy usuwaniu pomiaru", Toast.LENGTH_SHORT).show()
                    }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadMeasurements()

        addButton.setOnClickListener {
            val intent = Intent(this, AddMeasurementActivity::class.java)
            addMeasurementLauncher.launch(intent)
        }

        scheduleDailyReminder(9, 0)
    }

    private fun loadMeasurements() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "Nie jesteś zalogowany", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("pomiary")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val newList = mutableListOf<Pomiar>()
                for (doc in documents) {
                    val pomiar = doc.toObject(Pomiar::class.java)
                    pomiar.id = doc.id
                    newList.add(pomiar)
                }
                adapter.updateData(newList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Błąd podczas wczytywania pomiarów", Toast.LENGTH_SHORT).show()
            }
    }

    private fun scheduleDailyReminder(hour: Int, minute: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
