package br.edu.infnet.atv.one.tasklist.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.infnet.atv.one.tasklist.ui.activities.MainActivity
import br.edu.infnet.atv.one.tasklist.R
import br.edu.infnet.atv.one.tasklist.data.models.Task


class TaskAdapter(
    private val ctx: MainActivity,
    private val dataList: MutableList<Task>
): RecyclerView.Adapter<TaskAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {

        val itemList = LayoutInflater.from(ctx).inflate(R.layout.fragment_list_item, parent, false)
        val holder = MenuViewHolder(itemList)
        return holder
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        (holder as MenuViewHolder).bind(position)
        holder.title.text = dataList[position].title
        holder.description.text = dataList[position].description
    }

    override fun getItemCount(): Int = dataList.size

    inner class  MenuViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title   = itemView.findViewById<TextView>(R.id.tv_title)
        val description   = itemView.findViewById<TextView>(R.id.tv_note)
        val imgTrash = itemView.findViewById<ImageView>(R.id.btn_delete)
        val imgComplete = itemView.findViewById<ImageView>(R.id.btn_complete_task)
        val liTask = itemView.findViewById<LinearLayout>(R.id.li_task)

        fun bind(position: Int) {
            imgTrash.setOnClickListener {
                ctx.deleteTask(dataList[position].taskId)
            }
            liTask.setOnClickListener {
                ctx.editTask(dataList[position])
            }
            imgComplete.setOnClickListener {
                ctx.completeTask(dataList[position].taskId)
            }
        }

    }

}