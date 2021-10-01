package com.cyberace.ticaphub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        btnResetPassword.setOnClickListener {

            //Validate email if it exist or it is a valid email address and output on the "txtPromptMessage"
            //Create PHP File to Retrieve Email Address and send Email to the user

            AlertDialog.Builder(this)
                .setTitle("Check your email")
                .setMessage("We have sent a password recover instructions to your email.")
                .setPositiveButton("Ok"){_,_ ->
                    this.finish()
                }
                .create()
                .show()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}