package com.cyberace.ticaphub.ui.taskDetails

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.model.ActivityClass
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_comment.view.*
import kotlinx.android.synthetic.main.adapter_event_list.view.*
import java.text.SimpleDateFormat

class TaskDetailsAdapter(
    private val listener: OnItemClickListener,
    private val context: Context
): RecyclerView.Adapter<TaskDetailsAdapter.TaskDetailsViewHolder>() {

    //THIS ADAPTER WILL BE PRIMARILY USED FOR TASK COMMENTS/ACTIVITY

    inner class TaskDetailsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        //Add TextView of File Names Here
        val txtIncomingAttachment = itemView.txtIncomingAttachment
        val txtOutgoingAttachment = itemView.txtOutgoingAttachment

        init {
            txtIncomingAttachment.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onFileClick(adapterPosition)
                }
            }
            txtOutgoingAttachment.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onFileClick(adapterPosition)
                }
            }
        }
    }

    interface OnItemClickListener{
        fun getUserProfilePic(userID: Int): String
        fun onFileClick(position: Int)
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

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: TaskDetailsViewHolder, position: Int) {

        val sharedPrefUserID = context.getSharedPreferences("loginCredential", Context.MODE_PRIVATE).getInt("userID", 0)
        var dateSQLFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSz")

        if (Build.VERSION.SDK_INT >= 24){
            dateSQLFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
        }

        var commentDate = dateSQLFormat.parse("2021-10-20T15:31:41.000000Z")
        val dateOutputFormat = SimpleDateFormat("MM/dd/yyyy hh:mm")


        //Add a logic to get profile image
            //Add an interface function to get the image using the user id?
            //Use that interface function to display the image here

        //Use the shared preference to identify if the user id is the same as the user logged
        holder.itemView.apply {

            try {
                commentDate = dateSQLFormat.parse(comments[position].created_at)!!
            } catch (e: Exception) {
                Log.e("Date Parse", e.message.toString())
            }

            //If statement para malaman kapag kay user galing yung comment
            if(comments[position].user_id == sharedPrefUserID){
                constraintIncoming.visibility = View.GONE
                constraintOutgoing.visibility = View.VISIBLE
                txtOutgoingDate.text = dateOutputFormat.format(commentDate!!)
                txtOutgoingComment.text = comments[position].description
                //Picasso.get().load(getUserProfilePic(comments[position].user_id)).into(imgOutgoing)
                if(comments[position].files.isEmpty()){
                    txtOutgoingAttachment.visibility = View.GONE
                }else{
                    txtOutgoingAttachment.text = comments[position].files[comments[position].files.lastIndex].name
                }

                //Else-if statement para malaman kung galing sa ibang user yung comment
                //Using else only will result to pagiging visible ng incoming adapter
            }else if(comments[position].user_id != sharedPrefUserID && comments[position].user_id>0){
                constraintOutgoing.visibility = View.GONE
                constraintIncoming.visibility = View.VISIBLE
                txtIncomingDate.text = dateOutputFormat.format(commentDate!!)
                txtIncomingComment.text = comments[position].description
                //Picasso.get().load(listener.getUserProfilePic(comments[position].user_id)).into(imgOutgoing)
                if(comments[position].files.isEmpty()){
                    txtIncomingAttachment.visibility = View.GONE
                }else{
                    txtIncomingAttachment.text = comments[position].files[comments[position].files.lastIndex].name
                }
            }
        }
    }

}