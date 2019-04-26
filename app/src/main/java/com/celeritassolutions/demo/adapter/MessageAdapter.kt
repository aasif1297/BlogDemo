package com.celeritassolutions.demo.adapter

import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.celeritassolutions.demo.App
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.activities.GlideApp
import com.celeritassolutions.demo.activities.SendMessage
import com.celeritassolutions.demo.model.MessageModel
import com.celeritassolutions.demo.model.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MessageAdapter (var list:MutableList<MessageModel>, var image_url: String) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    var firebaseFirestore = FirebaseFirestore.getInstance()
    var firebaseAuth = FirebaseAuth.getInstance()
    var user : FirebaseUser? = null

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var image : ImageView = itemView.findViewById(R.id.img)
        var message: TextView = itemView.findViewById(R.id.show_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == MESSAGE_TYPE_RIGHT) {
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item_right, parent, false)
        }else{
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item_left, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val item : MessageModel = list[position]
        val currentUserID = firebaseAuth.currentUser!!.uid
        val userID = item.reciever
        GlideApp.with(holder.itemView.context)
            .load(image_url)
            .placeholder(R.drawable.default_image)
            .into(holder.image)
        holder.message.text = item.message
    }

    override fun getItemViewType(position: Int): Int {
        user = firebaseAuth.currentUser
        if (list[position].sender == user!!.uid) return MESSAGE_TYPE_RIGHT else MESSAGE_TYPE_LEFT
        return super.getItemViewType(position)


    }

    override fun getItemCount() = list.size

    companion object {
        const val MESSAGE_TYPE_LEFT = 0
        const val MESSAGE_TYPE_RIGHT = 1
    }

}