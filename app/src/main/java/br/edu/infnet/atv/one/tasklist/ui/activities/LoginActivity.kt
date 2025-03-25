package br.edu.infnet.atv.one.tasklist.ui.activities
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.infnet.atv.one.tasklist.R
import br.edu.infnet.atv.one.tasklist.databinding.ActivityLoginBinding
import br.edu.infnet.atv.one.tasklist.ui.fragments.EmailValidationView
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: ActivityLoginBinding
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        analytics = Firebase.analytics
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        binding.btnGoogle.visibility = View.INVISIBLE
        binding.orTextView.visibility = View.INVISIBLE
    }

    fun loginGoogleButtonTapped(v: View) {

    }

    fun login(v: View) {
        val bundle: Bundle = Bundle()
        val emailValidationView = findViewById<EmailValidationView>(R.id.email_validation_view)
        val email = emailValidationView.getEmail()
//        val email = this.findViewById<EditText>(R.id.edit_mail).text.toString()
        val password = this.findViewById<EditText>(R.id.edit_password).text.toString()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                bundle.putInt("Success", 100)

                analytics.logEvent("Login", bundle)

                var user = auth.currentUser
                if (user != null) {
                    saveUserDataToLocal(user.uid, email, user.displayName)
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            bundle.putInt("Error", 100)
            analytics.logEvent("Login", bundle)
            Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun saveUserDataToLocal(uid: String, email: String, name: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("user_firebase_uid", uid)
        editor.putString("user_email", email)
        editor.putString("user_name", name)
        editor.apply()
    }

    fun goToRegister(v: View) {
        val intent = Intent(this, RegisterAccountActivity::class.java)
        startActivity(intent)
        finish()
    }
}