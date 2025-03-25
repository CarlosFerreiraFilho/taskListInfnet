package br.edu.infnet.atv.one.tasklist.ui.fragments

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import br.edu.infnet.atv.one.tasklist.R
import com.google.android.material.textfield.TextInputEditText

class PasswordStrengthView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val passwordEditText: TextInputEditText
    private val passwordStrengthIndicator: TextView

    init {
        inflate(context, R.layout.password_strength_view, this)
        passwordEditText = findViewById(R.id.edit_password)
        passwordStrengthIndicator = findViewById(R.id.password_strength_indicator)

        passwordEditText.addTextChangedListener {
            val password = passwordEditText.text.toString()
            val strength = getPasswordStrength(password)

            Log.d("TAG", "Valor da senha: ${context.getString(strength)}")
            passwordStrengthIndicator.text = context.getString(strength)
        }
    }

    private fun getPasswordStrength(password: String): Int {
        Log.d("TAG", "Valor da senha: $password")

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }

        return when {
            password.length < 6 -> R.string.weak_password
            password.length in 6..10 && (!hasUpperCase || !hasDigit) -> R.string.medium_password
            password.length > 10 && hasUpperCase && hasDigit -> R.string.strong_password
            else -> R.string.medium_password
        }
    }

    fun getPassword(): String {
        return passwordEditText.text.toString()
    }
}


