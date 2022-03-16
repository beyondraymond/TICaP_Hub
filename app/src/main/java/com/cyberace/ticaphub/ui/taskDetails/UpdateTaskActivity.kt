package com.cyberace.ticaphub.ui.taskDetails

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.RetrofitInstance
import com.cyberace.ticaphub.model.User
import kotlinx.android.synthetic.main.activity_task_details.*
import kotlinx.android.synthetic.main.activity_update_task.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.widget.ArrayAdapter
import android.widget.Toast
import com.cyberace.ticaphub.model.Ticap


class UpdateTaskActivity : AppCompatActivity(),
    UpdateTaskAdapter.OnItemClickListener {

    val membersAdapter = UpdateTaskAdapter(this)
    lateinit var fetchedOfficers: List<User>
    var membersMutableList = mutableListOf<User>()
    var nonMembersMutableList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_task)

        supportActionBar?.title = "Update Task Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fetchOfficers()

        rvMembers.adapter = membersAdapter
        rvMembers.layoutManager = LinearLayoutManager(this)

        editTaskTitle.setText(intent.getStringExtra("taskTitle"))
        editTaskDescription.setText(intent.getStringExtra("taskDesc"))

        btnAddMember.setOnClickListener {
            val ticap = Ticap(0, "", 0, 0,
                0,0,0,0,
                0,0,"","",0)
            var selectedUser = User(0, "",
                "", "", "", 0, "", "", "",
            0, listOf(), listOf(), "", listOf(), ticap)
            nonMembersMutableList.forEach {
                if(it.first_name + " " + it.middle_name.substring(0,1) + " " + it.last_name == autoCompleteTextView.text.toString()){
                    selectedUser = it
                }
            }
            if(selectedUser.id != 0){
                membersMutableList.add(selectedUser)
                nonMembersMutableList.remove(selectedUser)
                membersAdapter.notifyDataSetChanged()
                Log.e("User Added", selectedUser.first_name)
                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                    this@UpdateTaskActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    nonMembersMutableList.map { it.first_name + " " + it.middle_name.substring(0,1) + " " + it.last_name }
                )
                autoCompleteTextView.setAdapter(adapter)
                autoCompleteTextView.setText("")
            }

        }
        btnUpdateTask.setOnClickListener {
            if(editTaskTitle.text.toString() == "" ){
                txtErrorMessage.text = "Please enter a valid Task Title"
            }else if (editTaskDescription.text.toString() == "" ||
            editTaskDescription.text.toString() == resources.getString(R.string.default_task_desc)){
                txtErrorMessage.text = "Please enter a valid Task Description"
            } else{
                txtErrorMessage.text = ""
                val tagName = "UpdateTaskOnClick"
                lifecycleScope.launch {
                    val response = try {
                        RetrofitInstance.api.updateTask(
                            "Bearer " + this@UpdateTaskActivity
                                .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                .getString("userToken", "0"),
                            intent.getIntExtra("taskID", 0),
                            editTaskTitle.text.toString(),
                            editTaskDescription.text.toString(),
                            membersMutableList.map { it.id },
                            intent.getIntExtra("listID", 0),
                        )
                    } catch (e: IOException) {
                        Log.e(tagName, e.message.toString())
                        return@launch
                    } catch (e: HttpException) {
                        Log.e(tagName, "HttpException, unexpected response")
                        return@launch
                    }
                    if(response.isSuccessful && response.code() == 200) {
                        Toast.makeText(this@UpdateTaskActivity,  response.body()!!.message , Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        return@launch
                    } else {
                        Toast.makeText(this@UpdateTaskActivity, "Operation Failed", Toast.LENGTH_SHORT).show()
                        Log.e(tagName, "Response not successful " + response.code() )
                        return@launch
                    }
                }

            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun fetchOfficers() {

        val tag = "FetchOfficer-"
        lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getOfficers(
                    "Bearer " + this@UpdateTaskActivity
                        .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                        .getString("userToken", "0")
                )
            } catch (e: IOException) {
                txtTaskTitle.text = "IO Error: Failed to connect to the server"
                Log.e(tag, "IO Error:" + e.message.toString())
                return@launch
            } catch (e: HttpException) {
                txtTaskTitle.text = "HTTP Error: Unexpected response from the server"
                Log.e(tag, "HTTP Error:" + e.message.toString())
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                fetchedOfficers = response.body()!!

                response.body()!!.forEach {
                    if(intent.getIntegerArrayListExtra("taskMembers")!!.contains(it.id)){
                        membersMutableList.add(it)
                    }else if(this@UpdateTaskActivity
                            .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                            .getInt("userID", 0) == it.id){

                    }else{
                        nonMembersMutableList.add(it)
                    }
                }

                membersAdapter.officers = membersMutableList

                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                    this@UpdateTaskActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    nonMembersMutableList.map { it.first_name + " " + it.middle_name.substring(0,1) + " " + it.last_name }
                )

                autoCompleteTextView.setAdapter(adapter)

                return@launch
            } else {
                Log.e(tag+"OnFailure", response.errorBody().toString())
            }

        }
    }

    override fun onRemoveBtnClick(position: Int) {
        val clickedUser = membersMutableList[position]
        nonMembersMutableList.add(clickedUser)
        membersMutableList.remove(clickedUser)
        membersAdapter.notifyDataSetChanged()

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this@UpdateTaskActivity,
            android.R.layout.simple_dropdown_item_1line,
            nonMembersMutableList.map { it.first_name + " " + it.middle_name.substring(0,1) + " " + it.last_name }
        )
        autoCompleteTextView.setAdapter(adapter)

        Log.e("REMOVED ITEM", clickedUser.first_name + " at position" + position)
    }

}