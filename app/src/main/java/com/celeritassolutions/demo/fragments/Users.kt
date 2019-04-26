package com.celeritassolutions.demo.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import com.bumptech.glide.request.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.celeritassolutions.demo.App
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.activities.Account
import com.celeritassolutions.demo.activities.LoginActivity
import com.celeritassolutions.demo.adapter.MyPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_users.*

class Users : Fragment() {

    private var mainUril : Uri? = null
    private var firebaseAuth = FirebaseAuth.getInstance()
    private var storageReference = FirebaseStorage.getInstance().reference
    private var firestore = FirebaseFirestore.getInstance()
    private var user_id= firebaseAuth.currentUser!!.uid
    private var ischanged: Boolean = false
    private lateinit var setupprogress: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_users, container, false)
        setupprogress = root.findViewById(R.id.setup_progress)
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentAdapter = MyPagerAdapter(childFragmentManager)
        viewpager_main.adapter = fragmentAdapter
        tabs_main.setupWithViewPager(viewpager_main)

        setupprogress.visibility = View.VISIBLE
        firestore.collection("users").document(user_id).get().addOnCompleteListener {
            setupprogress.visibility = View.VISIBLE
            if (it.isSuccessful){
                if (it.result!!.exists()){
                    val user_id = it.result!!.getString("user_id")
                    val user_name = it.result!!.getString("user_name")
                    val img_path = it.result!!.getString("img_path")
                    val user_des = it.result!!.getString("user_description")

                    mainUril = Uri.parse(img_path)

                    var placeholder : RequestOptions = RequestOptions.placeholderOf(R.drawable.default_image)

                    Glide.with(App.context!!)
                        .setDefaultRequestOptions(placeholder).asBitmap()
                        .load(img_path)
                        .into(object: SimpleTarget<Bitmap>(){
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                constraint.setImageBitmap(resource)
                            }
                        })
                    Glide.with(App.context!!)
                        .setDefaultRequestOptions(placeholder)
                        .load(img_path)
                        .into(user_profile)

                    tv_name.text = user_name
                    tv_des.text = user_des
                    Toast.makeText(App.context,"Data Exists..!! :)",Toast.LENGTH_SHORT).show()
                    setupprogress.visibility = View.GONE
                }
            }else{
                setupprogress.visibility = View.GONE
                Toast.makeText(App.context!!,"Error: ${it.exception!!.message}",Toast.LENGTH_SHORT).show()
            }
            setupprogress.visibility = View.GONE
        }

        user_profile.setOnClickListener {
            val intent = Intent(activity, Account::class.java)
            activity!!.startActivity(intent)
            activity!!.finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth!!.currentUser
        if (currentUser == null){
            sendToLogin()
        }
    }

    private fun sendToLogin() {
        val intent = Intent(activity!!, LoginActivity::class.java)
        startActivity(intent)
        activity!!.finish()
    }
}