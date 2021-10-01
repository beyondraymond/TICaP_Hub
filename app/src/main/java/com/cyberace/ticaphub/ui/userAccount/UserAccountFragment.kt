package com.cyberace.ticaphub.ui.userAccount

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.cyberace.ticaphub.LoginActivity
import com.cyberace.ticaphub.R
import kotlinx.android.synthetic.main.fragment_user_account.*

class UserAccountFragment : Fragment(R.layout.fragment_user_account){

    private val tagName = "Home Fragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val sharedPref = this.requireActivity().getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        btnLogout.setOnClickListener {
            editor.clear()
            editor.apply()

            Intent(requireActivity(), LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(this)
            }
        }
    }



}


