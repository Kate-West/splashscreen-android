package com.example.splashscreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

class NewsFragment : Fragment() {

    private lateinit var notificationStatusText: TextView
    private lateinit var soundStatusText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val refreshButton = view.findViewById<Button>(R.id.refreshButton)
        notificationStatusText = view.findViewById(R.id.notificationStatusText)
        soundStatusText = view.findViewById(R.id.soundStatusText)

        // Показываем текущий статус настроек при загрузке
        updateSettingsDisplay()

        refreshButton.setOnClickListener {
            val message = checkNotificationSettings()
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            // Обновляем отображение статуса
            updateSettingsDisplay()
        }
    }

    private fun checkNotificationSettings(): String {
        val sharedPreferences = requireContext().getSharedPreferences("app_settings", 0)
        val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        val soundEnabled = sharedPreferences.getBoolean("sound_enabled", true)

        return if (notificationsEnabled) {
            if (soundEnabled) {
                "🔔 Новости обновлены (со звуком)"
            } else {
                "📢 Новости обновлены (без звука)"
            }
        } else {
            "📵 Новости обновлены (уведомления выключены)"
        }
    }

    private fun updateSettingsDisplay() {
        val sharedPreferences = requireContext().getSharedPreferences("app_settings", 0)
        val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        val soundEnabled = sharedPreferences.getBoolean("sound_enabled", true)

        // Обновляем текстовые поля со статусом
        notificationStatusText.text = if (notificationsEnabled) {
            "🔔 Уведомления: ВКЛ"
        } else {
            "📵 Уведомления: ВЫКЛ"
        }

        soundStatusText.text = if (soundEnabled) {
            "🔊 Звук: ВКЛ"
        } else {
            "🔇 Звук: ВЫКЛ"
        }

        // Используем цвета из ресурсов
        val activeColor = ContextCompat.getColor(requireContext(), R.color.active_green)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.inactive_red)

        notificationStatusText.setTextColor(
            if (notificationsEnabled) activeColor else inactiveColor
        )

        soundStatusText.setTextColor(
            if (soundEnabled) activeColor else inactiveColor
        )
    }
}