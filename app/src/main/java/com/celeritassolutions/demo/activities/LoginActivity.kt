package com.celeritassolutions.demo.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.celeritassolutions.demo.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        login.setOnClickListener {
            val stringEmail = email.text.toString()
            val stringPass = password.text.toString()

            if (!TextUtils.isEmpty(stringEmail) && !TextUtils.isEmpty(stringPass)){
                mAuth.signInWithEmailAndPassword(stringEmail,stringPass).addOnCompleteListener { task->
                    if (task.isSuccessful){
                        val intent = Intent(this@LoginActivity,MainActivity::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this,"Error: ${task.exception!!.message}",Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Toast.makeText(this,"Fill all Fields",Toast.LENGTH_LONG).show()
            }
        }

        register.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        val currentuser = mAuth.currentUser

        if (currentuser != null){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }else{

        }
    }
}
