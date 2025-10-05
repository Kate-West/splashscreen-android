package com.example.splashscreen

import AboutFragment
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavigationView: BottomNavigationView
    private var isDestroyed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isDestroyed = false
        sharedPreferences = getSharedPreferences("app_settings", 0)

        setupBackPressedHandler()
        setupNavigation()

        if (savedInstanceState == null) {
            showFragment(NewsFragment(), false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isDestroyed = true
    }

    private fun setupNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (isDestroyed) return@setOnItemSelectedListener false

            when (item.itemId) {
                R.id.navigation_news -> {
                    showFragment(NewsFragment(), false)
                    true
                }
                R.id.navigation_profile -> {
                    openProfile()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isDestroyed) return

                val currentFragment = supportFragmentManager.findFragmentByTag("PROFILE")
                if (currentFragment != null) {
                    closeProfile()
                } else {
                    if (supportFragmentManager.backStackEntryCount == 0) {
                        finish()
                    } else {
                        supportFragmentManager.popBackStack()
                    }
                }
            }
        })
    }

    // Методы для работы с данными профиля
    fun getUserName(): String {
        return sharedPreferences.getString("user_name", "Ваше Имя") ?: "Ваше Имя"
    }

    fun getUserEmail(): String {
        return sharedPreferences.getString("user_email", "user@example.com") ?: "user@example.com"
    }

    fun getAvatarBitmap(): Bitmap? {
        val avatarString = sharedPreferences.getString("user_avatar", null)
        return if (avatarString != null) {
            decodeBase64(avatarString)
        } else {
            null
        }
    }

    fun updateUserProfile(name: String, email: String) {
        if (isDestroyed) return

        sharedPreferences.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .apply()

        updateProfileFragments()
    }

    // Метод для открытия галереи
    fun openGalleryForAvatar() {
        pickImageLauncher.launch("image/*")
    }

    private fun updateProfileFragments() {
        if (isDestroyed) return

        val currentProfileFragment = supportFragmentManager.findFragmentByTag("PROFILE")
        if (currentProfileFragment != null && currentProfileFragment is ProfileFragment) {
            currentProfileFragment.updateProfileData()
        }
    }

    private fun updateAvatarInAllFragments(bitmap: Bitmap) {
        if (isDestroyed) return

        // Обновляем аватар в профиле
        val currentProfileFragment = supportFragmentManager.findFragmentByTag("PROFILE")
        if (currentProfileFragment != null && currentProfileFragment is ProfileFragment) {
            currentProfileFragment.updateAvatar(bitmap)
        }

        // Обновляем аватар в настройках
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is SettingsFragment) {
                fragment.updateAvatar(bitmap)
            }
        }
    }

    private fun saveAvatarToPreferences(bitmap: Bitmap) {
        val avatarString = encodeToBase64(bitmap)
        sharedPreferences.edit().putString("user_avatar", avatarString).apply()
    }

    // Создаем квадратное изображение для круга
    private fun createSquareBitmap(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2

        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    private fun encodeToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun decodeBase64(input: String): Bitmap {
        val decodedBytes = Base64.decode(input, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    // Метод для открытия профиля
    fun openProfile() {
        if (isDestroyed) return

        val existingProfile = supportFragmentManager.findFragmentByTag("PROFILE")
        if (existingProfile != null) {
            showFragment(existingProfile, true)
            return
        }

        val profileFragment = ProfileFragment()
        showFragment(profileFragment, true, "PROFILE")
    }

    // Метод для открытия настроек
    fun openSettings() {
        if (isDestroyed) return

        val settingsFragment = SettingsFragment()
        showFragment(settingsFragment, true, "SETTINGS")
    }

    // Метод для открытия "О приложении"
    fun openAbout() {
        if (isDestroyed) return

        val aboutFragment = AboutFragment()
        showFragment(aboutFragment, true, "ABOUT")
    }

    // Метод для закрытия профиля
    fun closeProfile() {
        if (isDestroyed) return

        supportFragmentManager.popBackStack()
    }

    private fun showFragment(fragment: Fragment, addToBackStack: Boolean = false, tag: String? = null) {
        if (isDestroyed) return

        try {
            val transaction = supportFragmentManager.beginTransaction()

            if (addToBackStack) {
                transaction.addToBackStack(tag)
            }

            transaction.replace(R.id.fragment_container, fragment, tag)
            transaction.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    // Создаем идеально круглый bitmap
                    val circularBitmap = createCircularBitmap(bitmap)
                    saveAvatarToPreferences(circularBitmap)
                    updateAvatarInAllFragments(circularBitmap)
                    Toast.makeText(this, "Аватар успешно изменен! 📸", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Создаем идеально круглое изображение
    private fun createCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2

        // Сначала обрезаем до квадрата
        val squaredBitmap = Bitmap.createBitmap(bitmap, x, y, size, size)

        // Создаем круглый bitmap
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, size, size)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(squaredBitmap, rect, rect, paint)

        return output
    }

    // Сжимаем изображение для хранения
    private fun compressBitmapForStorage(bitmap: Bitmap): Bitmap {
        val maxSize = 400
        return Bitmap.createScaledBitmap(bitmap, maxSize, maxSize, true)
    }
}