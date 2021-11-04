package com.cyberace.ticaphub.ui.taskDetails

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.model.User
import kotlinx.android.synthetic.main.adapter_member.view.*

class UpdateTaskAdapter(
    private val listener: OnItemClickListener,
): RecyclerView.Adapter<UpdateTaskAdapter.UpdateTaskViewHolder>() {

    //THIS ADAPTER WILL BE PRIMARILY USED FOR TASK COMMENTS/ACTIVITY

    inner class UpdateTaskViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val imgRemoveMember: ImageView = itemView.imgRemoveMember

        init {
            imgRemoveMember.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onRemoveBtnClick(adapterPosition)
                }
            }
        }
    }

    interface OnItemClickListener{
        fun onRemoveBtnClick(position: Int)
    }


    private val diffCallback = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var officers: List<User>
        get() = differ.currentList
        set(value) { differ.submitList(value)}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdateTaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_member, parent, false)
        return  UpdateTaskViewHolder(view)
    }

    override fun getItemCount(): Int {
        return officers.size
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: UpdateTaskViewHolder, position: Int) {

        //Use the shared preference to identify if the user id is the same as the user logged
        holder.itemView.apply {
            txtMemberName.text = officers[position].first_name + " " +
                    officers[position].middle_name.substring(0,1) + " " +
                    officers[position].last_name
        }

    }

}