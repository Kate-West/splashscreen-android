package com.example.splashscreen

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchNotifications: Switch
    private lateinit var switchSound: Switch
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var avatarImageView: ImageView
    private lateinit var changeAvatarButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("app_settings", 0)

        // Находим все элементы
        val backButton = view.findViewById<View>(R.id.backButton)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchSound = view.findViewById(R.id.switchSound)
        nameEditText = view.findViewById(R.id.nameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        avatarImageView = view.findViewById(R.id.avatarImageView)
        changeAvatarButton = view.findViewById(R.id.changeAvatarButton)
        val saveButton = view.findViewById<Button>(R.id.buttonSave)

        // Устанавливаем круглую форму для аватара
        avatarImageView.background = ContextCompat.getDrawable(requireContext(), R.drawable.circle_avatar_background)
        avatarImageView.clipToOutline = true

        // Загружаем сохраненные настройки
        loadSavedSettings()

        // Обработка кнопки назад
        backButton.setOnClickListener {
            backButton.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    backButton.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    parentFragmentManager.popBackStack()
                }
                .start()
        }

        // Обработка кнопки смены аватара - открываем галерею
        changeAvatarButton.setOnClickListener {
            (requireActivity() as MainActivity).openGalleryForAvatar()
        }

        // Обработка сохранения настроек
        saveButton.setOnClickListener {
            saveSettings()
            saveButton.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    saveButton.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    Toast.makeText(requireContext(), "Настройки сохранены! ✅", Toast.LENGTH_SHORT).show()
                }
                .start()
        }

        // Обработка переключателей
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "включены" else "выключены"
            Toast.makeText(requireContext(), "Уведомления $status", Toast.LENGTH_SHORT).show()
        }

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "включен" else "выключен"
            Toast.makeText(requireContext(), "Звук $status", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем аватар при возвращении на фрагмент
        updateAvatar()
    }

    // Загрузка сохраненных настроек
    private fun loadSavedSettings() {
        // Настройки уведомлений
        switchNotifications.isChecked = sharedPreferences.getBoolean("notifications_enabled", true)
        switchSound.isChecked = sharedPreferences.getBoolean("sound_enabled", true)

        // Данные профиля
        val userName = (requireActivity() as MainActivity).getUserName()
        val userEmail = (requireActivity() as MainActivity).getUserEmail()

        nameEditText.setText(userName)
        emailEditText.setText(userEmail)

        // Загружаем аватар
        updateAvatar()
    }

    // Сохранение настроек
    private fun saveSettings() {
        val editor = sharedPreferences.edit()

        // Настройки уведомлений
        editor.putBoolean("notifications_enabled", switchNotifications.isChecked)
        editor.putBoolean("sound_enabled", switchSound.isChecked)
        editor.apply()

        // Данные профиля сохраняем через MainActivity
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        (requireActivity() as MainActivity).updateUserProfile(name, email)
    }

    // Обновление аватара
    fun updateAvatar() {
        val avatarBitmap = (requireActivity() as MainActivity).getAvatarBitmap()
        if (avatarBitmap != null) {
            avatarImageView.setImageBitmap(avatarBitmap)
        } else {
            avatarImageView.setImageResource(R.drawable.logo)
        }
    }

    fun updateAvatar(bitmap: Bitmap) {
        avatarImageView.setImageBitmap(bitmap)
    }
}