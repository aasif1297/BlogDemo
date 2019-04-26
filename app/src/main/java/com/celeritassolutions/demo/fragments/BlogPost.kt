package com.celeritassolutions.demo.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.celeritassolutions.demo.App
import com.celeritassolutions.demo.R
import com.celeritassolutions.demo.activities.LoginActivity
import com.celeritassolutions.demo.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.fragment_blog.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

class BlogPost : Fragment() {
    private var mainUril : Uri? = null
    private var storageReference = FirebaseStorage.getInstance().getReference()
    private var firebaseFirestore = FirebaseFirestore.getInstance()
    private var firebaseAuth = FirebaseAuth.getInstance()
    private var mCurrentUser: String = firebaseAuth.currentUser!!.uid
    private lateinit var progress: ProgressBar
    private lateinit var compressedImageFile: Bitmap
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_blog,container,false)
        progress = root.findViewById(R.id.post_progress)
        progress.visibility = View.GONE
        return  root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        img_post.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(context!!.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(App.context!!, "Permission Denied", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(
                        this.activity!!,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
                    )
                } else {
                    selectImage()
                    Toast.makeText(App.context!!, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            } else {
                selectImage()
                Toast.makeText(App.context!!, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
        }

        btn_post.setOnClickListener {
            if (!TextUtils.isEmpty(post_title.text) && !TextUtils.isEmpty(post_description.text) && mainUril != null){
                progress.visibility = View.VISIBLE
                val randonName = UUID.randomUUID().toString()
                val newimage = File(mainUril!!.path)
                try {
                    compressedImageFile = Compressor(this.activity)
                        .setMaxHeight(720)
                        .setMaxWidth(720)
                        .setQuality(50)
                        .compressToBitmap(newimage)
                }catch (e: IOException){
                    e.printStackTrace()
                }

                val byte = ByteArrayOutputStream()
                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, byte)
                val imagedata = byte.toByteArray()

                val ref= storageReference.child("post_images").child("$randonName.jpg")
                val filepath = ref.putBytes(imagedata)


                filepath.addOnFailureListener {
                    Toast.makeText(this.activity,"Error: ${it.message}",Toast.LENGTH_SHORT).show()
                }.addOnSuccessListener {
                    val downloadurl = ref.downloadUrl
                    if (it.task.isSuccessful){
                        val newthumbfile = File(mainUril!!.path)
                        try {
                            compressedImageFile = Compressor(this.activity)
                                .setMaxHeight(100)
                                .setMaxWidth(100)
                                .setQuality(1)
                                .compressToBitmap(newthumbfile)
                        }catch (e:IOException){
                            e.printStackTrace()
                        }
                        val byte = ByteArrayOutputStream()
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, byte)
                        val thumbdata = byte.toByteArray()

                        val uploadTask: UploadTask = storageReference.child("post_images/thumbs")
                            .child("$randonName.jpg").putBytes(thumbdata)

                        uploadTask.addOnFailureListener {
                            Toast.makeText(this.activity,"Error: ${it.message}",Toast.LENGTH_SHORT).show()
                        }.addOnSuccessListener {
                            val downloadthumburl = downloadurl.result
                            Log.d("TAGTHUMG","Thumb: $downloadthumburl")
                            val items = HashMap<String, Any>()
                            items["image_url"] = downloadurl.result.toString()
                            items["image_thumb"] = downloadthumburl.toString()
                            items["post_title"] = post_title.text.toString()
                            items["post_description"] = post_description.text.toString()
                            items["user_id"] = mCurrentUser
                            items["timestamp"] = FieldValue.serverTimestamp()

                            firebaseFirestore.collection("Posts").add(items)
                                .addOnCompleteListener {
                                    if (it.isSuccessful){
                                        Toast.makeText(App.context!!, "Post was added", Toast.LENGTH_LONG).show()
                                        val intent = Intent(activity, MainActivity::class.java)
                                        activity!!.startActivity(intent)
                                        activity!!.finish()
                                    }
                                    progress.visibility = View.GONE
                                }.addOnFailureListener {
                                    Toast.makeText(App.context!!,"FireStore Error: ${it.message}",Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }

            }else{
                progress.visibility = View.GONE
                Toast.makeText(App.context!!,"All Fields Are mandatory",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null){
            sendToLogin()
        }
    }

    private fun sendToLogin() {
        val intent = Intent(activity!!, LoginActivity::class.java)
        startActivity(intent)
        activity!!.finish()
    }

    private fun selectImage() {
        CropImage.activity()
            .setMinCropResultSize(512,512)
            .setAspectRatio(1,1)
            .start(context!!, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mainUril = result.uri
                img_post.setImageURI(mainUril)
                //user_profile.setImageURI(mainUril)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }
}

