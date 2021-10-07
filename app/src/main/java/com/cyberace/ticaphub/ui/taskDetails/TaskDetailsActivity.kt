package com.cyberace.ticaphub.ui.taskDetails

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_task_details.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.RetrofitInstance
import com.cyberace.ticaphub.model.TaskCardClass
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.dialog_add_new_board.view.*
import kotlinx.android.synthetic.main.dialog_choose_task_list.*
import kotlinx.android.synthetic.main.dialog_choose_task_list.view.*
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailsActivity : AppCompatActivity() {

    private val tag = "Task Details Activity"

    private lateinit var fetchedTask: TaskCardClass

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)


        val dateSQLFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a")

        //Add a logic na kapag nai-tap yung task desccription, makikita yung full view ng task and kapag officer yung user, pwede niya i-edit

        lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getTask(intent.getIntExtra("id", 0))
            } catch(e: IOException) {
                Log.e(tag, e.message.toString())
                return@launch
            } catch (e: HttpException) {
                Log.e(tag, "HttpException, unexpected response")
                return@launch
            }
            if(response.isSuccessful && response.body() != null) {
                fetchedTask = response.body()!!
                txtTaskTitle.text = fetchedTask.title
                txtTaskDesc.setText(fetchedTask.description)
                try {
                    var taskDate = dateSQLFormat.parse(fetchedTask.created_at)
                    taskDate = dateSQLFormat.parse(fetchedTask.updated_at)
                    txtLastUpdated.text = "Last Updated: " + dateFormatter.format(taskDate)
                } catch (e: Exception) {
                    Log.e("Date Parse", e.message.toString())
                }

            } else {
                Log.e("Retrofit-TaskDetails", "Response not successful")
            }
        }

        val density = this.resources.displayMetrics.density

        txtTaskTitle.post {
            Log.d("LineCount", txtTaskTitle.lineCount.toString())
            if (txtTaskTitle.lineCount == 3) {
                Log.d("LineDP", (50 * density + 0.5f).toInt().toString())
                constraintTaskTitle.setPadding(
                    (10 * density + 0.5f).toInt(),
                    (50 * density + 0.5f).toInt(),
                    (10 * density + 0.5f).toInt(),
                    0
                )
            } else {
                Log.d("LineDP", (75 * density + 0.5f).toInt().toString())
                constraintTaskTitle.setPadding(
                    (10 * density + 0.5f).toInt(),
                    (75 * density + 0.5f).toInt(),
                    (10 * density + 0.5f).toInt(),
                    0
                )
            }

        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        btnDownload.setOnClickListener {
//            val urlRequest = baseUrl+txtFileName.text.toString()
//            val request = DownloadManager.Request(Uri.parse(urlRequest))
//            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            //val title = URLUtil.guessFileName(urlRequest, null, null)
//            //val cookie = CookieManager.getInstance().getCookie(urlRequest)
//
//            request.setTitle(txtFileName.text.toString())
//            request.setDescription("Downloading File Attachments.")
//            //request.addRequestHeader("cookie", cookie)
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, txtFileName.text.toString())
//            request.setAllowedOverMetered(true)
//            downloadManager.enqueue(request)
//
//            Toast.makeText(this, "Downloading Attachment", Toast.LENGTH_SHORT).show()
//
//        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.taskdetails_action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.finish()
            }

            R.id.menu_move_task -> {
                val tagName = "MoveTask"

                //Getting event ID first from the list ID
                lifecycleScope.launch {
                    val response = try {
                        RetrofitInstance.api.getEventID(fetchedTask.list_id)
                    } catch (e: IOException) {
                        Log.e(tagName, e.message.toString())
                        return@launch
                    } catch (e: HttpException) {
                        Log.e(tagName, "HttpException, unexpected response")
                        return@launch
                    }
                    if (response.isSuccessful && response.body() != null) {
                        fetchBoards(response.body()!!.sqlResponse.toInt())
                        return@launch
                    } else {
                        Log.e(tagName, "Response not successful")
                    }
                }

            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchBoards(eventID: Int){
        val tagName = "fbTaskDetails"
        lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getBoards(eventID)
            } catch (e: IOException) {
                Log.e(tagName, e.message.toString())
                return@launch
            } catch (e: HttpException) {
                Log.e(tagName, "HttpException, unexpected response")
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                val inflater = this@TaskDetailsActivity.layoutInflater.inflate(R.layout.dialog_choose_task_list,null)
                inflater.spinnerTaskLists.adapter = ArrayAdapter<String>(this@TaskDetailsActivity,
                    R.layout.support_simple_spinner_dropdown_item, response.body()!!.map { it.title })
                AlertDialog.Builder(this@TaskDetailsActivity)
                    .setTitle("Move Task To Another List")
                    .setView(inflater)
                    .setPositiveButton("Submit"){_,_ ->
                        response.body()!!.forEach {
                            if (it.title == inflater.spinnerTaskLists.selectedItem.toString()){
                                moveTask(it.id)
                            }
                        }
                    }
                    .create()
                    .show()
            } else {
                Log.e(tagName, "Response not successful")
            }
        }
    }

    private fun moveTask(listID: Int){
        val tagName = "MoveTaskFunction"
        lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.moveTask(intent.getIntExtra("id", 0), listID)
            } catch (e: IOException) {
                Log.e(tagName, e.message.toString())
                return@launch
            } catch (e: HttpException) {
                Log.e(tagName, "HttpException, unexpected response")
                return@launch
            }
            if(response.isSuccessful && response.body()!!.sqlResponse == "201") {
                //ITS FUNCTIONING NAMAN KASO HINDI NALILIPAT YUNG TASK SA ANOTHER BOARD MARS
                Toast.makeText(this@TaskDetailsActivity, "Operation Completed with Code " + response.body()!!.sqlResponse , Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                return@launch
            } else {
                Toast.makeText(this@TaskDetailsActivity, "Operation Failed: " + response.body()!!, Toast.LENGTH_SHORT).show()
                Log.e(tagName, "Response not successful")
                return@launch
                //Implement Refresh VIEW kapag nag back button ka sa swis
            }
        }
    }
}