package com.cyberace.ticaphub.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.RetrofitInstance
import com.cyberace.ticaphub.TaskDetailsActivity
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

        //ADD LOGIC TO ONLY GET TASKS FOR THE SPECIFIC USER USING USER ID
        fetchTasks()

        refreshLayoutHome.setOnRefreshListener {
            refreshLayoutHome.isRefreshing = true
            fetchTasks()
            refreshLayoutHome.isRefreshing = false
        }

    }

    override fun onItemClick(position: Int) {
        val clickedItem = taskAdapter.todos[position]

        Intent(activity, TaskDetailsActivity::class.java).apply {
            putExtra("id", clickedItem.id)
            startActivity(this)
        }
    }

    private fun fetchTasks(){
        viewLifecycleOwner.lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getTasks()
            } catch (e: IOException) {
                Log.e(tagName, e.message.toString())
                return@launch
            } catch (e: HttpException) {
                Log.e(tagName, "HttpException, unexpected response")
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.isEmpty()){
                    rvHome.visibility = View.GONE
                    txtPromptHome.visibility = View.VISIBLE
                    imageViewHome.visibility = View.VISIBLE
                }else{
                    rvHome.visibility = View.VISIBLE
                    txtPromptHome.visibility = View.GONE
                    imageViewHome.visibility = View.GONE
                    taskAdapter.todos = response.body()!!
                }
            } else {
                Log.e(tagName, "Response not successful")
            }
        }
    }


}