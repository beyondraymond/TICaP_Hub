package com.cyberace.ticaphub

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private val tag = "Login Act:"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_TICaPHub_NoActionBar)
        setContentView(R.layout.activity_login)

        val sharedPref = getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val userID = sharedPref.getInt("userID", 0)
        val userFullName = sharedPref.getString("userFullName", null)

        //val intent = Intent(this, MainActivity::class.java)

        if (userID != 0 && userFullName != null){
            Intent(this, MainActivity::class.java).apply {
                putExtra("id", userID)
                startActivity(this)
            }
        }else{
            constraintLayoutLoginActivity.visibility = View.VISIBLE
        }

        btnLogin.setOnClickListener {
            lifecycleScope.launch {
                val response = try {
                    RetrofitInstance.api.getUser(editEmail.text.toString(), editPassword.text.toString())
                } catch(e: IOException) {
                    txtErrorMessage.text = "Your email or password is incorrect."
                    Log.e(tag + "IOException", e.message.toString())
                    return@launch
                } catch (e: HttpException) {
                    txtErrorMessage.text = "Your email or password is incorrect."
                    Log.e(tag + "HttpException", "HttpException, unexpected response")
                    return@launch
                }
                if(response.isSuccessful && response.body() != null) {

                    editor.putInt("userID", response.body()!!.user_id)
                    editor.putString("userFullName", response.body()!!.full_name)
                    editor.apply()

                    Intent(this@LoginActivity, MainActivity::class.java).apply {
                        putExtra("id", userID)
                        startActivity(this)
                    }
                } else {
                    txtErrorMessage.text = "Your email or password is incorrect."
                    return@launch
                }
            }
        }

        txtForgotPassword.setOnClickListener {
            val intent2 = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent2)
        }
    }
}