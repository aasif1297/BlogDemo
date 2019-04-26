package com.celeritassolutions.demo.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.adapter.CalenderAdapter

class Calender: Fragment() {
    lateinit var recycler : RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_calender,container,false)
        recycler = root.findViewById(R.id.calender_recycler)
        recycler.layoutManager = LinearLayoutManager(activity)
        recycler.adapter = CalenderAdapter(activity!!.applicationContext)
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}