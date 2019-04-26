package com.celeritassolutions.demo.fragments

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.activities.LoginActivity
import com.celeritassolutions.demo.adapter.BlogAdapter
import com.celeritassolutions.demo.model.BlogPost
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

class Home : Fragment() {

    private var blog_list_view: RecyclerView? = null
    private var blog_list: MutableList<BlogPost>? = null

    private var firebaseFirestore: FirebaseFirestore? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var blogRecyclerAdapter: BlogAdapter? = null

    private var lastVisible: DocumentSnapshot? = null
    private var isFirstPageFirstLoad: Boolean? = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        blog_list = ArrayList()
        blog_list_view = view.findViewById(R.id.recyclerview)

        firebaseAuth = FirebaseAuth.getInstance()
        blogRecyclerAdapter = BlogAdapter(blog_list!!)
        blog_list_view!!.layoutManager = LinearLayoutManager(container!!.context)
        blog_list_view!!.adapter = blogRecyclerAdapter
        blog_list_view!!.setHasFixedSize(true)

        if (firebaseAuth!!.currentUser != null) {

            firebaseFirestore = FirebaseFirestore.getInstance()

            blog_list_view!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val reachedBottom = !recyclerView.canScrollVertically(1)

                    if (reachedBottom) {

                        loadMorePost()

                    }

                }
            })

            val firstQuery = firebaseFirestore!!.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3)
            firstQuery.addSnapshotListener(activity!!, object : EventListener<QuerySnapshot> {
                override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty) {

                            if (isFirstPageFirstLoad!!) {

                                lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
                                blog_list!!.clear()

                            }

                            for (doc in documentSnapshots.documentChanges) {

                                if (doc.type === DocumentChange.Type.ADDED) {

                                    val blogPostId = doc.document.id
                                    val blogPost = doc.document.toObject(BlogPost::class.java).withId<BlogPost>(blogPostId)

                                    if (isFirstPageFirstLoad!!) {

                                        blog_list!!.add(blogPost)

                                    } else {

                                        blog_list!!.add(0, blogPost)

                                    }


                                    blogRecyclerAdapter!!.notifyDataSetChanged()

                                }
                            }

                            isFirstPageFirstLoad = false

                        }
                    }

                }
            })

        }

        // Inflate the layout for this fragment
        return view
    }

    fun loadMorePost() {

        if (firebaseAuth!!.currentUser != null) {

            val nextQuery = firebaseFirestore!!.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible!!)
                .limit(3)

            nextQuery.addSnapshotListener(activity!!, object : EventListener<QuerySnapshot>{
                override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty) {

                            lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
                            for (doc in documentSnapshots.documentChanges) {

                                if (doc.type === DocumentChange.Type.ADDED) {

                                    val blogPostId = doc.document.id
                                    val blogPost = doc.document.toObject(BlogPost::class.java).withId<BlogPost>(blogPostId)
                                    blog_list!!.add(blogPost)

                                    blogRecyclerAdapter!!.notifyDataSetChanged()
                                }

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