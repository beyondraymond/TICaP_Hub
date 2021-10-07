package com.cyberace.ticaphub.ui.taskDetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.model.ActivityClass
import kotlinx.android.synthetic.main.adapter_task_list.view.*

class TaskDetailsAdapter(
    private val listener: OnItemClickListener
): RecyclerView.Adapter<TaskDetailsAdapter.TaskDetailsViewHolder>() {

    //THIS ADAPTER IS USED FOR COMMENTS

    inner class TaskDetailsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener{

        init {
            itemView.setOnClickListener(this)
        }

        //MIGHT DELETE THIS LATER
        override fun onClick(v: View?) {
            if (adapterPosition != RecyclerView.NO_POSITION){
                listener.onItemClick(comments[adapterPosition].id)
            }
        }

    }

    interface OnItemClickListener{
        fun onItemClick(taskID: Int)
    }


    private val diffCallback = object : DiffUtil.ItemCallback<ActivityClass>() {
        override fun areItemsTheSame(oldItem: ActivityClass, newItem: ActivityClass): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivityClass, newItem: ActivityClass): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var comments: List<ActivityClass>
        get() = differ.currentList
        set(value) { differ.submitList(value)}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_comment, parent, false)
        return  TaskDetailsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onBindViewHolder(holder: TaskDetailsViewHolder, position: Int) {

        //Add a logic to get profile image
            //Add an interface function to get the image
            //Use that interface function to display the image here
        holder.itemView.apply {

        }
    }

}