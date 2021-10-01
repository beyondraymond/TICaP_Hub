package com.cyberace.ticaphub.ui.eventboard

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
import com.cyberace.ticaphub.TaskDetailsActivity
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

    val resultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
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
            //Run tests kung pano siya magreact kapag empty yung task lists/task board


        }
    }



    private fun fetchBoards(){
        lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getBoards(intent.getIntExtra("id", 0))
            } catch (e: IOException) {
                Log.e(tagName, e.message.toString())
                return@launch
            } catch (e: HttpException) {
                Log.e(tagName, "HttpException, unexpected response")
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.isEmpty()){
                    rvEventBoard.visibility = View.GONE
                    txtPromptEventboard.visibility = View.VISIBLE
                    imageViewEventBoard.visibility = View.VISIBLE
                }else{
                    eventBoardAdapter.taskLists = response.body()!!
                    rvEventBoard.visibility = View.VISIBLE
                    txtPromptEventboard.visibility = View.GONE
                    imageViewEventBoard.visibility = View.GONE
                }
            } else {
                Log.e(tagName, "Response not successful")
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
                    .setTitle("Enter Board Name")
                    .setView(inflater)
                    .setPositiveButton("Submit"){_,_ ->

                        val tag = "AddBoardDialog"
                        lifecycleScope.launch {
                            val response = try {
                                RetrofitInstance.api.addBoard(
                                    inflater.inputBoardName.text.toString(),
                                    intent.getIntExtra("id", 0),
                                    getSharedPreferences("loginCredential", Context.MODE_PRIVATE).getInt("userID", 0))
                            } catch(e: IOException) {
                                Log.e(tag, e.message.toString())
                                return@launch
                            } catch (e: HttpException) {
                                Log.e(tag, "HttpException, unexpected response")
                                return@launch
                            }
                            if(response.isSuccessful && response.body()!!.sqlResponse == "201") {
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

        val popupMenu = PopupMenu(this,  imgViewPopup).apply {
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
                                            inflater.inputBoardName.text.toString(),
                                            clickedItem.id,
                                            getSharedPreferences("loginCredential", Context.MODE_PRIVATE).getInt("userID", 0))
                                        //Add user id, created date, etc. Or whatevah
                                    } catch(e: IOException) {
                                        Log.e(tag, e.message.toString())
                                        return@launch
                                    } catch (e: HttpException) {
                                        Log.e(tag, "HttpException, unexpected response")
                                        return@launch
                                    }
                                    if(response.isSuccessful && response.body()!!.sqlResponse == "201") {
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

                    else -> return@setOnMenuItemClickListener true
                }
            }
        }
        popupMenu.show()
    }

    override fun onNestedItemClick(taskID: Int) {
        val intent = Intent(this, TaskDetailsActivity::class.java).apply {
            putExtra("id", taskID)
        }
        resultContract.launch(intent)
    }
}