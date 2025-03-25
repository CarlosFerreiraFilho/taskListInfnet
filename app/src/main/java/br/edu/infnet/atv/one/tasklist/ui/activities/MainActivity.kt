package br.edu.infnet.atv.one.tasklist.ui.activities

import br.edu.infnet.atv.one.tasklist.ui.adapters.TaskAdapter
import br.edu.infnet.atv.one.tasklist.utils.ToastUtils
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.infnet.atv.one.tasklist.R
import br.edu.infnet.atv.one.tasklist.data.models.Task
import br.edu.infnet.atv.one.tasklist.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import android.app.AlertDialog
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import br.edu.infnet.atv.one.tasklist.data.models.WeatherResponse
import br.edu.infnet.atv.one.tasklist.network.RetrofitClient
import br.edu.infnet.atv.one.tasklist.services.TaskExpirationWorker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    val database = Firebase.database
    val taskReference = database.getReference("tasks")
    lateinit var binding: ActivityMainBinding
    private lateinit var analytics: FirebaseAnalytics

    private val apiKey = "15a3c21f98fa67e939718c6e0e946240"

    private var currentImageType: String = "profile_images"

    private val pickImageContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToStorage(it, currentImageType)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        scheduleTaskExpirationCheck()
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        analytics = Firebase.analytics
        if (auth.currentUser != null) {
            getTasks()
        } else {
            signOut()
        }

        inicializaFirebase()
    }

    private fun getTasks() {
        val tasksReference = taskReference

        tasksReference.orderByChild("completed").equalTo(false).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks: MutableList<Task> = mutableListOf()
                val adapter = TaskAdapter(this@MainActivity, tasks)

                binding.recyclerView.adapter = adapter
                binding.tvNolista.visibility = if (snapshot.children.count() > 0) View.INVISIBLE else View.VISIBLE

                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    task?.let {
                        tasks.add(it)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    fun deleteTask(taskId: String) {
        val deleteRef = taskReference.child(taskId)
        deleteRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ToastUtils.showToast(this@MainActivity, getString(R.string.task_deleted))
                getTasks()
            } else {
                ToastUtils.showToast(this@MainActivity, "Falha ao remover tarefa: ${task.exception?.message}")
            }
        }
    }

    fun completeTask(taskId: String) {
        val completeRef = taskReference.child(taskId)

        val updates = mapOf<String, Any>(
            "completed" to true
        )

        completeRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ToastUtils.showToast(this@MainActivity, getString(R.string.task_completed))
                getTasks()
            } else {
                ToastUtils.showToast(this@MainActivity, "Falha ao completar tarefa: ${task.exception?.message}")
            }
        }
    }


    fun editTask(task: Task) {
        val intent = Intent(this, AddTaskActivity::class.java)
        intent.putExtra("taskId", task.taskId)
        intent.putExtra("title", task.title)
        intent.putExtra("description", task.description)
        intent.putExtra("dueDate", task.dueDate)
        startActivity(intent)
    }

    fun goToAddTask(view: View){
        val intent = Intent(this, AddTaskActivity::class.java)
        startActivity(intent)
    }

    fun logout(view: View){
        val bundle: Bundle = Bundle()
        bundle.putInt("Success", 101)
        analytics.logEvent("Logout", bundle)

        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun getActionOptions(view: View) {
        val options = arrayOf(getString(R.string.change_profile_picture), getString(R.string.change_cover_photo), getString(R.string.weather_forecast), getString(R.string.recover_password))

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_an_option))
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        pickImage("profile_images")
                    }
                    1 -> {
                        pickImage("cover_images")
                    }
                    2 -> {
                        showWeatherForecastDialog()
                    }
                    3 -> {
                        showPasswordRecoveryDialog()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun pickImage(imageType: String) {
        currentImageType = imageType
        pickImageContract.launch("image/*")
    }

    private fun uploadImageToStorage(imageUri: Uri?, imageType: String) {
        if (imageUri != null) {
            // Verifica se o arquivo existe
            val file = File(imageUri.path ?: "")
            if (file.exists()) {
                Log.d("Upload", "File exists: ${file.path}")

                val storageRef: StorageReference = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("${Uri.encode(imageType)}/${UUID.randomUUID()}.jpg")

                Log.d("Upload", "Uploading to path: ${imageRef.path}")
                Log.d("UploadRef", "Reff to path: ${storageRef}")

                val uploadTask = imageRef.putFile(imageUri)

                Log.d("UploadTask", "Task: ${uploadTask}")

                uploadTask.addOnProgressListener { snapshot ->
                    val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount)
                    Log.d("UploadProgress", "Upload is $progress% done")
                }.addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveImageUrlToDatabase(imageUrl, imageType)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("UploadError", "Erro ao fazer upload: ${exception.message}")
                    Toast.makeText(this, "Erro ao fazer upload da imagem: ${exception.message}", Toast.LENGTH_SHORT).show()
                    throw RuntimeException("Erro ao fazer upload: ${exception.message}")
                }
            } else {
                Log.e("Upload", "File does not exist: ${imageUri.path}")
                // Lidar com o erro adequadamente
                Toast.makeText(this, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun saveImageUrlToDatabase(imageUrl: String, imageType: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val databaseRef = FirebaseDatabase.getInstance().reference
        val userRef = databaseRef.child("users").child(userId)

        when (imageType) {
            "profile_images" -> userRef.child("profileImageUrl").setValue(imageUrl)
            "cover_images" -> userRef.child("coverImageUrl").setValue(imageUrl)
        }

        Toast.makeText(this, "Imagem atualizada com sucesso!", Toast.LENGTH_SHORT).show()
    }

    private fun showPasswordRecoveryDialog() {
        val emailInput = EditText(this)
        emailInput.hint = "Digite seu e-mail"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Recuperação de Senha")
            .setMessage("Digite o e-mail associado à sua conta")
            .setView(emailInput)
            .setPositiveButton("Enviar") { _, _ ->
                val email = emailInput.text.toString()

                if (email.isNotEmpty()) {
                    sendPasswordResetEmail(email)
                } else {
                    Toast.makeText(this, "E-mail não pode estar vazio", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun sendPasswordResetEmail(email: String) {
        val auth = FirebaseAuth.getInstance()

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "E-mail de recuperação enviado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Falha ao enviar e-mail: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showWeatherForecastDialog() {
        val cityInput = EditText(this)
        cityInput.hint = "Digite a cidade"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Previsão do Tempo")
            .setMessage("Digite o nome da cidade para obter a previsão")
            .setView(cityInput)
            .setPositiveButton("Buscar") { _, _ ->
                val city = cityInput.text.toString().trim()

                if (city.isNotEmpty()) {
                    fetchWeatherForecast(city)
                } else {
                    Toast.makeText(this, "Cidade não pode estar vazia", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun fetchWeatherForecast(city: String) {
        val weatherApiService = RetrofitClient.weatherApiService

        val weatherTranslations = mapOf(
            "clear sky" to "Céu Limpo",
            "few clouds" to "Poucas Nuvens",
            "scattered clouds" to "Nuvens Dispersas",
            "broken clouds" to "Nuvens Quebradas",
            "overcast clouds" to "Nublado",
            "shower rain" to "Chuva Passageira",
            "rain" to "Chuva",
            "thunderstorm" to "Tempestade",
            "snow" to "Neve",
            "mist" to "Névoa"
        )

        weatherApiService.getWeatherForecast(city, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    val temp = weatherData?.main?.temp ?: "N/A"
                    val description = weatherData?.weather?.get(0)?.description ?: "N/A"

                    val translatedDescription = weatherTranslations[description] ?: description

                    Toast.makeText(
                        this@MainActivity,
                        "Previsão para $city: $temp°C, $translatedDescription",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this@MainActivity, "Erro ao buscar a previsão", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun scheduleTaskExpirationCheck() {
        val workRequest = PeriodicWorkRequestBuilder<TaskExpirationWorker>(6, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun inicializaFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "Token FCM: $token")
        }
    }
}