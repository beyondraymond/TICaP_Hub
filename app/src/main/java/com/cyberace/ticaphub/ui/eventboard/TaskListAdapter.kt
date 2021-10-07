package com.cyberace.ticaphub.ui.eventboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.model.TaskCardClass
import kotlinx.android.synthetic.main.adapter_task_list.view.*

class TaskListAdapter(
    private val listener: OnItemClickListener
): RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder>() {


    inner class TaskListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener{

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (adapterPosition != RecyclerView.NO_POSITION){
                listener.onItemClick(todos[adapterPosition].id)
            }
        }



    }

    interface OnItemClickListener{
        fun onItemClick(taskID: Int)
    }


    private val diffCallback = object : DiffUtil.ItemCallback<TaskCardClass>() {
        override fun areItemsTheSame(oldItem: TaskCardClass, newItem: TaskCardClass): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskCardClass, newItem: TaskCardClass): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var todos: List<TaskCardClass>
        get() = differ.currentList
        set(value) { differ.submitList(value)}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_task_list, parent, false)
        return  TaskListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {

        holder.itemView.apply {
            txtTaskTitleInList.text = todos[position].title
        }
    }

}