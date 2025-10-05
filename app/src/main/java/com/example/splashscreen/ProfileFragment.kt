package com.example.splashscreen

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var avatarImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем элементы
        userNameTextView = view.findViewById(R.id.userNameTextView)
        userEmailTextView = view.findViewById(R.id.userEmailTextView)
        avatarImageView = view.findViewById(R.id.avatarImageView)

        // Устанавливаем круглую форму
        avatarImageView.background = ContextCompat.getDrawable(requireContext(), R.drawable.circle_avatar_background)
        avatarImageView.clipToOutline = true

        // Обновляем данные при создании
        updateProfileData()

        // Обработка пунктов меню
        view.findViewById<View>(R.id.profileItem).setOnClickListener {
            Toast.makeText(requireContext(), "Открыть профиль", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.settingsItem).setOnClickListener {
            (requireActivity() as MainActivity).openSettings()
        }

        view.findViewById<View>(R.id.aboutItem).setOnClickListener {
            (requireActivity() as MainActivity).openAbout()
        }

        view.findViewById<View>(R.id.exitItem).setOnClickListener {
            Toast.makeText(requireContext(), "Выход из приложения", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные при возвращении на фрагмент
        updateProfileData()
    }

    // Обновление данных профиля
    fun updateProfileData() {
        if (!isAdded || activity == null) return

        val userName = (requireActivity() as MainActivity).getUserName()
        val userEmail = (requireActivity() as MainActivity).getUserEmail()

        userNameTextView.text = userName
        userEmailTextView.text = userEmail

        // Обновляем аватар
        updateAvatar()
    }

    // Обновление аватара
    fun updateAvatar() {
        val avatarBitmap = (requireActivity() as MainActivity).getAvatarBitmap()
        if (avatarBitmap != null) {
            avatarImageView.setImageBitmap(avatarBitmap)
        } else {
            // Если аватар не установлен, используем стандартный
            avatarImageView.setImageResource(R.drawable.logo)
        }
    }

    fun updateAvatar(bitmap: Bitmap) {
        avatarImageView.setImageBitmap(bitmap)
    }
}