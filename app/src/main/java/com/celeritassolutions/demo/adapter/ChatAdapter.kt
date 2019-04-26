package com.celeritassolutions.demo.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.activities.GlideApp
import com.celeritassolutions.demo.model.UsersModel
import kotlinx.android.synthetic.main.fragment_chat.view.*
import java.util.zip.Inflater

class ChatAdapter(val context: Context, var list:MutableList<UsersModel>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
       var userName: TextView = itemView.findViewById(R.id.user_name)
        var userImage: ImageView = itemView.findViewById(R.id.profile_image)
    }


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ChatAdapter.ViewHolder {
        val v  = LayoutInflater.from(p0.context).inflate(R.layout.chat_recycler,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ChatAdapter.ViewHolder, position: Int) {
        //holder.setIsRecyclable(false)
        val item = list[position]
        holder.userName.text = item.user_name
        GlideApp.with(holder.itemView.context)
            .load(item.img_path)
            .placeholder(R.drawable.default_image)
            .into(holder.userImage)
    }
}