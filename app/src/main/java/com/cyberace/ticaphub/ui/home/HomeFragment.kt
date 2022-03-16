package com.cyberace.ticaphub.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberace.ticaphub.LoginActivity
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.RetrofitInstance
import com.cyberace.ticaphub.ui.taskDetails.TaskDetailsActivity
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


class HomeFragment : Fragment(R.layout.fragment_home),
    HomeAdapter.OnItemClickListener {

    private val taskAdapter = HomeAdapter(this)
    private val tagName = "Home Fragment"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        rvHome.adapter = taskAdapter
        rvHome.layoutManager = LinearLayoutManager(activity)

        fetchTasks()

        refreshLayoutHome.setOnRefreshListener {
            fetchTasks()
        }

    }

    override fun onItemClick(position: Int) {
        val clickedItem = taskAdapter.todos[position]

        Intent(activity, TaskDetailsActivity::class.java).apply {
            putExtra("taskID", clickedItem.id)
            putExtra("committeeID", clickedItem.committee_id)
            startActivity(this)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchTasks(){
        refreshLayoutHome.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getAssignedTasks(
                    "Bearer " +
                    this@HomeFragment
                        .requireActivity().getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                        .getString("userToken", "0")
                )
            } catch (e: IOException) {
                refreshLayoutHome.isRefreshing = false
                rvHome.visibility = View.GONE
                txtPromptHome.text = "IO Error: Failed to connect to the server"
                txtPromptHome.visibility = View.VISIBLE
                imageViewHome.visibility = View.VISIBLE
                Log.e(tagName, "IO Error:" + e.message.toString())
                return@launch
            } catch (e: HttpException) {
                refreshLayoutHome.isRefreshing = false
                rvHome.visibility = View.GONE
                txtPromptHome.text = "HTTP Error: Failed to connect to the server"
                txtPromptHome.visibility = View.VISIBLE
                imageViewHome.visibility = View.VISIBLE
                Log.e(tagName, "HTTP Error:" + e.message.toString())
                return@launch
            } catch (e: JsonSyntaxException) {
                refreshLayoutHome.isRefreshing = false
                Toast.makeText(requireActivity(), "Token Expired. Login Again.", Toast.LENGTH_LONG).show()
                val sharedPref = this@HomeFragment.requireActivity().getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
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
                refreshLayoutHome.isRefreshing = false
                when{
                    response.body()!!.tasks == null && response.body()!!.committee_tasks != null && response.body()!!.committee_tasks.isNotEmpty() -> {
                        rvHome.visibility = View.VISIBLE
                        txtPromptHome.visibility = View.GONE
                        imageViewHome.visibility = View.GONE
                        taskAdapter.todos = response.body()!!.committee_tasks
                    }
                    response.body()!!.tasks != null && response.body()!!.tasks.isNotEmpty() && response.body()!!.committee_tasks == null -> {
                        rvHome.visibility = View.VISIBLE
                        txtPromptHome.visibility = View.GONE
                        imageViewHome.visibility = View.GONE
                        taskAdapter.todos = response.body()!!.tasks
                    }
                    else -> {
                        rvHome.visibility = View.GONE
                        txtPromptHome.visibility = View.VISIBLE
                        imageViewHome.visibility = View.VISIBLE
                    }
                }

//TODO MIGHT DELETE THE LINES BELOW SOON
//                if (response.body()!!.tasks == null && response.body()!!.committee_tasks.isEmpty()){
//                    rvHome.visibility = View.GONE
//                    txtPromptHome.visibility = View.VISIBLE
//                    imageViewHome.visibility = View.VISIBLE
//                }else if(response.body()!!.tasks.isEmpty() && response.body()!!.committee_tasks == null){
//                    rvHome.visibility = View.GONE
//                    txtPromptHome.visibility = View.VISIBLE
//                    imageViewHome.visibility = View.VISIBLE
//                }
//                else{
//                    rvHome.visibility = View.VISIBLE
//                    txtPromptHome.visibility = View.GONE
//                    imageViewHome.visibility = View.GONE
//                    taskAdapter.todos = response.body()!!.tasks
//                }
            } else {
                refreshLayoutHome.isRefreshing = false
                val msg= "Response not successful"
                rvHome.visibility = View.GONE
                txtPromptHome.text = msg
                txtPromptHome.visibility = View.VISIBLE
                imageViewHome.visibility = View.VISIBLE
                Log.e(tagName, msg)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}