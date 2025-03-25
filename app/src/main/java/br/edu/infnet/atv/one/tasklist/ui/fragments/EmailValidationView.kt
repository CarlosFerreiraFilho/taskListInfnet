package br.edu.infnet.atv.one.tasklist.ui.fragments

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import br.edu.infnet.atv.one.tasklist.R
import com.google.android.material.textfield.TextInputEditText

class EmailValidationView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val emailEditText: TextInputEditText
    private val emailValidationIndicator: TextView

    init {
        inflate(context, R.layout.email_validation_view, this)
        emailEditText = findViewById(R.id.edit_email)
        emailValidationIndicator = findViewById(R.id.email_validation_indicator)

        emailEditText.addTextChangedListener {
            val email = emailEditText.text.toString()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailValidationIndicator.text = context.getString(R.string.invalid_email)
            } else {
                emailValidationIndicator.text = ""
            }
        }
    }

    fun getEmail(): String {
        return emailEditText.text.toString()
    }
}
