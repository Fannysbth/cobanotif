# Obat Reminder App 🕒💊

Aplikasi Android untuk mengingatkan pengguna agar tidak lupa minum obat sesuai jadwal. Notifikasi akan dikirim pada waktu yang ditentukan, dan pengguna bisa mencatat jumlah konsumsi obat.

---

## ✨ Fitur Utama

- ✅ Registrasi dan login menggunakan Firebase Authentication
- ✅ Menambahkan nama obat, jumlah, dan waktu minum (2 waktu per hari)
- ✅ Notifikasi otomatis saat waktu minum tiba (via AlarmManager & Firebase Cloud Messaging)
- ✅ Flashcard untuk menunjukkan nama obat dan sisa jumlah minum
- ✅ Data tersimpan di Firebase Firestore
- ✅ Mendukung Android 13+ (izin notifikasi otomatis diminta)

---

## 📱 Tampilan Layar

- **Login & Register** (satu layar): input email dan password
- **Input Obat**: nama obat, jumlah total, dan 2 waktu pengingat
- **Notifikasi**: muncul otomatis saat waktu minum tiba
- **Flashcard**: menampilkan nama obat, sisa jumlah, dan tombol `Done` atau `Skip`

---

## 🧑‍💻 Teknologi yang Digunakan

- Kotlin
- Firebase Authentication
- Firebase Firestore
- Firebase Cloud Messaging (FCM)
- Android AlarmManager
- XML Layout (LinearLayout)

---

## 🚀 Instalasi dan Menjalankan Aplikasi

1. **Clone repositori ini:**
```bash
git clone https://github.com/namakamu/obat-reminder.git
cd obat-reminder
```
2. **Buka di Android Studio**
3. **Pasang konfigurasi Firebase:**
- Tambahkan file google-services.json di folder app/**
- Aktifkan Firebase Authentication dan Firestore di Firebase Console
4. **Build & jalankan di emulator atau HP Android**

---

## 🔐 Akun Demo Login (Testing)

Gunakan akun berikut untuk mencoba aplikasi tanpa perlu registrasi:
```markdown
📧 Email: cobanotif@example.com
🔑 Password: 123456
```
Jika akun tidak tersedia, silakan gunakan fitur **Register** di aplikasi untuk membuat akun baru.

---

## 🔐 Permissions

1. Android 13+ akan meminta izin notifikasi (POST_NOTIFICATIONS)
2. AlarmManager digunakan untuk menjadwalkan reminder

