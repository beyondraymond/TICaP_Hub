package com.cyberace.ticaphub.ui.userAccount

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cyberace.ticaphub.LoginActivity
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.RetrofitInstance
import com.cyberace.ticaphub.UploadRequestBody
import com.google.gson.JsonSyntaxException
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_task_details.*
import kotlinx.android.synthetic.main.activity_update_task.*
import kotlinx.android.synthetic.main.adapter_comment.view.*
import kotlinx.android.synthetic.main.dialog_add_new_board.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_user_account.*
import kotlinx.android.synthetic.main.fragment_user_account.txtErrorMessage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class UserAccountFragment : Fragment(R.layout.fragment_user_account),
    UploadRequestBody.UploadCallback{

    private var requestBody: UploadRequestBody? = null

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
                        cursor = requireActivity().contentResolver.query(it, null, null, null, null)
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

                txtImageName.text = displayName
                Picasso.get().load(it).into(imgProfilePic)

                //Get the content type of the selected File
                val contentResolver: ContentResolver = requireActivity().contentResolver
                val type = contentResolver.getType(it)!!

                if (Build.VERSION.SDK_INT >= 19){
                    val parcelFileDescriptor =
                        contentResolver.openFileDescriptor(it, "r", null) ?: return@ActivityResultCallback

                    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                    val file = File(requireActivity().cacheDir, displayName!!)
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)

                    progressBar2.visibility = View.VISIBLE
                    progressBar2.progress = 0
                    Log.e("CONTENT-TYPE", type)
                    requestBody = UploadRequestBody(file, type, this)

                }else{
                    txtImageName.text = "File uploading is unavailable in the current Android Version"
                    Toast.makeText(requireContext(), "File uploading is unavailable in the current Android Version", Toast.LENGTH_LONG).show()
                }
            }

        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val sharedPref = this.requireActivity().getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        //API REQUEST TO GET THE USER'S NAME, PROFILE PIC
        viewLifecycleOwner.lifecycleScope.launch {
            val tagName = "FetchUserInfo4Update"
            val response = try {
                RetrofitInstance.api.getAssignedTasks(
                    "Bearer "+ sharedPref.getString("userToken", "0"),
                )
            } catch (e: IOException) {
                txtErrorMessage.text = "IO Error: Failed to connect to the server"
                Log.e(tagName, "IO Error:" + e.message.toString())
                return@launch
            } catch (e: HttpException) {
                txtErrorMessage.text = "HTTP Error: Failed to connect to the server"
                Log.e(tagName, "HTTP Error:" + e.message.toString())
                return@launch
            } catch (e: JsonSyntaxException) {
                Toast.makeText(requireActivity(), "Token Expired. Login Again.", Toast.LENGTH_LONG).show()
                editor.clear()
                editor.apply()

                Intent(requireActivity(), LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(this)
                }
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                editFirstName.setText(response.body()!!.first_name)
                editMiddleName.setText(response.body()!!.middle_name)
                editLastName.setText(response.body()!!.last_name)

                if(response.body()!!.profile_picture != null) {
                    Picasso.get()
                        .load("https://ticaphub.com/storage/" + response.body()!!.profile_picture)
                        .into(imgProfilePic)
                }

            } else {
                val msg= "Response not successful"
                txtErrorMessage.text = "$msg. User details cannot be fetched."
                Log.e(tagName, msg)
            }
        }


        btnLogout.setOnClickListener {

            lifecycleScope.launch {
                val tag = "LogoutBtn"
                val response = try {
                    RetrofitInstance.api.logoutUser("Bearer "+ sharedPref.getString("userToken", "0"))
                } catch(e: IOException) {
                    Log.e(tag, e.message.toString())
                    return@launch
                } catch (e: HttpException) {
                    Log.e(tag, "HttpException, unexpected response")
                    return@launch
                } catch (e: JsonSyntaxException) {
                    Toast.makeText(requireActivity(), "Token Expired. Login Again.", Toast.LENGTH_LONG).show()
                    editor.clear()
                    editor.apply()

                    Intent(requireActivity(), LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(this)
                    }
                    this@UserAccountFragment.requireActivity().finish()
                    return@launch
                }
                if(response.isSuccessful && response.code() == 200) {
                    Toast.makeText(requireActivity(), "Logout Successful", Toast.LENGTH_SHORT).show()
                    editor.clear()
                    editor.apply()

                    Intent(requireActivity(), LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(this)
                    }
                    this@UserAccountFragment.requireActivity().finish()
                    return@launch
                } else {
                    Toast.makeText(requireActivity(), "Server error with code: " + response.code() , Toast.LENGTH_SHORT).show()
                    editor.clear()
                    editor.apply()

                    Intent(requireActivity(), LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(this)
                    }
                    this@UserAccountFragment.requireActivity().finish()
                    Log.e(tag, "Error on Response")
                    return@launch
                }
            }

        }

        btnUpdate.setOnClickListener {
            if(editFirstName.text.toString() == "" || editMiddleName.text.toString() == "" ||
                editLastName.text.toString() == ""){
                txtErrorMessage.text = "Please make sure that all fields are not blank."
            }else{

                val part: MultipartBody.Part? = if(requestBody != null &&
                    txtImageName.text != "File uploading is unavailable in the current Android Version" &&
                    txtImageName.text != ""){
                    MultipartBody.Part.createFormData("profile_picture", txtImageName.text.toString(), requestBody!!)
                }else{
                    null
                }

                lifecycleScope.launch {
                    Log.e("Credentials", sharedPref.getInt("userID", 0).toString())
                    val tag = "UpdateBtn"
                    val response = try {
                        RetrofitInstance.api.updateUser(
                            "Bearer "+ sharedPref.getString("userToken", "0"),
                            sharedPref.getInt("userID", 0),
                            editFirstName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                            editMiddleName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                            editLastName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                            part
                        )

                    } catch(e: IOException) {
                        Log.e(tag, e.message.toString())
                        return@launch
                    } catch (e: HttpException) {
                        Log.e(tag, "HttpException, unexpected response")
                        return@launch
                    } catch (e: JsonSyntaxException) {
                        Toast.makeText(requireActivity(), "Token Expired. Login Again.", Toast.LENGTH_LONG).show()
                        editor.clear()
                        editor.apply()

                        Intent(requireActivity(), LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(this)
                        }
                        return@launch
                    }
                    if(response.isSuccessful && response.code() == 200) {

                        AlertDialog.Builder(requireContext())
                            .setTitle("User Details Updated Successfully")
                            .setMessage("Your personal information has been updated.")
                            .setPositiveButton("OK"){_,_ ->

                            }
                            .create()
                            .show()
                        return@launch
                    } else {
                        Toast.makeText(requireActivity(), "Server error with code: " + response.code() , Toast.LENGTH_SHORT).show()

                        Log.e(tag, "Error on Response")
                        return@launch
                    }
                }
            }
        }

        btnUploadImage.setOnClickListener {
            resultContract.launch(arrayOf("image/jpeg", "image/jpg", "image/png"))
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        progressBar2.progress = percentage
    }

}


