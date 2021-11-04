package com.cyberace.ticaphub.ui.eventboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberace.ticaphub.LoginActivity
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.RetrofitInstance
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.adapter_event_board.*
import kotlinx.android.synthetic.main.adapter_event_list.*
import kotlinx.android.synthetic.main.dialog_add_new_board.view.*
import kotlinx.android.synthetic.main.fragment_eventlist.*
import kotlinx.android.synthetic.main.fragment_eventlist.imageViewEventBoard
import kotlinx.android.synthetic.main.fragment_eventlist.txtPromptEventboard
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


class EventlistFragment : Fragment(R.layout.fragment_eventlist),
    EventListAdapter.OnItemClickListener {

    private val eventListAdapter = EventListAdapter(this)
    private val tagName = "E-List Fragment"
    private var isRole = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        rvEventList.adapter = eventListAdapter
        rvEventList.layoutManager = LinearLayoutManager(activity)

        //Checking Role First before fetching the task
        checkRole()

        refreshLayoutEventList.setOnRefreshListener {
            checkRole()
            //Run tests kung pano siya magreact kapag empty yung task lists/task board

        }

    }

    override fun onItemClick(position: Int) {
        //Add Intent Something to get data from fragment
        val clickedItem = eventListAdapter.events[position]

        Intent(activity, EventboardActivity::class.java).apply {
            putExtra("eventID", clickedItem.id)
            putExtra("event-name", clickedItem.name)
            startActivity(this)
        }
    }

    override fun onMenuOptionPopupClick(position: Int) {

        val clickedItem = eventListAdapter.events[position]

        val popupMenu = PopupMenu(requireContext(), rvEventList.findViewHolderForAdapterPosition(position)!!.itemView.findViewById(R.id.imgEventListPopup)).apply {
            inflate(R.menu.eventlist_popup_menu)
            setOnMenuItemClickListener {
                when(it.itemId) {

                    R.id.menu_rename_event -> {

                        //I'm reusing the dialog box that I created for Add New Board, I'm replacing it with with task list
                        val inflater = this@EventlistFragment.layoutInflater.inflate(R.layout.dialog_add_new_board,null)
                        inflater.inputBoardName.setText(clickedItem.name)
                        AlertDialog.Builder(requireActivity())
                            .setTitle("Rename Event Board")
                            .setView(inflater)
                            .setPositiveButton("Submit"){_,_ ->

                                val tag = "RenameEventDialog"

                                lifecycleScope.launch {
                                    val response = try {
                                        RetrofitInstance.api.updateEvent(
                                            "Bearer " + requireActivity()
                                                .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                                .getString("userToken", "0"),
                                            clickedItem.id,
                                            inflater.inputBoardName.text.toString()
                                        )
                                    } catch(e: IOException) {
                                        Log.e(tag, e.message.toString())
                                        return@launch
                                    } catch (e: HttpException) {
                                        Log.e(tag, "HttpException, unexpected response")
                                        return@launch
                                    }
                                    if(response.isSuccessful && response.code() == 200) {
                                        fetchEvents()
                                        Toast.makeText(requireContext(), response.body()!!.message, Toast.LENGTH_SHORT).show()
                                        return@launch
                                    } else {
                                        Toast.makeText(requireContext(), "Operation failed with code: " + response.code(), Toast.LENGTH_SHORT).show()
                                        Log.e(tag, "Error on Response")
                                        return@launch
                                    }
                                }
                            }
                            .create()
                            .show()

                        return@setOnMenuItemClickListener true
                    }

                    R.id.menu_delete_event -> {
                        AlertDialog.Builder(requireActivity())
                            .setTitle("Confirm Event Deletion")
                            .setMessage("\"" + clickedItem.name + "\"" + " will be deleted permanently.")
                            .setPositiveButton("Delete"){_,_ ->

                                val tag = "RenameEventDialog"

                                lifecycleScope.launch {
                                    val response = try {
                                        RetrofitInstance.api.deleteEvent(
                                            "Bearer " + requireActivity()
                                                .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                                .getString("userToken", "0"),
                                            clickedItem.id
                                        )
                                    } catch(e: IOException) {
                                        Log.e(tag, e.message.toString())
                                        return@launch
                                    } catch (e: HttpException) {
                                        Log.e(tag, "HttpException, unexpected response")
                                        return@launch
                                    }
                                    if(response.isSuccessful && response.code() == 200) {
                                        fetchEvents()
                                        Toast.makeText(requireContext(), response.body()!!.message, Toast.LENGTH_SHORT).show()
                                        return@launch
                                    } else {
                                        Toast.makeText(requireContext(), "Operation failed with code: " + response.code(), Toast.LENGTH_SHORT).show()
                                        Log.e(tag, "Error on Response")
                                        return@launch
                                    }
                                }
                            }
                            .setNegativeButton("Cancel"){_, _ ->
                                Toast.makeText(requireContext(), "Operation cancelled.", Toast.LENGTH_SHORT).show()
                            }
                            .create()
                            .show()


                        return@setOnMenuItemClickListener true
                    }

                    else -> return@setOnMenuItemClickListener true
                }
            }
        }
        popupMenu.show()
    }

    private fun checkRole(){
        refreshLayoutEventList.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getAssignedTasks(
                    "Bearer " +
                            this@EventlistFragment
                                .requireActivity().getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                .getString("userToken", "0")
                )
            } catch (e: IOException) {
                rvEventList.visibility = View.GONE
                txtPromptEventboard.text = "IO Error: Failed to connect to the server"
                txtPromptEventboard.visibility = View.VISIBLE
                imageViewEventBoard.visibility = View.VISIBLE
                Log.e(tagName, "IO Error:" + e.message.toString())
                return@launch
            } catch (e: HttpException) {
                rvEventList.visibility = View.GONE
                txtPromptEventboard.text = "HTTP Error: Unexpected response from the server"
                txtPromptEventboard.visibility = View.VISIBLE
                imageViewEventBoard.visibility = View.VISIBLE
                Log.e(tagName, "HTTP Error:" + e.message.toString())
                return@launch
            } catch (e: JsonSyntaxException) {
                Toast.makeText(requireActivity(), "Token Expired. Login Again.", Toast.LENGTH_LONG).show()
                val sharedPref = this@EventlistFragment.requireActivity().getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.clear()
                editor.apply()

                Intent(requireActivity(), LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(this)
                }
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.roles.forEach {
                    if(it.id in 1..3){
                        isRole = true
                        fetchEvents()
                    }
                }
                fetchEvents()
            } else {
                val msg= "Response not successful"
                rvHome.visibility = View.GONE
                txtPromptHome.text = msg
                txtPromptHome.visibility = View.VISIBLE
                imageViewHome.visibility = View.VISIBLE
                Log.e(tagName, msg)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchEvents(){
        if (isRole){
            viewLifecycleOwner.lifecycleScope.launch {
                val response = try {
                    RetrofitInstance.api.getEvents(
                        "Bearer " +
                                this@EventlistFragment
                                    .requireActivity().getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                    .getString("userToken", "0")
                    )
                } catch (e: IOException) {
                    refreshLayoutEventList.isRefreshing = false
                    rvEventList.visibility = View.GONE
                    txtPromptEventboard.text = "IO Error: Failed to connect to the server"
                    txtPromptEventboard.visibility = View.VISIBLE
                    imageViewEventBoard.visibility = View.VISIBLE
                    Log.e(tagName, "IO Error:" + e.message.toString())
                    return@launch
                } catch (e: HttpException) {
                    refreshLayoutEventList.isRefreshing = false
                    rvEventList.visibility = View.GONE
                    txtPromptEventboard.text = "HTTP Error: Unexpected response from the server"
                    txtPromptEventboard.visibility = View.VISIBLE
                    imageViewEventBoard.visibility = View.VISIBLE
                    Log.e(tagName, "HTTP Error:" + e.message.toString())
                    return@launch
                }
                if (response.isSuccessful && response.body() != null) {
                    refreshLayoutEventList.isRefreshing = false
                    if (response.body()!!.isEmpty()){
                        rvEventList.visibility = View.GONE
                        txtPromptEventboard.visibility = View.VISIBLE
                        imageViewEventBoard.visibility = View.VISIBLE
                    }else{
                        eventListAdapter.events = response.body()!!
                        rvEventList.visibility = View.VISIBLE
                        txtPromptEventboard.visibility = View.GONE
                        imageViewEventBoard.visibility = View.GONE
                    }
                    return@launch
                } else {
                    refreshLayoutEventList.isRefreshing = false
                    val msg= "Response not successful"
                    rvEventList.visibility = View.GONE
                    txtPromptEventboard.text = msg
                    txtPromptEventboard.visibility = View.VISIBLE
                    imageViewEventBoard.visibility = View.VISIBLE
                    Log.e(tagName, msg + "Response Code: " + response.code())
                }
            }
        }else{
            refreshLayoutEventList.isRefreshing = false
            rvEventList.visibility = View.GONE
            txtPromptEventboard.visibility = View.VISIBLE
            imageViewEventBoard.visibility = View.VISIBLE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //add new menu.xml para sa event list swis depende sa balak ni peter
        inflater.inflate(R.menu.eventboard_action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_add_task_list -> {

                val inflater = this.layoutInflater.inflate(R.layout.dialog_add_new_board,null)
                inflater.inputBoardName.hint = "Event Name"
                AlertDialog.Builder(requireActivity())
                    .setTitle("Enter Event Name")
                    .setView(inflater)
                    .setPositiveButton("Submit"){_,_ ->

                        val tag = "AddEventDialog"
                        lifecycleScope.launch {
                            val response = try {
                                RetrofitInstance.api.addEvent(
                                    "Bearer " +
                                            this@EventlistFragment
                                                .requireActivity().getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                                .getString("userToken", "0"),
                                    inflater.inputBoardName.text.toString()
                                )
                            } catch(e: IOException) {
                                Log.e(tag, e.message.toString())
                                return@launch
                            } catch (e: HttpException) {
                                Log.e(tag, "HttpException, unexpected response")
                                return@launch
                            }
                            if(response.isSuccessful && response.body()!!.message == "Event has been created.") {
                                Toast.makeText(requireActivity(), "New event added successfully.", Toast.LENGTH_SHORT).show()
                                fetchEvents()
                                return@launch
                            } else {
                                Toast.makeText(requireActivity(), "Operation Failed: " + response.body()!!, Toast.LENGTH_SHORT).show()
                                Log.e(tag, "Error on Response")
                                return@launch
                            }
                        }
                    }
                    .create()
                    .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}