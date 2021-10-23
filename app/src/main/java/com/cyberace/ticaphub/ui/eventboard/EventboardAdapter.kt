package com.cyberace.ticaphub.ui.eventboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.model.TaskCardClass
import com.cyberace.ticaphub.model.TaskListClass
import kotlinx.android.synthetic.main.adapter_event_board.view.*


class EventboardAdapter(
    private val listener: OnItemClickListener
): RecyclerView.Adapter<EventboardAdapter.EventboardViewHolder>(), TaskListAdapter.OnItemClickListener {

    lateinit var context: Context

    inner class EventboardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val imgViewPopup: ImageView = itemView.imgViewPopup

        init {
            imgViewPopup.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION){
                    listener.onMenuOptionPopupClick(adapterPosition)
                }
            }
        }
    }

    interface OnItemClickListener{
        fun onMenuOptionPopupClick(position: Int)
        fun onNestedItemClick(taskID: Int)
    }

    private val diffCallback = object : DiffUtil.ItemCallback<TaskListClass>() {
        override fun areItemsTheSame(oldItem: TaskListClass, newItem: TaskListClass): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskListClass, newItem: TaskListClass): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var taskLists: List<TaskListClass>
        get() = differ.currentList
        set(value) { differ.submitList(value)}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_event_board, parent, false)
        return  EventboardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return taskLists.size
    }

    override fun onBindViewHolder(holder: EventboardViewHolder, position: Int) {
        holder.itemView.apply {
            txtBoardTitle.text = taskLists[position].title
            if (taskLists[position].tasks.isEmpty()){
                rvTaskList.visibility = View.GONE
                txtPromptEventboardAdapter.visibility = View.VISIBLE
                imageViewEventboardAdapter.visibility = View.VISIBLE
            }else{
                rvTaskList.visibility = View.VISIBLE
                txtPromptEventboardAdapter.visibility = View.GONE
                imageViewEventboardAdapter.visibility = View.GONE
                populateBoards(rvTaskList, taskLists[position].tasks, context)
            }

        }
        context = holder.itemView.context
    }

    private fun populateBoards(recyclerView: RecyclerView, taskCardList: List<TaskCardClass>, context: Context){
        val todoAdapter = TaskListAdapter(this)

        recyclerView.adapter = todoAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        todoAdapter.todos = taskCardList
    }

    override fun onItemClick(taskID: Int) {
        listener.onNestedItemClick(taskID)

    }



}