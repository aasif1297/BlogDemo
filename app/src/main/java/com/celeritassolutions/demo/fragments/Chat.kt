package com.celeritassolutions.demo.activities

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.adapter.ChatAdapter
import com.celeritassolutions.demo.fragments.NewChat
import com.celeritassolutions.demo.model.ChatList
import com.celeritassolutions.demo.model.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class Chat : Fragment() {

    private var recycler: RecyclerView? = null
    private var btn_fab: FloatingActionButton? = null
    private var recycler_adapter: ChatAdapter? = null
    private var usersList: MutableList<ChatList>? = null
    private var mAuth = FirebaseAuth.getInstance()
    private var firestore = FirebaseFirestore.getInstance()
    private lateinit var mCurrentUser: String
    private lateinit var mUsers: MutableList<UsersModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_chat,container,false)
        recycler = root.findViewById(R.id.chat_recycler)
        btn_fab = root.findViewById(R.id.fab)
        mCurrentUser = mAuth.currentUser!!.uid

        val linearLayoutManager = LinearLayoutManager(context)

        usersList = ArrayList()
        mUsers = ArrayList()
        linearLayoutManager.stackFromEnd = true
        recycler!!.layoutManager = linearLayoutManager
        recycler_adapter = ChatAdapter(context!!, mUsers)
        recycler!!.adapter = recycler_adapter
        firestore.collection("chatlist")
            .addSnapshotListener(activity!!, object : EventListener<QuerySnapshot> {
            override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
                if (documentSnapshots != null) {
                    if (!documentSnapshots.isEmpty) {
                        for (doc in documentSnapshots.documentChanges) {
                            if (doc.type === DocumentChange.Type.ADDED) {
                                val chatList = doc.document.toObject(ChatList::class.java)
                                usersList!!.add(chatList)
                            }
                        }
                    }
                    Log.d("#CHATLIST", "${usersList?.size}")
                    chatList()
                }
            }
        })
        return root
    }

    /*firestore.collection("users").addSnapshotListener { documentSnapshot, e ->
            when {
                e != null -> Log.e("ERROR", e.message)
                documentSnapshot != null
                        && !documentSnapshot.isEmpty ->{
                    with(documentSnapshot) {
                        for (doc in this.documentChanges) {
                            if (doc.type === DocumentChange.Type.ADDED) {
                                mUsers.clear()

                                val users = doc.document.toObject(UsersModel::class.java)
                                Log.d("#users", "access")
                                for (chatlist in usersList!!) {

                                    if (users.user_id == chatlist.id) {
                                        mUsers.add(users)
                                    }
                                }
                            }
                        }
                        recycler_adapter!!.notifyDataSetChanged()
                    }
                }
            }
        }*/

    private fun chatList() {

        firestore.collection("users").addSnapshotListener(activity!!, object : EventListener<QuerySnapshot> {
            override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {


                        for (doc in documentSnapshots!!.documentChanges) {
                            if (doc.type === DocumentChange.Type.ADDED) {
                                mUsers.clear()

                                val users = doc.document.toObject(UsersModel::class.java)

                                for (chatlist in usersList!!) {
                                    Log.d("#users", "access")
                                    if (users.user_id == chatlist.id) {
                                        mUsers.add(users)
                                    }
                                }
                            }
                }
                recycler_adapter!!.notifyDataSetChanged()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_fab?.setOnClickListener {
            val fragmentManager = fragmentManager
            val ft = fragmentManager!!.beginTransaction()
            ft.replace(R.id.content_frame,NewChat())
            ft.addToBackStack(null)
            ft.commit()
        }
    }
}
