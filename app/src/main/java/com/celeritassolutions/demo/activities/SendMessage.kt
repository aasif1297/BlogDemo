package com.celeritassolutions.demo.activities


/*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Message
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.celeritassolutions.demo.GetTimeAgo
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.R.id.message_swipe_layout
import com.celeritassolutions.demo.adapter.UsersAdapter
import com.celeritassolutions.demo.model.Messages

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

import org.w3c.dom.Text

import java.util.ArrayList
import java.util.HashMap

import de.hdodenhof.circleimageview.CircleImageView

class SendMessage : AppCompatActivity() {

    private var mChatUser:String? = null
    private var mChatToolbar:Toolbar? = null

    private var mRootRef:DatabaseReference? = null

    private var mTitleView:TextView? = null
    private var mLastSeenView:TextView? = null
    private var mProfileImage:CircleImageView? = null
    private var mAuth:FirebaseAuth? = null
    private var mCurrentUserId:String? = null

    private var mChatAddBtn:ImageButton? = null
    private var mChatSendBtn:ImageButton? = null
    private var mChatMessageView:EditText? = null

    private var mMessagesList:RecyclerView? = null
    private var mRefreshLayout:SwipeRefreshLayout? = null

    private val messagesList = ArrayList<Messages>()
    private var mLinearLayout:LinearLayoutManager? = null
    private var mAdapter: UsersAdapter? = null
    private var mCurrentPage = 1

    // Storage Firebase
    private var mImageStorage:StorageReference? = null


    //New Solution
    private var itemPos = 0

    private var mLastKey:String? = ""
    private var mPrevKey:String? = ""


    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_chat_details)

        mRootRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        mCurrentUserId = mAuth?.currentUser?.uid
        Log.d("#FIRETAG","$mCurrentUserId")

        mChatUser = intent.getStringExtra("user_id")
        val userName = intent.getStringExtra("user_name")

        mAdapter = UsersAdapter(messagesList)

        mMessagesList = findViewById(R.id.message_recycler)
        mRefreshLayout = findViewById(message_swipe_layout)
        mLinearLayout = LinearLayoutManager(this)

        mMessagesList!!.setHasFixedSize(true)
        mMessagesList!!.layoutManager = mLinearLayout

        mMessagesList!!.adapter = mAdapter

        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().reference

       mRootRef!!.child("Chat").child(mCurrentUserId!!).child(mChatUser!!).child("seen").setValue(true)

        loadMessages()


        mTitleView!!.text = userName

        mRootRef!!.child("Users").child(mChatUser!!).addValueEventListener(object:ValueEventListener {
            override fun onDataChange(dataSnapshot:DataSnapshot) {

                val online = dataSnapshot.child("online").value!!.toString()
                val image = dataSnapshot.child("image").value!!.toString()

                if (online == "true")
                {

                    mLastSeenView!!.text = "Online"

                }
                else
                {

                    val getTimeAgo = GetTimeAgo()

                    val lastTime = java.lang.Long.parseLong(online)

                    val lastSeenTime = getTimeAgo.getTimeAgo(lastTime, applicationContext)

                    mLastSeenView!!.text = lastSeenTime

                }

            }

            override fun onCancelled(databaseError:DatabaseError) {

            }
        })


        mRootRef!!.child("Chat").child(mCurrentUserId!!).addValueEventListener(object:ValueEventListener {
            override fun onDataChange(dataSnapshot:DataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser!!))
                {

                    val chatAddMap = HashMap<String, Any>()
                    chatAddMap["seen"] = false
                    chatAddMap["timestamp"] = ServerValue.TIMESTAMP

                    val chatUserMap = HashMap<String, Any>()
                    chatUserMap["Chat/$mCurrentUserId/$mChatUser"] = chatAddMap
                    chatUserMap["Chat/$mChatUser/$mCurrentUserId"] = chatAddMap

                    mRootRef!!.updateChildren(chatUserMap
                    ) { databaseError, databaseReference ->
                        if (databaseError != null) {

                            Log.d("CHAT_LOG", databaseError.message)

                        }
                    }

                }

            }

            override fun onCancelled(databaseError:DatabaseError) {

            }
        })



        mChatSendBtn!!.setOnClickListener(object:View.OnClickListener {
            override fun onClick(view:View) {

                sendMessage()

            }
        })



        mChatAddBtn!!.setOnClickListener(object:View.OnClickListener {
            override fun onClick(view:View) {

                val galleryIntent = Intent()
                //galleryIntent.type = "image/*(*/remove)"
                galleryIntent.action = Intent.ACTION_GET_CONTENT

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK)

            }
        })



        mRefreshLayout!!.setOnRefreshListener(object:SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {

                mCurrentPage++

                itemPos = 0

                loadMoreMessages()


            }
        })


    }


    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK)
        {

            val imageUri = data!!.data

            val current_user_ref = "messages/$mCurrentUserId/$mChatUser"
            val chat_user_ref = "messages/$mChatUser/$mCurrentUserId"

            val user_message_push = mRootRef!!.child("messages")
                .child(mCurrentUserId!!).child(mChatUser!!).push()

            val push_id = user_message_push.key


            val filepath = mImageStorage!!.child("message_images").child(push_id!! + ".jpg")

            filepath.putFile(imageUri!!).addOnCompleteListener(object:OnCompleteListener<UploadTask.TaskSnapshot> {
                override fun onComplete(task:Task<UploadTask.TaskSnapshot>) {

                    if (task.isSuccessful)
                    {

                        val download_url = filepath.downloadUrl.result

                        val messageMap = HashMap<String, Any>()
                        messageMap["message"] = download_url.toString()
                        messageMap["seen"] = false
                        messageMap["type"] = "image"
                        messageMap["time"] = ServerValue.TIMESTAMP
                        messageMap["from"] = mCurrentUserId!!

                        val messageUserMap = HashMap<String, Any>()
                        messageUserMap["$current_user_ref/$push_id"] = messageMap
                        messageUserMap["$chat_user_ref/$push_id"] = messageMap

                        mChatMessageView!!.setText("")

                        mRootRef!!.updateChildren(messageUserMap, object:DatabaseReference.CompletionListener {
                            override fun onComplete(databaseError:DatabaseError?, databaseReference:DatabaseReference) {

                                if (databaseError != null)
                                {

                                    Log.d("CHAT_LOG", databaseError!!.message.toString())

                                }

                            }
                        })


                    }

                }
            })

        }

    }

    private fun loadMoreMessages() {

        val messageRef = mRootRef!!.child("messages").child(mCurrentUserId!!).child(mChatUser!!)

        val messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10)

        messageQuery.addChildEventListener(object:ChildEventListener {
            override fun onChildAdded(dataSnapshot:DataSnapshot, s:String?) {


                val message = dataSnapshot.getValue<Messages>(Messages::class.java!!)
                val messageKey = dataSnapshot.key

                if (mPrevKey != messageKey)
                {

                    messagesList.add(itemPos++, message!!)

                }
                else
                {

                    mPrevKey = mLastKey

                }


                if (itemPos == 1)
                {

                    mLastKey = messageKey

                }


                Log.d("TOTALKEYS", "Last Key : $mLastKey | Prev Key : $mPrevKey | Message Key : $messageKey")

                mAdapter!!.notifyDataSetChanged()

                mRefreshLayout!!.isRefreshing = false

                mLinearLayout!!.scrollToPositionWithOffset(10, 0)

            }

            override fun onChildChanged(dataSnapshot:DataSnapshot, s:String?) {

            }

            override fun onChildRemoved(dataSnapshot:DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot:DataSnapshot, s:String?) {

            }

            override fun onCancelled(databaseError:DatabaseError) {

            }
        })

    }

    private fun loadMessages() {

        val messageRef = mRootRef!!.child("messages").child(mCurrentUserId!!).child(mChatUser!!)

        val messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD)


        messageQuery.addChildEventListener(object:ChildEventListener {
            override fun onChildAdded(dataSnapshot:DataSnapshot, s:String?) {

                val message = dataSnapshot.getValue<Messages>(Messages::class.java!!)

                itemPos++

                if (itemPos == 1)
                {

                    val messageKey = dataSnapshot.key

                    mLastKey = messageKey
                    mPrevKey = messageKey

                }

                messagesList.add(message!!)
                mAdapter!!.notifyDataSetChanged()

                mMessagesList!!.scrollToPosition(messagesList.size - 1)

                mRefreshLayout!!.isRefreshing = false

            }

            override fun onChildChanged(dataSnapshot:DataSnapshot, s:String?) {

            }

            override fun onChildRemoved(dataSnapshot:DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot:DataSnapshot, s:String?) {

            }

            override fun onCancelled(databaseError:DatabaseError) {

            }
        })

    }

    private fun sendMessage() {


        val message = mChatMessageView!!.text.toString()

        if (!TextUtils.isEmpty(message))
        {

            val current_user_ref = "messages/$mCurrentUserId/$mChatUser"
            val chat_user_ref = "messages/$mChatUser/$mCurrentUserId"

            val user_message_push = mRootRef!!.child("messages")
                .child(mCurrentUserId!!).child(mChatUser!!).push()

            val push_id = user_message_push.key

            val messageMap = HashMap<String, Any>()
            messageMap.put("message", message)
            messageMap.put("seen", false)
            messageMap.put("type", "text")
            messageMap.put("time", ServerValue.TIMESTAMP)
            messageMap.put("from", mCurrentUserId!!)

            val messageUserMap = HashMap<String, Any>()
            messageUserMap.put("$current_user_ref/$push_id", messageMap)
            messageUserMap.put("$chat_user_ref/$push_id", messageMap)

            mChatMessageView!!.setText("")

            mRootRef!!.child("Chat").child(mCurrentUserId!!).child(mChatUser!!).child("seen").setValue(true)
            mRootRef!!.child("Chat").child(mCurrentUserId!!).child(mChatUser!!).child("timestamp").setValue(ServerValue.TIMESTAMP)

            mRootRef!!.child("Chat").child(mChatUser!!).child(mCurrentUserId!!).child("seen").setValue(false)
            mRootRef!!.child("Chat").child(mChatUser!!).child(mCurrentUserId!!).child("timestamp").setValue(ServerValue.TIMESTAMP)

            mRootRef!!.updateChildren(messageUserMap, object:DatabaseReference.CompletionListener {
                override fun onComplete(databaseError:DatabaseError?, databaseReference:DatabaseReference) {

                    if (databaseError != null)
                    {

                        Log.d("CHAT_LOG", databaseError!!.message.toString())

                    }

                }
            })

        }

    }

    companion object {

        private val TOTAL_ITEMS_TO_LOAD = 10

        private val GALLERY_PICK = 1
    }
}

*/

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.adapter.MessageAdapter
import com.celeritassolutions.demo.model.BlogPost
import com.celeritassolutions.demo.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_setup_account.*
import kotlinx.android.synthetic.main.fragment_chat_details.*

class SendMessage :AppCompatActivity(){

    private var mAuth = FirebaseAuth.getInstance()
    private var firestore = FirebaseFirestore.getInstance()
    private var storageReference = FirebaseStorage.getInstance().reference
    private lateinit var linearlayoutmanager: LinearLayoutManager
    private lateinit var messageAdapter: MessageAdapter
    private var recieverID : String? = null
    private var senderID : String? = null
    private var image_url: String? = null
    private lateinit var list:MutableList<MessageModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_chat_details)

        val intent = intent.extras
        recieverID = intent?.getString("reciever_id")
        senderID = intent?.getString("sender_id")
        image_url = intent?.getString("image_url")
        list = ArrayList()

        message_recycler.setHasFixedSize(true)
        linearlayoutmanager = LinearLayoutManager(this)
        linearlayoutmanager.stackFromEnd = true
        message_recycler.layoutManager = linearlayoutmanager
        readMessages(senderID!!, recieverID!!, image_url!!)

        btn_send.setOnClickListener {
            if (!TextUtils.isEmpty(text_content.text)){
                val items = HashMap<String, Any>()
                items["sender"] = senderID.toString()
                items["reciever"] = recieverID.toString()
                items["message"] = text_content.text.toString()
                firestore.collection("chats").document().set(items)
                val item1 = HashMap<String, Any>()
                item1["id"] = recieverID.toString()
                firestore.collection("chatlist/$senderID/$recieverID").document().set(item1)
                text_content.setText("")
            }else{
                Toast.makeText(this,"Message can't be empty",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun readMessages(myid: String, userid: String, image_url: String){
        val firstQuery = firestore.collection("chats")
        firstQuery.addSnapshotListener(this, object : EventListener<QuerySnapshot> {
            override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
                if (documentSnapshots != null) {
                    if (!documentSnapshots.isEmpty) {

                    for (doc in documentSnapshots.documentChanges) {
                        if (doc.type === DocumentChange.Type.ADDED) {
                            val blogPost = doc.document.toObject(MessageModel::class.java)
                            if (blogPost.reciever == myid && blogPost.sender == userid || blogPost.reciever == userid && blogPost.sender == myid) {
                                list.add(blogPost)
                            }
                            messageAdapter = MessageAdapter(list, image_url)
                            message_recycler.adapter = messageAdapter
                            messageAdapter.notifyDataSetChanged()
                        }
                    }
                    }
                }
            }
        })
    }
}
