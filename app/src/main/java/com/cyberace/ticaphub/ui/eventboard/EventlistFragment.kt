package com.cyberace.ticaphub.ui.eventboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlinx.android.synthetic.main.dialog_add_new_board.view.*
import kotlinx.android.synthetic.main.fragment_eventlist.*


class EventlistFragment : Fragment(R.layout.fragment_eventlist),
    EventListAdapter.OnItemClickListener {

    private val eventListAdapter = EventListAdapter(this)
    private val tagName = "E-List Fragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        rvEventList.adapter = eventListAdapter
        rvEventList.layoutManager = LinearLayoutManager(activity)


        //Add some logic para i-restrict yung access kapag hindi naman siya officer

        fetchEvents()

        refreshLayoutEventList.setOnRefreshListener {
            refreshLayoutEventList.isRefreshing = true
            fetchEvents()
            refreshLayoutEventList.isRefreshing = false
            //Run tests kung pano siya magreact kapag empty yung task lists/task board

        }

    }

    override fun onItemClick(position: Int) {
        //Add Intent Something to get data from fragment
        val clickedItem = eventListAdapter.events[position]

        Intent(activity, EventboardActivity::class.java).apply {
            putExtra("id", clickedItem.id)
            putExtra("event-name", clickedItem.name)
            startActivity(this)
        }
    }

    private fun fetchEvents(){
        viewLifecycleOwner.lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getEvents()
            } catch (e: IOException) {
                Log.e(tagName, e.message.toString())
                return@launch
            } catch (e: HttpException) {
                Log.e(tagName, "HttpException, unexpected response")
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
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
            } else {
                Log.e(tagName, "Response not successful")
            }
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
                                RetrofitInstance.api.addEvent(inflater.inputBoardName.text.toString())
                            } catch(e: IOException) {
                                Log.e(tag, e.message.toString())
                                return@launch
                            } catch (e: HttpException) {
                                Log.e(tag, "HttpException, unexpected response")
                                return@launch
                            }
                            if(response.isSuccessful && response.body()!!.sqlResponse == "201") {
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