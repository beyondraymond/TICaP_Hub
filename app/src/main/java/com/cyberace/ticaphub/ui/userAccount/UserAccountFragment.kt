package com.cyberace.ticaphub.ui.userAccount

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cyberace.ticaphub.LoginActivity
import com.cyberace.ticaphub.R
import com.cyberace.ticaphub.RetrofitInstance
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.dialog_add_new_board.view.*
import kotlinx.android.synthetic.main.fragment_user_account.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class UserAccountFragment : Fragment(R.layout.fragment_user_account){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val sharedPref = this.requireActivity().getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()


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
                    val sharedPref = this@UserAccountFragment.requireActivity().getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.clear()
                    editor.apply()

                    Intent(requireActivity(), LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(this)
                    }
                    return@launch
                }
                if(response.isSuccessful && response.body()!!.message == "Logged out") {
                    Toast.makeText(requireActivity(), "Logout Successful", Toast.LENGTH_SHORT).show()
                    editor.clear()
                    editor.apply()

                    Intent(requireActivity(), LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(this)
                    }
                    return@launch
                } else {
                    Toast.makeText(requireActivity(), "Server error with code: " + response.code() , Toast.LENGTH_SHORT).show()
                    editor.clear()
                    editor.apply()

                    Intent(requireActivity(), LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(this)
                    }
                    Log.e(tag, "Error on Response")
                    return@launch
                }
            }


        }
    }



}


