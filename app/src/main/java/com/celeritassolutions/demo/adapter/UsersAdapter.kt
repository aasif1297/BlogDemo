package com.celeritassolutions.demo.adapter
/*
import android.content.Context
import android.graphics.Color
import android.os.Message
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.celeritassolutions.demo.App
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.activities.GlideApp
import com.celeritassolutions.demo.model.Messages

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.nav_header_main.view.*

class UsersAdapter(private val mMessageList: List<Messages>) :
    RecyclerView.Adapter<UsersAdapter.MessageViewHolder>() {

    private var mUserDatabase: DatabaseReference? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {

        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_users, parent, false)

        return MessageViewHolder(v)

    }

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var messageText: TextView
        var profileImage: CircleImageView
        var displayName: TextView
        var messageImage: ImageView

        init {

            messageText = view.findViewById(R.id.txt_des) as TextView
            profileImage = view.findViewById(R.id.thumb_img) as CircleImageView
            displayName = view.findViewById(R.id.txt_name) as TextView
            messageImage = view.findViewById(R.id.message_image_layout) as ImageView

        }
    }


    override fun onBindViewHolder(viewHolder: UsersAdapter.MessageViewHolder, i: Int) {

        val c = mMessageList[i]

        val from_user = c.from
        val message_type = c.type


        mUserDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(from_user!!)

        mUserDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val name = dataSnapshot.child("name").value!!.toString()
                val image = dataSnapshot.child("thumb_image").value!!.toString()

                viewHolder.displayName.text = name
                GlideApp.with(App.context!!)
                    .load(image)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.profileImage)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        if (message_type == "text") {

            viewHolder.messageText.setText(c.message)
            viewHolder.messageImage.visibility = View.INVISIBLE


        } else {

            viewHolder.messageText.visibility = View.INVISIBLE
            GlideApp.with(viewHolder.profileImage.context).load(c.message)
                .placeholder(R.drawable.placeholder).into(viewHolder.messageImage)

        }

    }

    override fun getItemCount(): Int {
        return mMessageList.size
    }


}*/

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
import com.celeritassolutions.demo.model.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UsersAdapter (var list:MutableList<UsersModel>) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {
    private var firebaseFirestore = FirebaseFirestore.getInstance()
    var firebaseAuth = FirebaseAuth.getInstance()!!

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var image : ImageView = itemView.findViewById(R.id.thumb_img)
        var name: TextView = itemView.findViewById(R.id.txt_name)
        var des: TextView = itemView.findViewById(R.id.txt_des)
        var layout: ConstraintLayout = itemView.findViewById(R.id.layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_users, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val item : UsersModel = list[position]
        val currentUserID = firebaseAuth.currentUser!!.uid
        val userID = item.user_id
        GlideApp.with(holder.itemView.context)
            .load(item.img_path)
            .placeholder(R.drawable.default_image)
            .into(holder.image)
        holder.name.text = item.user_name
        holder.des.text = item.user_description
        holder.layout.setOnClickListener {
          val intent = Intent(App.context!!,SendMessage::class.java)
            intent.putExtra("reciever_id",item.user_id)
            intent.putExtra("sender_id",currentUserID)
            intent.putExtra("image_url", item.img_path)
            App.context!!.startActivity(intent)
        }


        firebaseFirestore.collection("users").document(userID).get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    var userName = it.result!!.getString("user_name")
                }
            }
    }

    override fun getItemCount() = list.size

}