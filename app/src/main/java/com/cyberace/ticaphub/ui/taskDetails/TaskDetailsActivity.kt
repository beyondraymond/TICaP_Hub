package com.cyberace.ticaphub.ui.taskDetails

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_task_details.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.RetrofitInstance
import com.cyberace.ticaphub.model.TaskCardClass
import kotlinx.android.synthetic.main.activity_events.*
import kotlinx.android.synthetic.main.dialog_add_new_board.view.*
import kotlinx.android.synthetic.main.dialog_choose_task_list.*
import kotlinx.android.synthetic.main.dialog_choose_task_list.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.net.URLConnection
import android.provider.OpenableColumns
import android.database.Cursor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import android.content.ContentResolver
import android.content.Intent
import com.cyberace.ticaphub.UploadRequestBody
import okhttp3.MultipartBody
import kotlin.collections.ArrayList


class TaskDetailsActivity : AppCompatActivity(),
    TaskDetailsAdapter.OnItemClickListener, UploadRequestBody.UploadCallback {

    private val commentsAdapter = TaskDetailsAdapter(this, this)
    private lateinit var fetchedTask: TaskCardClass
    private var requestBody: UploadRequestBody? = null

    private val startActivityResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK){
            fetchTask()
        }
    }

    @SuppressLint("SetTextI18n")
    private val resultContract = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
        ActivityResultCallback {

            if (it?.toString() != null) {
                //Get the File Name of the selected file
                val uriString = it.toString()
                var displayName: String? = null
                val myFile = File(uriString)
                if (uriString.startsWith("content://")) {
                    var cursor: Cursor? = null
                    try {
                        cursor = this.contentResolver.query(it, null, null, null, null)
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName =
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        }
                    } finally {
                        cursor!!.close()
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.name
                }

                linearLayoutFileUploaded.visibility = View.VISIBLE
                txtViewFileName.text = displayName

                //Get the content type of the selected File
                val contentResolver: ContentResolver = this.contentResolver
                val type = contentResolver.getType(it)!!

                if (Build.VERSION.SDK_INT >= 19){
                    val parcelFileDescriptor =
                        contentResolver.openFileDescriptor(it, "r", null) ?: return@ActivityResultCallback

                    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                    val file = File(cacheDir, displayName!!)
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)

                    progressBar1.visibility = View.VISIBLE
                    progressBar1.progress = 0
                    Log.e("CONTENT-TYPE", type)
                    requestBody = UploadRequestBody(file, type, this)

                }else{
                    txtViewFileName.text = "File uploading is unavailable in the current Android Version"
                    Toast.makeText(this@TaskDetailsActivity, "File uploading is unavailable in the current Android Version", Toast.LENGTH_LONG).show()
                }
            }

        }
    )

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        fetchTask()
        rvActivity.adapter = commentsAdapter
        rvActivity.layoutManager = LinearLayoutManager(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        refreshLayoutTaskDetails.setOnRefreshListener {
            refreshLayoutTaskDetails.isRefreshing = true
            fetchTask()
            refreshLayoutTaskDetails.isRefreshing = false
        }
        refreshLayoutEmptyList.setOnRefreshListener {
            refreshLayoutEmptyList.isRefreshing = true
            fetchTask()
            refreshLayoutEmptyList.isRefreshing = false
        }

        btnSendComment.setOnClickListener {
            if(editTxtComment.text.isEmpty()){
                Toast.makeText(this@TaskDetailsActivity, "Error: Activity Report field is empty.", Toast.LENGTH_LONG).show()
            }else{
                lifecycleScope.launch {
                    val tagName = "SendComment1-getEvent"
                    val response = try {
                        RetrofitInstance.api.getEventID(
                            "Bearer " + this@TaskDetailsActivity
                                .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                .getString("userToken", "0"),
                            fetchedTask.id)
                    } catch (e: IOException) {
                        Log.e(tagName, e.message.toString())
                        return@launch
                    } catch (e: HttpException) {
                        Log.e(tagName, "HttpException, unexpected response")
                        return@launch
                    }
                    if (response.isSuccessful && response.body() != null) {
                        sendComment(response.body()!!.event_id)
                        return@launch
                    } else {
                        Log.e(tagName, "Response not successful with code " + response.code())
                    }
                }
            }
        }

        imgViewUploadFile.setOnClickListener {
            resultContract.launch(arrayOf("*/*"))
         }

        btnCancelUpload.setOnClickListener {
            requestBody = null
            txtViewFileName.text = ""
            linearLayoutFileUploaded.visibility = View.GONE
            progressBar1.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.taskdetails_action_bar_menu, menu)
        if(::fetchedTask.isInitialized && !isCreator(fetchedTask)){
            menu?.findItem(R.id.menu_move_task)?.isVisible = false
            menu?.findItem(R.id.menu_update_task)?.isVisible = false
            menu?.findItem(R.id.menu_delete_task)?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when(item.itemId) {
            android.R.id.home -> {
                this.finish()
            }

            R.id.menu_move_task -> {

                val tagName = "MoveTaskLevel1"
                //Getting event ID first from the list ID
                lifecycleScope.launch {
                    val response = try {
                        RetrofitInstance.api.getEventID(
                            "Bearer " + this@TaskDetailsActivity
                                .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                .getString("userToken", "0"),
                            fetchedTask.id)
                    } catch (e: IOException) {
                        Log.e(tagName, e.message.toString())
                        return@launch
                    } catch (e: HttpException) {
                        Log.e(tagName, "HttpException, unexpected response")
                        return@launch
                    }
                    if (response.isSuccessful && response.body() != null) {
                        fetchBoards(response.body()!!.event_id)
                        return@launch
                    } else {
                        Log.e(tagName, "Response not successful with code " + response.code())
                    }
                }
            }

            R.id.menu_refresh_data -> {
                refreshLayoutTaskDetails.isRefreshing = true
                fetchTask()
                refreshLayoutTaskDetails.isRefreshing = false
            }

            R.id.menu_update_task -> {
                val intent = Intent(this, UpdateTaskActivity::class.java).apply {
                    putExtra("taskID", fetchedTask.id)
                    putExtra("listID", fetchedTask.list_id)
                    putExtra("taskTitle", fetchedTask.title)
                    putExtra("taskDesc", fetchedTask.description)
                    putIntegerArrayListExtra("taskMembers", ArrayList(fetchedTask.users.map { it.id }))
                }
                startActivityResultContract.launch(intent)
            }

            R.id.menu_delete_task -> {

                AlertDialog.Builder(this@TaskDetailsActivity)
                    .setTitle("Confirm Task List Deletion")
                    .setMessage("\"" + fetchedTask.title + "\"" + " will be deleted permanently.")
                    .setPositiveButton("Delete"){_,_ ->

                        val tag = "RenameEventDialog"

                        lifecycleScope.launch {
                            val response = try {
                                RetrofitInstance.api.deleteTask(
                                    "Bearer " + this@TaskDetailsActivity
                                        .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                                        .getString("userToken", "0"),
                                    fetchedTask.id
                                )
                            } catch(e: IOException) {
                                Log.e(tag, e.message.toString())
                                return@launch
                            } catch (e: HttpException) {
                                Log.e(tag, "HttpException, unexpected response")
                                return@launch
                            }
                            if(response.isSuccessful && response.code() == 200) {
                                Toast.makeText(this@TaskDetailsActivity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK)
                                this@TaskDetailsActivity.finish()
                                return@launch
                            } else {
                                Toast.makeText(this@TaskDetailsActivity,"Operation failed with code: " + response.code(), Toast.LENGTH_SHORT).show()
                                Log.e(tag, "Error on Response")
                                return@launch
                            }
                        }
                    }
                    .setNegativeButton("Cancel"){_, _ ->
                        Toast.makeText(this@TaskDetailsActivity, "Operation cancelled.", Toast.LENGTH_SHORT).show()
                    }
                    .create()
                    .show()
            }

        }
        return super.onOptionsItemSelected(item)
    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun fetchTask(){
        val tag = "TaskDetailsGetTask"
        var dateSQLFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSz")
        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a")

        if (Build.VERSION.SDK_INT >= 24){
            dateSQLFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
        }


        lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getTask(
                    "Bearer " + this@TaskDetailsActivity
                        .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                        .getString("userToken", "0"),
                    intent.getIntExtra("taskID", 0)
                )
            } catch(e: IOException) {
                txtTaskTitle.text = "IO Error: Failed to connect to the server"
                Log.e(tag, "IO Error:" + e.message.toString())
                return@launch
            } catch (e: HttpException) {
                txtTaskTitle.text = "HTTP Error: Unexpected response from the server"
                Log.e(tag, "HTTP Error:" + e.message.toString())
                return@launch
            }
            if(response.isSuccessful && response.body() != null) {
                fetchedTask = response.body()!!
                txtTaskTitle.text = fetchedTask.title
                txtTaskDesc.text = fetchedTask.description

                try {
                    val taskDate = dateSQLFormat.parse(fetchedTask.updated_at)!!
                    txtLastUpdated.text = "Last Updated: " + dateFormatter.format(taskDate)
                } catch (e: Exception) {
                    Log.e("Date Parse", e.message.toString())
                }

                if (fetchedTask.activities.isEmpty()){
                    refreshLayoutTaskDetails.visibility = View.GONE
                    refreshLayoutEmptyList.visibility = View.VISIBLE
                }else{
                    refreshLayoutEmptyList.visibility =  View.GONE
                    refreshLayoutTaskDetails.visibility = View.VISIBLE
                    commentsAdapter.comments = fetchedTask.activities
                    rvActivity.scrollToPosition(commentsAdapter.comments.size-1)
                }
                //Check if user is member of task before allowing to comment
                if(!isMemberOrCreator(fetchedTask)){
                    editTxtComment.hint = "Comment Disabled: \n You're not a member of this task"
                    btnSendComment.isEnabled = false
                    editTxtComment.isEnabled = false
                    imgViewUploadFile.isEnabled = false
                }

                invalidateOptionsMenu()
                return@launch
            } else {
                txtTaskTitle.text = "Response not successful"
                Log.e("Retrofit-TaskDetails", response.errorBody().toString())
            }
        }
    }

    private fun fetchBoards(eventID: Int){
        val tagName = "fbTaskDetails"
        lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.getBoards(
                    "Bearer " + this@TaskDetailsActivity
                        .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                        .getString("userToken", "0"),
                    eventID)
            } catch (e: IOException) {
                Toast.makeText(this@TaskDetailsActivity, "IO Error: Failed to connect to the server", Toast.LENGTH_SHORT).show()
                Log.e(tagName, "IO Error:" + e.message.toString())
                return@launch
            } catch (e: HttpException) {
                Toast.makeText(this@TaskDetailsActivity, "HTTP Error: Unexpected response from the server", Toast.LENGTH_SHORT).show()
                Log.e(tagName, "HTTP Error:" + e.message.toString())
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {

                val taskLists= response.body()!!.lists.toMutableList()
                for (list in taskLists){
                    if(list.id == fetchedTask.list_id){
                        taskLists.remove(list)
                        break
                    }
                }

                val inflater = this@TaskDetailsActivity.layoutInflater.inflate(R.layout.dialog_choose_task_list,null)
                inflater.spinnerTaskLists.adapter = ArrayAdapter(this@TaskDetailsActivity,
                    R.layout.support_simple_spinner_dropdown_item, taskLists.map { it.title })
                AlertDialog.Builder(this@TaskDetailsActivity)
                    .setTitle("Move Task To Another List")
                    .setView(inflater)
                    .setPositiveButton("Submit"){_,_ ->
                        taskLists.forEach {
                            if (it.title == inflater.spinnerTaskLists.selectedItem.toString()){
                                moveTask(it.id)
                            }
                        }
                    }
                    .create()
                    .show()
            } else {
                val msg = "Response not successful"
                Toast.makeText(this@TaskDetailsActivity, msg, Toast.LENGTH_SHORT).show()
                Log.e(tagName, msg)
            }
        }
    }

    private fun moveTask(listID: Int){
        val tagName = "MoveTaskFunction"
        lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.updateTask(
                    "Bearer " + this@TaskDetailsActivity
                        .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                        .getString("userToken", "0"),
                    fetchedTask.id,
                    fetchedTask.title,
                    fetchedTask.description,
                    fetchedTask.users.map { it.id },
                    listID
                )
            } catch (e: IOException) {
                Log.e(tagName, e.message.toString())
                return@launch
            } catch (e: HttpException) {
                Log.e(tagName, "HttpException, unexpected response")
                return@launch
            }
            if(response.isSuccessful && response.code() == 200) {
                Toast.makeText(this@TaskDetailsActivity, "Operation Completed: " + response.body()!!.message , Toast.LENGTH_SHORT).show()
                fetchTask()
                setResult(RESULT_OK)
                return@launch
            } else {
                Toast.makeText(this@TaskDetailsActivity, "Operation Failed", Toast.LENGTH_SHORT).show()
                Log.e(tagName, "Response not successful " + response.code() )
                return@launch
            }
        }
    }

    private fun sendComment(eventID: Int){
        val part: MultipartBody.Part? = if(requestBody != null &&
            txtViewFileName.text != "File uploading is unavailable in the current Android Version" &&
            txtViewFileName.text != ""){
            MultipartBody.Part.createFormData("files[0]", txtViewFileName.text.toString(), requestBody!!)
        }else{
            null
        }

        val tag = "Send Comment"
        lifecycleScope.launch {
            val response = try {
                RetrofitInstance.api.addActivity(
                    "Bearer " + this@TaskDetailsActivity
                        .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                        .getString("userToken", "0"),
                    eventID, fetchedTask.list_id,
                    intent.getIntExtra("taskID", 0),
                    editTxtComment.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                     part
                )
            } catch(e: IOException) {
                Toast.makeText(this@TaskDetailsActivity, "Operation Failed: " + e.message, Toast.LENGTH_SHORT).show()
                Log.e(tag, e.message.toString())
                return@launch
            } catch (e: HttpException) {
                Toast.makeText(this@TaskDetailsActivity, "Operation Failed: HttpException, unexpected response", Toast.LENGTH_SHORT).show()
                Log.e(tag, "HttpException, unexpected response")
                return@launch
            }
            if(response.isSuccessful && response.code() == 200) {

                progressBar1.progress = 100
                linearLayoutFileUploaded.visibility = View.GONE
                requestBody = null
                progressBar1.visibility = View.GONE

                editTxtComment.text.clear()

                if(editTxtComment.requestFocus()){
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(editTxtComment.applicationWindowToken, 0)
                }
                fetchTask()

                commentsAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        (rvActivity.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(positionStart, 0)
                    }
                })

                return@launch
            } else {
                Toast.makeText(this@TaskDetailsActivity, "Operation Failed", Toast.LENGTH_SHORT).show()
                Log.e(tag, "Error on Response: " + response.code())
                return@launch
            }
        }
    }

    private fun isCreator(selectedTask: TaskCardClass): Boolean{
        val userID = this@TaskDetailsActivity
            .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
            .getInt("userID", 0)
        return userID == selectedTask.user_id
    }

    private fun isMemberOrCreator(selectedTask: TaskCardClass): Boolean{
        val userID = this@TaskDetailsActivity
            .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
            .getInt("userID", 0)
        if(userID == selectedTask.user_id){
            return true
        }
        for (id in selectedTask.users.map { it.id }){
            if (id == userID){
                return true
            }
        }
        return false
    }




    override fun onFileClick(position: Int) {
        val downloadBaseUrl = "https://www.ticaphub.com/api/download/"
        val clickedItem = fetchedTask.activities[position].files[fetchedTask.activities[position].files.lastIndex]
        val urlRequest = downloadBaseUrl + clickedItem.path

        val request = DownloadManager.Request(Uri.parse(urlRequest))
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cookie = CookieManager.getInstance().getCookie(urlRequest)

        request.setTitle(clickedItem.name)
        request.setDescription("Downloading File Attachments.")
        request.addRequestHeader("Authorization", "Bearer " + this@TaskDetailsActivity
            .getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
            .getString("userToken", "0"))
        request.addRequestHeader("cookie", cookie)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, clickedItem.name)
        request.setMimeType(getMimeFromURLRequest(urlRequest))
        downloadManager.enqueue(request)

        Toast.makeText(this, "Downloading Attachment", Toast.LENGTH_SHORT).show()
    }

    private fun getMimeFromURLRequest(url: String): String {
        return URLConnection.guessContentTypeFromName(url)
    }

    override fun onProgressUpdate(percentage: Int) {
        progressBar1.progress = percentage
    }
}