package br.edu.infnet.atv.one.tasklist.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.infnet.atv.one.tasklist.R
import br.edu.infnet.atv.one.tasklist.data.models.Task
import br.edu.infnet.atv.one.tasklist.databinding.ActivityAddTaskBinding
import br.edu.infnet.atv.one.tasklist.ui.fragments.SaveButtonFragment
import br.edu.infnet.atv.one.tasklist.utils.ToastUtils
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.database
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity(), SaveButtonFragment.OnSaveButtonClickListener {
    private val database = Firebase.database
    private val taskReference = database.getReference("tasks")
    lateinit var binding: ActivityAddTaskBinding
    var taskIdToEdit: String = ""
    var isUpdate = false
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
        setupDatePicker()
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
    }

    private fun validateTaskFields(title: String, description: String): Boolean {
        return if (title.isEmpty() || description.isEmpty()) {
            ToastUtils.showToast(this, getString(R.string.all_fields_are_mandatory))
            false
        } else {
            true
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()

        val savedDate = binding.editDueDate.text.toString()
        if (savedDate.isNotEmpty()) {
            try {
                val parsedDate = dateFormat.parse(savedDate)
                parsedDate?.let {
                    calendar.time = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            binding.editDueDate.setText(dateFormat.format(calendar.time))
        }

        binding.editDueDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                DatePickerDialog(
                    this@AddTaskActivity,
                    datePicker,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }
}
