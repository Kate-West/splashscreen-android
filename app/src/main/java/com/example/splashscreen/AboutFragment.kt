import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.splashscreen.R

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Устанавливаем версию приложения
        setVersionText()

        // Находим кнопку назад
        val backButton = view.findViewById<View>(R.id.backButton)

        // Находим остальные кнопки
        val contactButton = view.findViewById<Button>(R.id.contactButton)
        val shareButton = view.findViewById<Button>(R.id.shareButton)
        val rateButton = view.findViewById<Button>(R.id.rateButton)

        // Обработка кнопки назад
        backButton.setOnClickListener {
            // Легкая анимация нажатия
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

                    // Возвращаемся назад
                    parentFragmentManager.popBackStack()
                }
                .start()
        }

        // Обработка кнопки "Написать разработчику" - ТЕПЕРЬ С ФОРМОЙ
        contactButton.setOnClickListener {
            showFeedbackForm()
        }

        // Обработка кнопки "Поделиться"
        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Посмотрите это крутое приложение ${getAppName()}!")
                putExtra(Intent.EXTRA_SUBJECT, "Рекомендую приложение ${getAppName()}")
            }
            startActivity(Intent.createChooser(shareIntent, "Поделиться через"))
        }

        // Обработка кнопки "Оценить"
        rateButton.setOnClickListener {
            showRatingDialog()
        }
    }

    private fun showFeedbackForm() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_feedback_form, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Находим элементы формы
        val subjectEditText = dialogView.findViewById<EditText>(R.id.subjectEditText)
        val messageEditText = dialogView.findViewById<EditText>(R.id.messageEditText)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val sendButton = dialogView.findViewById<Button>(R.id.sendButton)

        // Обработка кнопки "Отмена"
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Обработка кнопки "Отправить"
        sendButton.setOnClickListener {
            val subject = subjectEditText.text.toString().trim()
            val message = messageEditText.text.toString().trim()

            if (subject.isEmpty() || message.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Закрываем диалог и отправляем email
            dialog.dismiss()
            sendEmailToDeveloper(subject, message)
        }

        dialog.show()
    }

    private fun sendEmailToDeveloper(subject: String, message: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:katty.west.l@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "[${getAppName()}] $subject")
            putExtra(Intent.EXTRA_TEXT,
                """
                Сообщение из приложения ${getAppName()}:
                
                $message
                
                ---
                Версия приложения: ${getAppVersion()}
                """.trimIndent()
            )
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Отправить через"))
            Toast.makeText(requireContext(), "Открываю почтовое приложение...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Почтовое приложение не найдено", Toast.LENGTH_LONG).show()
        }
    }

    private fun showRatingDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rating, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Находим звезды в диалоге
        val star1 = dialogView.findViewById<TextView>(R.id.star1)
        val star2 = dialogView.findViewById<TextView>(R.id.star2)
        val star3 = dialogView.findViewById<TextView>(R.id.star3)
        val star4 = dialogView.findViewById<TextView>(R.id.star4)
        val star5 = dialogView.findViewById<TextView>(R.id.star5)
        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)
        val ratingText = dialogView.findViewById<TextView>(R.id.ratingText)

        val stars = listOf(star1, star2, star3, star4, star5)
        var selectedRating = 0

        // Обработка кликов по звездам
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                updateStars(stars, selectedRating)
                ratingText.text = when (selectedRating) {
                    1 -> "Плохо 😞"
                    2 -> "Не очень 😕"
                    3 -> "Нормально 🙂"
                    4 -> "Хорошо 😊"
                    5 -> "Отлично! 🤩"
                    else -> "Оцените приложение"
                }
            }
        }

        // Обработка кнопки отправки
        submitButton.setOnClickListener {
            if (selectedRating > 0) {
                when (selectedRating) {
                    1, 2 -> showFeedbackDialog(selectedRating, dialog)
                    3, 4, 5 -> {
                        Toast.makeText(
                            requireContext(),
                            "Спасибо за ${selectedRating} звезд! ❤️",
                            Toast.LENGTH_LONG
                        ).show()
                        dialog.dismiss()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Пожалуйста, выберите оценку", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updateStars(stars: List<TextView>, selectedRating: Int) {
        stars.forEachIndexed { index, star ->
            if (index < selectedRating) {
                star.text = "★"
                star.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
            } else {
                star.text = "☆"
                star.setTextColor(requireContext().getColor(android.R.color.darker_gray))
            }
        }
    }

    private fun showFeedbackDialog(rating: Int, ratingDialog: AlertDialog) {
        val feedbackDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_feedback, null)

        val feedbackDialog = AlertDialog.Builder(requireContext())
            .setView(feedbackDialogView)
            .setCancelable(false)
            .create()

        // Находим кнопки в диалоге обратной связи
        val submitFeedbackButton = feedbackDialogView.findViewById<Button>(R.id.submitFeedbackButton)
        val skipButton = feedbackDialogView.findViewById<Button>(R.id.skipButton)

        submitFeedbackButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Спасибо за обратную связь! Мы учтем ваши пожелания 💫",
                Toast.LENGTH_LONG
            ).show()
            ratingDialog.dismiss()
            feedbackDialog.dismiss()
        }

        skipButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Спасибо за честность! Мы будем стараться лучше 🌟",
                Toast.LENGTH_LONG
            ).show()
            ratingDialog.dismiss()
            feedbackDialog.dismiss()
        }

        feedbackDialog.show()
    }

    private fun setVersionText() {
        try {
            val version = getAppVersion()
            val versionText = requireView().findViewById<TextView>(R.id.versionText)
            versionText.text = "Версия $version"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAppVersion(): String {
        return try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            pInfo.versionName ?: "1.0.0" // Если версия null, используем значение по умолчанию
        } catch (e: Exception) {
            "1.0.0" // Если ошибка, возвращаем значение по умолчанию
        }
    }

    private fun getAppName(): String {
        return try {
            val applicationInfo = requireContext().packageManager.getApplicationInfo(requireContext().packageName, 0)
            val appName = requireContext().packageManager.getApplicationLabel(applicationInfo)
            appName?.toString() ?: "Мое приложение" // Если null, используем значение по умолчанию
        } catch (e: Exception) {
            "Мое приложение" // Если ошибка, возвращаем значение по умолчанию
        }
    }
}