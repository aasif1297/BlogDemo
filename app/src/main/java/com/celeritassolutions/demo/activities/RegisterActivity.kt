package com.celeritassolutions.demo.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.fragments.Users
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        reg_btn.setOnClickListener {
            var email = reg_email.text.toString()
            var pass = reg_pass.text.toString()
            var c_pass = reg_confirm_pass.text.toString()

            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(c_pass)){
                reg_progress.visibility = View.VISIBLE
                if (pass == c_pass){
                    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener {task->
                        if (task.isSuccessful){
                            sendToMain()
                        }else{
                            Toast.makeText(this,"Error: ${task.exception!!.message}",Toast.LENGTH_LONG).show()
                        }
                    }
                }else{
                    Toast.makeText(this,"Passwords are not matching",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentuser = mAuth.currentUser
        if (currentuser != null){
            sendToMain()
        }
    }

    private fun sendToMain() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        startActivity(intent)
    }
}
