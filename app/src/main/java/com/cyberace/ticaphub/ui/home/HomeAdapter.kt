package com.cyberace.ticaphub.ui.home

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.model.TaskCardClass
import kotlinx.android.synthetic.main.adapter_home_card.view.*
import java.text.SimpleDateFormat

class HomeAdapter(
    private val listener: OnItemClickListener
): RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    inner class HomeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener{

        //Add an onlick interface para sa download linkeu
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (adapterPosition != RecyclerView.NO_POSITION){
                listener.onItemClick(adapterPosition)
            }
        }

    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_card, parent, false)
        return  HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        //The following variable will parse the SQL date into a readable Java Date
        var dateSQLFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSz")

        if (Build.VERSION.SDK_INT >= 24){
            dateSQLFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
        }

        var taskDate = dateSQLFormat.parse("2021-10-19T15:47:10.000000Z")

        val dayNameFormatter = SimpleDateFormat("EEE")
        val dayNumberFormatter = SimpleDateFormat("dd")
        val monthNameFormatter = SimpleDateFormat("MMM")

        holder.itemView.apply {

            try {
                taskDate = dateSQLFormat.parse(todos[position].created_at)!!
            } catch (e: Exception) {
                Log.e("Date Parse", e.message.toString())
            }

            txtTaskTitle.text = todos[position].title
            txtTaskDesc.text = todos[position].description
            txtDayName.text = dayNameFormatter.format(taskDate!!)
            txtDayNumber.text = dayNumberFormatter.format(taskDate!!)
            txtMonthName.text = monthNameFormatter.format(taskDate!!)
        }
    }
}