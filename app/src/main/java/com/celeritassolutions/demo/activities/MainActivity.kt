package com.celeritassolutions.demo.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.widget.TextView
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.fragments.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var tool: android.support.v7.widget.Toolbar
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mCurrentUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)
        tool = findViewById(R.id.toolbar)
        toggle = ActionBarDrawerToggle(this, drawer_layout, null,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        //toolbar.inflateMenu(R.menu.activity_main_drawer)
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp)
        toolbar.hideOverflowMenu()
        toolbar.title = "DEMO"
        tool.changeToolbarFont()
        toolbar.setNavigationOnClickListener {
            drawer_layout.openDrawer(Gravity.START)
        }

        toolbar.setOnMenuItemClickListener {
            true
        }

        drawer_layout.addDrawerListener(toggle)


        nav_view.setNavigationItemSelectedListener(this)
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        displaySelectedScreen(R.id.nav_home)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser == null){
            sendToLogin()
        }else{
            mCurrentUser = mAuth.currentUser!!.uid
            firestore.collection("users").document(mCurrentUser).get().addOnCompleteListener {
                if (it.isSuccessful){
                    if (!it.result!!.exists()){
                        val fragmentManager = supportFragmentManager
                        val ft = fragmentManager.beginTransaction()
                        ft.replace(R.id.content_frame,Users())
                        ft.addToBackStack(null)
                        ft.commit()
                    }
                }
            }
        }
    }

    private fun sendToLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        displaySelectedScreen(item.itemId)
        return true
    }

    private fun displaySelectedScreen(itemId: Int) {
        var fragment: Fragment? = null
        when (itemId) {
            R.id.nav_home -> fragment = Home()
            R.id.nav_add_blog -> { fragment = BlogPost() }
            R.id.nav_calender -> fragment = Calender()
            R.id.nav_about -> fragment = About()
            R.id.nav_contact_us -> fragment = ContactUs()
            R.id.nav_users -> fragment = Users()
            R.id.nav_chat -> fragment = Chat()
            R.id.nav_log_out -> {
                logOut()
            }

        }
        if (fragment != null) {
            val fragmentManager = supportFragmentManager
            val ft = fragmentManager.beginTransaction()
            ft.replace(R.id.content_frame,fragment)
            ft.addToBackStack(null)
            ft.commit()
        }
        drawer_layout.closeDrawer(GravityCompat.START)
    }

    private fun logOut() {
        mAuth.signOut()
        sendToLogin()
    }


    private fun android.support.v7.widget.Toolbar.changeToolbarFont(){
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view is TextView && view.text == title) {
                view.typeface = Typeface.createFromAsset(view.context.assets, "fonts/myfont.ttf")
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP,20F)
                view.setTextColor(resources.getColor(R.color.call))
                break
            }
        }
    }
}
