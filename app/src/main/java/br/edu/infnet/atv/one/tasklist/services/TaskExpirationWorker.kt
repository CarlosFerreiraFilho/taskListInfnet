package br.edu.infnet.atv.one.tasklist.services

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import br.edu.infnet.atv.one.tasklist.data.models.Task
import br.edu.infnet.atv.one.tasklist.utils.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.text.SimpleDateFormat
import java.util.*

class TaskExpirationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val database = Firebase.database
    private val taskReference = database.getReference("tasks")

    override fun doWork(): Result {
        Log.d("TaskExpirationWorker", "Verificando tarefas para expiração...")

        taskReference.orderByChild("completed").equalTo(false).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    Log.d("TaskExpirationWorker", "Tarefa : $task")
                    task?.let {
                        if (isTaskExpiringSoon(it.dueDate)) {
                            Log.d("TaskExpirationWorker", "Tarefa expirando")

                            sendNotification(it.title, it.dueDate)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TaskExpirationWorker", "Erro ao buscar tarefas: ${error.message}")
            }
        })

        return Result.success()
    }

    private fun isTaskExpiringSoon(dueDate: String?): Boolean {
        if (dueDate.isNullOrEmpty()) return false

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        val taskDate = Calendar.getInstance()

        return try {
            taskDate.time = sdf.parse(dueDate) ?: return false
            val diff = (taskDate.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)
            diff in 0..1
        } catch (e: Exception) {
            false
        }
    }

    private fun sendNotification(title: String?, dueDate: String?) {
        Log.d("TaskExpirationWorker", "Dispara notificacao")

        NotificationHelper(applicationContext).createNotification(
            "Tarefa Expirando!",
            "A tarefa \"$title\" expira em $dueDate."
        )
    }

}
