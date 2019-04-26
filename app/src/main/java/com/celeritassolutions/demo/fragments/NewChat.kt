package com.celeritassolutions.demo.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.activities.LoginActivity
import com.celeritassolutions.demo.adapter.ChatAdapter
import com.celeritassolutions.demo.adapter.UsersAdapter
import com.celeritassolutions.demo.model.BlogPost
import com.celeritassolutions.demo.model.MessageModel
import com.celeritassolutions.demo.model.Messages
import com.celeritassolutions.demo.model.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class NewChat : Fragment() {

    private var recycler: RecyclerView? = null
    private var list: MutableList<UsersModel>? = null

    private var firebaseFirestore: FirebaseFirestore? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var chatRecyclerAdapter: UsersAdapter? = null

    private var lastVisible: DocumentSnapshot? = null
    private var isFirstPageFirstLoad: Boolean? = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_new_chat, container, false)
        recycler = root.findViewById(R.id.chat_users)
        list = ArrayList()
        firebaseAuth = FirebaseAuth.getInstance()
        chatRecyclerAdapter = UsersAdapter(list!!)
        recycler!!.layoutManager = LinearLayoutManager(container!!.context)
        recycler!!.adapter = chatRecyclerAdapter
        recycler!!.setHasFixedSize(true)

        if (firebaseAuth!!.currentUser != null) {

            firebaseFirestore = FirebaseFirestore.getInstance()

            recycler!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val reachedBottom = !recyclerView.canScrollVertically(1)

                    if (reachedBottom) {

                        loadMorePost()

                    }

                }
            })

            val firstQuery =
                firebaseFirestore!!.collection("users").orderBy("user_name", Query.Direction.ASCENDING).limit(3)
            firstQuery.addSnapshotListener(activity!!, object : EventListener<QuerySnapshot> {
                override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (!documentSnapshots!!.isEmpty) {

                        if (isFirstPageFirstLoad!!) {

                            lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
                            list!!.clear()

                        }

                        for (doc in documentSnapshots.documentChanges) {

                            if (doc.type === DocumentChange.Type.ADDED) {

                                val id = doc.document.id
                                val users = doc.document.toObject(UsersModel::class.java)

                                if (isFirstPageFirstLoad!!) {

                                    list!!.add(users)
                                    //Log.d("USERS", "${list!![doc.newIndex]}")

                                } else {

                                    list!!.add(0, users)
                                    //Log.d("USERS", list!![0].user_name)
                                }


                                chatRecyclerAdapter!!.notifyDataSetChanged()

                            }
                        }

                        isFirstPageFirstLoad = false

                    }

                }
            })

        }

        // Inflate the layout for this fragment
        return root
    }

    fun loadMorePost() {

        if (firebaseAuth!!.currentUser != null) {

            val nextQuery = firebaseFirestore!!.collection("users")
                .orderBy("user_name", Query.Direction.ASCENDING)
                .startAfter(lastVisible!!)

            nextQuery.addSnapshotListener(activity!!, object : EventListener<QuerySnapshot> {
                override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (!documentSnapshots!!.isEmpty) {

                        lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
                        for (doc in documentSnapshots.documentChanges) {

                            if (doc.type === DocumentChange.Type.ADDED) {
                                val users = doc.document.toObject(UsersModel::class.java)
                                list!!.add(users)

                                chatRecyclerAdapter!!.notifyDataSetChanged()
                            }

                        }
                    }
                }


            })

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