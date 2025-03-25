package br.edu.infnet.atv.one.tasklist.data.repository

import androidx.lifecycle.LiveData
import br.edu.infnet.atv.one.tasklist.data.models.Task

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()

    suspend fun insert(task: Task){
        taskDao.insert(task)
    }

    suspend fun delete(taskId: String) {
        taskDao.delete(taskId)
    }

    suspend fun update(task: Task){
        taskDao.update(task.taskId, task.title, task.description)
    }
}