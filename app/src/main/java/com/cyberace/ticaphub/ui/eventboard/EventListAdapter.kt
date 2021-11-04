package com.cyberace.ticaphub.ui.eventboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cyberace.ticaphub.model.EventClass
import com.cyberace.ticaphub.R
import kotlinx.android.synthetic.main.adapter_event_list.view.*

class EventListAdapter(
    private val listener: OnItemClickListener
): RecyclerView.Adapter<EventListAdapter.EventListViewHolder>() {

    inner class EventListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener{

        val imgViewPopup: ImageView = itemView.imgEventListPopup


        init {
            itemView.setOnClickListener(this)
            imgViewPopup.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onMenuOptionPopupClick(adapterPosition)
                }
            }
        }

        override fun onClick(v: View?) {
            if (adapterPosition != RecyclerView.NO_POSITION){
                listener.onItemClick(adapterPosition)
            }
        }

    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onMenuOptionPopupClick(position: Int)
    }

    //rename taskcard with board or something
    private val diffCallback = object : DiffUtil.ItemCallback<EventClass>() {
        override fun areItemsTheSame(oldItem: EventClass, newItem: EventClass): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EventClass, newItem: EventClass): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var events: List<EventClass>
        get() = differ.currentList
        set(value) { differ.submitList(value)}
    //rename todos with board

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_event_list, parent, false)
        return  EventListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventListViewHolder, position: Int) {
        holder.itemView.apply {
            txtEventTitle.text = events[position].name
        }
    }


}