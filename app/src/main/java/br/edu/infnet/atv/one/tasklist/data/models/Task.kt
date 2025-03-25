package br.edu.infnet.atv.one.tasklist.data.models
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task")
data class Task(
    @PrimaryKey() val taskId: String = "",
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "dueDate") val dueDate: String = "",
    @ColumnInfo(name = "completed") val completed: Boolean = false
): java.io.Serializable