package br.edu.infnet.atv.one.tasklist.data.repository

import androidx.lifecycle.LiveData
import androidx.room.*
import br.edu.infnet.atv.one.tasklist.data.models.Task


@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Query("SELECT * from task order by title ASC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("UPDATE task set title = :title, description = :description where taskId = :taskId")
    suspend fun update(taskId: String?, title: String?, description: String?)

    @Query("delete from task where taskId = :taskId")
    suspend fun delete(taskId: String)
}