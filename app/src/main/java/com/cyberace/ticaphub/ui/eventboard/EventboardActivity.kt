package com.cyberace.ticaphub.ui.eventboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.RetrofitInstance
import com.cyberace.ticaphub.ui.taskDetails.TaskDetailsActivity
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.adapter_event_board.*
import kotlinx.android.synthetic.main.dialog_add_new_board.view.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class EventboardActivity : AppCompatActivity(), EventboardAdapter.OnItemClickListener {

    private val eventBoardAdapter = EventboardAdapter(this)
    private val tagName = "E-List Fragment"

    private val resultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK){
            fetchBoards()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        //setHasOptionsMenu(true) try mo muna i-implement yung menu na wala to

        supportActionBar?.title = intent.getStringExtra("event-name")

        rvEventBoard.adapter = eventBoardAdapter
        rvEventBoard.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        //Move tAsk bhie

        fetchBoards()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        refreshLayoutEventBoard.setOnRefreshListener {
            refreshLayoutEventBoard.isRefreshing = true
            fetchBoards()
            refreshLayoutEventBoard.isRefreshing = false
        }
    }



    @SuppressLint("SetTextI18n")
    private fun fetchBoards(){
//        val bundle = intent.extras
//        val event: EventClass = bundle?.getParcelable("event")!!

        lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getBoards(
                    "Bearer " + this@EventboardActivity
                                .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                .getString("userToken", "0"),
                    intent.getIntExtra("eventID", 0)
                )
            } catch (e: IOException) {
                rvEventBoard.visibility = View.GONE
                txtPromptEventboard.text = "IO Error: Failed to connect to the server"
                txtPromptEventboard.visibility = View.VISIBLE
                imageViewEventBoard.visibility = View.VISIBLE
                Log.e(tagName, "IO Error:" + e.message.toString())
                return@launch
            } catch (e: HttpException) {
                rvEventBoard.visibility = View.GONE
                txtPromptEventboard.text = "HTTP Error: Unexpected response from the server"
                txtPromptEventboard.visibility = View.VISIBLE
                imageViewEventBoard.visibility = View.VISIBLE
                Log.e(tagName, "HTTP Error:" + e.message.toString())
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.lists.isEmpty()){
                    rvEventBoard.visibility = View.GONE
                    txtPromptEventboard.visibility = View.VISIBLE
                    imageViewEventBoard.visibility = View.VISIBLE
                }else{
                    eventBoardAdapter.taskLists = response.body()!!.lists
                    rvEventBoard.visibility = View.VISIBLE
                    txtPromptEventboard.visibility = View.GONE
                    imageViewEventBoard.visibility = View.GONE
                }
            } else {
                val msg= "Response not successful"
                rvEventBoard.visibility = View.GONE
                txtPromptEventboard.text = msg
                txtPromptEventboard.visibility = View.VISIBLE
                imageViewEventBoard.visibility = View.VISIBLE
                Log.e(tagName, msg)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.eventboard_action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            android.R.id.home -> {
                this.finish()
                return true
            }

            R.id.menu_add_task_list -> {
                val inflater = this.layoutInflater.inflate(R.layout.dialog_add_new_board,null)
                AlertDialog.Builder(this)
                    .setTitle("Enter Task List Name")
                    .setView(inflater)
                    .setPositiveButton("Submit"){_,_ ->

                        val tag = "AddBoardDialog"
                        lifecycleScope.launch {
                            val response = try {
                                RetrofitInstance.api.addBoard(
                                    "Bearer " + this@EventboardActivity
                                        .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                        .getString("userToken", "0"),
                                    intent.getIntExtra("eventID", 0),
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
                                Toast.makeText(this@EventboardActivity, "New board added successfully.", Toast.LENGTH_SHORT).show()
                                fetchBoards()
                                return@launch
                            } else {
                                Toast.makeText(this@EventboardActivity, "Operation Failed: " + response.body()!!, Toast.LENGTH_SHORT).show()
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

    override fun onMenuOptionPopupClick(position: Int) {
        val clickedItem = eventBoardAdapter.taskLists[position]

        val popupMenu = PopupMenu(this,rvEventBoard.findViewHolderForAdapterPosition(position)!!.itemView.findViewById(R.id.imgViewPopup)).apply {
            inflate(R.menu.eventboard_popup_menu)
            setOnMenuItemClickListener {
                when(it.itemId) {

                    R.id.menu_add_task -> {

                        //I'm reusing the dialog box that I created for Add New Board, I'm replacing it with with task list
                        val inflater = this@EventboardActivity.layoutInflater.inflate(R.layout.dialog_add_new_board,null)
                        inflater.inputBoardName.hint = "Task Title"
                        AlertDialog.Builder(this@EventboardActivity)
                            .setTitle("Enter Task Title")
                            .setView(inflater)
                            .setPositiveButton("Submit"){_,_ ->

                                val tag = "AddTaskDialog"
                                lifecycleScope.launch {
                                    val response = try {
                                        RetrofitInstance.api.addTask(
                                            "Bearer " + this@EventboardActivity
                                                .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                                .getString("userToken", "0"),
                                            intent.getIntExtra("eventID", 0),
                                            clickedItem.id, //ListID
                                            inflater.inputBoardName.text.toString(),
                                            resources.getString(R.string.default_task_desc),
//                                            getSharedPreferences("loginCredential", Context.MODE_PRIVATE).getInt("userID", 0)
                                        )
                                        //Add user id, created date, etc. Or whatevah
                                    } catch(e: IOException) {
                                        Log.e(tag, e.message.toString())
                                        return@launch
                                    } catch (e: HttpException) {
                                        Log.e(tag, "HttpException, unexpected response")
                                        return@launch
                                    }
                                    if(response.isSuccessful && response.code() == 200) {
                                        fetchBoards()
                                        Toast.makeText(this@EventboardActivity, "New task added successfully.", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    } else {
                                        Toast.makeText(this@EventboardActivity, "Operation Failed: " + response.body()!!, Toast.LENGTH_SHORT).show()
                                        Log.e(tag, "Error on Response")
                                        return@launch
                                    }
                                }
                            }
                            .create()
                            .show()
                        return@setOnMenuItemClickListener true
                    }

                    R.id.menu_rename_task_list -> {

                        //I'm reusing the dialog box that I created for Add New Board, I'm replacing it with with task list
                        val inflater = this@EventboardActivity.layoutInflater.inflate(R.layout.dialog_add_new_board,null)
                        inflater.inputBoardName.setText(clickedItem.title)
                        AlertDialog.Builder(this@EventboardActivity)
                            .setTitle("Rename Task List")
                            .setView(inflater)
                            .setPositiveButton("Submit"){_,_ ->

                                val tag = "RenameTaskListDialog"
                                lifecycleScope.launch {
                                    val response = try {
                                        RetrofitInstance.api.updateList(
                                            "Bearer " + this@EventboardActivity
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
                                        fetchBoards()
                                        Toast.makeText(this@EventboardActivity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                                        return@launch
                                    } else {
                                        Toast.makeText(this@EventboardActivity, "Operation Failed with code: " + response.body()!!, Toast.LENGTH_SHORT).show()
                                        Log.e(tag, "Error on Response")
                                        return@launch
                                    }
                                }
                            }
                            .create()
                            .show()
                        return@setOnMenuItemClickListener true
                    }

                    R.id.menu_delete_task_list -> {

                        AlertDialog.Builder(this@EventboardActivity)
                            .setTitle("Confirm Task List Deletion")
                            .setMessage("\"" + clickedItem.title + "\"" + " will be deleted permanently.")
                            .setPositiveButton("Delete"){_,_ ->

                                val tag = "RenameEventDialog"

                                lifecycleScope.launch {
                                    val response = try {
                                        RetrofitInstance.api.deleteList(
                                            "Bearer " + this@EventboardActivity
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
                                        fetchBoards()
                                        Toast.makeText(this@EventboardActivity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                                        return@launch
                                    } else {
                                        Toast.makeText(this@EventboardActivity,"Operation failed with code: " + response.code(), Toast.LENGTH_SHORT).show()
                                        Log.e(tag, "Error on Response")
                                        return@launch
                                    }
                                }
                            }
                            .setNegativeButton("Cancel"){_, _ ->
                                Toast.makeText(this@EventboardActivity, "Operation cancelled.", Toast.LENGTH_SHORT).show()
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

    override fun onNestedItemClick(taskID: Int) {
        val intent = Intent(this, TaskDetailsActivity::class.java).apply {
            putExtra("taskID", taskID)
        }
        resultContract.launch(intent)
    }
}