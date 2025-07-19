package com.example.obatreminder

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var reminderQueue: Queue<Pair<String, String>> = LinkedList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        createNotificationChannel()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        getFCMToken()
                        setupObatUI()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Login gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Registrasi berhasil. Silakan login.", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Gagal registrasi: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
            }
        }

        intent.getStringExtra("medicineName")?.let {
            showFlashcard(it)
        }
    }

    private fun setupObatUI() {
        setContentView(R.layout.activity_input_obat)

        val nameEditText = findViewById<EditText>(R.id.obatNameEditText)
        val jumlahEditText = findViewById<EditText>(R.id.jumlahEditText)
        val timePicker1 = findViewById<TimePicker>(R.id.timePicker1)
        val timePicker2 = findViewById<TimePicker>(R.id.timePicker2)
        val addButton = findViewById<Button>(R.id.addObatButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        timePicker1.setIs24HourView(true)
        timePicker2.setIs24HourView(true)

        addButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val jumlah = jumlahEditText.text.toString().toIntOrNull() ?: 0
            if (name.isNotEmpty() && jumlah > 0) {
                val time1 = Pair(timePicker1.hour, timePicker1.minute)
                val time2 = Pair(timePicker2.hour, timePicker2.minute)
                addMedication(name, jumlah, time1, time2)
            }
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            recreate()
        }
    }

    private fun addMedication(name: String, jumlah: Int, time1: Pair<Int, Int>, time2: Pair<Int, Int>) {
        val userId = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "name" to name,
            "jumlah" to jumlah,
            "time1" to "%02d:%02d".format(time1.first, time1.second),
            "time2" to "%02d:%02d".format(time2.first, time2.second)
        )
        db.collection("users").document(userId).collection("medications")
            .add(data)
            .addOnSuccessListener {
                scheduleReminder(time1.first, time1.second, name)
                scheduleReminder(time2.first, time2.second, name)
                Toast.makeText(this, "Obat ditambahkan & pengingat disetel", Toast.LENGTH_SHORT).show()
            }
    }

    private fun scheduleReminder(hour: Int, minute: Int, medicineName: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("title", "Waktunya minum obat")
            putExtra("medicineName", medicineName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            (medicineName + hour + minute).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun showFlashcard(medicineName: String) {
        setContentView(R.layout.activity_flashcard)
        val text = findViewById<TextView>(R.id.medicineNameTextView)
        val countText = findViewById<TextView>(R.id.countTextView)
        val doneBtn = findViewById<Button>(R.id.doneButton)
        val skipBtn = findViewById<Button>(R.id.skipButton)
        text.text = medicineName

        val userId = auth.currentUser?.uid ?: return
        val medsRef = db.collection("users").document(userId).collection("medications")
        medsRef.whereEqualTo("name", medicineName).get().addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                val jumlah = doc.getLong("jumlah")?.toInt() ?: 0
                countText.text = "Sisa: $jumlah kali"

                doneBtn.setOnClickListener {
                    if (jumlah > 1) {
                        medsRef.document(doc.id).update("jumlah", jumlah - 1)
                    } else {
                        medsRef.document(doc.id).delete()
                    }
                    showNextFlashcard()
                }

                skipBtn.setOnClickListener { showNextFlashcard() }
            }
        }
    }

    private fun showNextFlashcard() {
        if (reminderQueue.isNotEmpty()) {
            val next = reminderQueue.poll()
            showFlashcard(next.first)
        } else {
            setupObatUI()
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
            db.collection("users").document(userId)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Obat Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
