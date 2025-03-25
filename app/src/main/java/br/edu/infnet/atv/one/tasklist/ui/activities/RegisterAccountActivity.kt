package br.edu.infnet.atv.one.tasklist.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.infnet.atv.one.tasklist.R
import com.google.firebase.auth.FirebaseAuth
import br.edu.infnet.atv.one.tasklist.ui.fragments.EmailValidationView
import br.edu.infnet.atv.one.tasklist.ui.fragments.PasswordStrengthView

class RegisterAccountActivity : AppCompatActivity() {
    private lateinit var  auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_account)
        title="Register"

        auth= FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
    }

    fun register(view: View){
        val emailValidationView = findViewById<EmailValidationView>(R.id.email_validation_view)
        val email = emailValidationView.getEmail()
//        val email= this.findViewById<EditText>(R.id.edit_mail_register).text.toString()
        val passwordStrengthView = findViewById<PasswordStrengthView>(R.id.password_strength_view)
        val password = passwordStrengthView.getPassword()
//        val password= this.findViewById<EditText>(R.id.edit_password_register).text.toString()
        val passwordConfirm = this.findViewById<EditText>(R.id.edit_confirm_password).text.toString()

        if (validateFields(email, password, passwordConfirm)) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var user = auth.currentUser
                    if (user != null) {
                        saveUserDataToLocal(user.uid, email, user.displayName)
                    }
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateFields(email: String, password: String, passwordConfirm: String): Boolean {
        val passwordTrimmed = password.trim()
        val passwordConfirmTrimmed = passwordConfirm.trim()

        if (email.isEmpty() || passwordTrimmed.isEmpty() || passwordConfirmTrimmed.isEmpty()) {
            Toast.makeText(applicationContext, getString(R.string.all_fields_are_mandatory), Toast.LENGTH_LONG).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(applicationContext, getString(R.string.invalid_email), Toast.LENGTH_LONG).show()
            return false
        }

        if (passwordTrimmed.length < 6) {
            Toast.makeText(applicationContext, getString(R.string.min_text_password), Toast.LENGTH_LONG).show()
            return false
        }


        Log.d("TAG", "Valor da senha: $passwordTrimmed")
        Log.d("TAG", "Valor da confirmacao senha: $passwordConfirmTrimmed")

        if (passwordTrimmed != passwordConfirmTrimmed) {
            Toast.makeText(applicationContext, getString(R.string.passwords_dont_match), Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }


    private fun saveUserDataToLocal(uid: String, email: String, name: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("user_firebase_uid", uid)
        editor.putString("user_email", email)
        editor.putString("user_name", name)
        editor.apply()
    }

    fun goToLogin(view: View) {
        val intent= Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

//    fun goToLogin(view: View) {
//        onBackPressed()
//        finish()
//    }

}