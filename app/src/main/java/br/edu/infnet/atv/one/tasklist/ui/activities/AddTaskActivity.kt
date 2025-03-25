package br.edu.infnet.atv.one.tasklist.ui.activities

import br.edu.infnet.atv.one.tasklist.utils.ToastUtils
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.infnet.atv.one.tasklist.R
import br.edu.infnet.atv.one.tasklist.data.models.Task
import br.edu.infnet.atv.one.tasklist.databinding.ActivityAddTaskBinding
import br.edu.infnet.atv.one.tasklist.ui.fragments.SaveButtonFragment
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.database

class AddTaskActivity : AppCompatActivity(), SaveButtonFragment.OnSaveButtonClickListener {
    private val database = Firebase.database
    private val taskReference = database.getReference("tasks")
    lateinit var binding: ActivityAddTaskBinding
    var taskIdToEdit: String = ""
    var isUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.saveButtonFragment, SaveButtonFragment.newInstance(getString(R.string.save)))
            .commit()

        taskToEdit()
    }

    override fun onSaveClicked() {
        saveTask(binding.root)
    }

    fun saveTask(v: View) {
        val title = binding.editTitleTask.text.toString()
        val description = binding.editDescriptionTask.text.toString()
        val dueDate = binding.editDueDate.text.toString()

        if (validateTaskFields(title, description)) {
            val taskReference = if (isUpdate) taskReference.child(taskIdToEdit) else taskReference.push()
            val taskId = taskIdToEdit.ifEmpty { taskReference.key }
            val newTask = Task(taskId!!, title, description, dueDate, completed = false)
            taskId?.let {
                taskReference.setValue(newTask)
                val message = if (isUpdate) getString(R.string.task_updated) else getString(R.string.task_created)
                ToastUtils.showToast(this, message)
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    fun taskToEdit() {
        intent.getStringExtra("taskId")?.let {
            if (it != "null") {
                isUpdate = true
                taskIdToEdit = it
                binding.editTitleTask.setText(intent.getStringExtra("title") ?: "")
                binding.editDescriptionTask.setText(intent.getStringExtra("description") ?: "")
                binding.editDueDate.setText(intent.getStringExtra("dueDate") ?: "")
            }
        }
    }
    fun backPage(view: View) {
        onBackPressedDispatcher.onBackPressed()
//        val intent= Intent(this,MainActivity::class.java)
//        startActivity(intent)
//        finish()
    }
    private fun validateTaskFields(title: String, description: String): Boolean {
        return if (title.isEmpty() || description.isEmpty()) {
            ToastUtils.showToast(this, getString(R.string.all_fields_are_mandatory))
            false
        } else {
            true
        }
    }
}
