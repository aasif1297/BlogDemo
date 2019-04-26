package com.celeritassolutions.demo.adapter

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.model.BlogPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BlogAdapter(var list:MutableList<BlogPost>) : RecyclerView.Adapter<BlogAdapter.ViewHolder>() {
    var firebaseFirestore = FirebaseFirestore.getInstance()
    var firebaseAuth = FirebaseAuth.getInstance()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var image : ImageView = itemView.findViewById(R.id.img_post)
        var title: TextView = itemView.findViewById(R.id.post_title)
        var layout: ConstraintLayout = itemView.findViewById(R.id.layout)
        var firebaseFirestore = FirebaseFirestore.getInstance()
        var firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.blog_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val item : BlogPost = list[position]
        //val blogpostID = item.BlogPostId
        //val currentUserID = firebaseAuth.currentUser!!.uid
        val userID = item.user_id
            Glide.with(holder.itemView.context)
            .load(item.image_url)
            .into(holder.image)
        holder.title.text = item.post_title

        firebaseFirestore.collection("users").document(userID).get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    var userName = it.result!!.getString("user_name")
                }
            }
    }

    override fun getItemCount() = list.size



}