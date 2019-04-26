package com.celeritassolutions.demo.activities


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.celeritassolutions.demo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_setup_account.*
import java.io.File

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.tasks.Task

@GlideModule
class App : AppGlideModule()

class Account: AppCompatActivity() {

    private var mainUril : Uri? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageReference : StorageReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var user_id: String
    private var ischanged: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_account)

        firebaseAuth = FirebaseAuth.getInstance()
        user_id = firebaseAuth.currentUser!!.uid
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        user_profile.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
                    )
                } else {
                    selectImage()
                }
            } else {
                selectImage()
            }
        }

        setup_progress.visibility = View.VISIBLE
        btn_setup.isEnabled = false
        firestore.collection("users").document(user_id).get().addOnCompleteListener {
            if (it.isSuccessful){
                if (it.result!!.exists()){
                    val user_id = it.result!!.getString("user_id")
                    val user_name = it.result!!.getString("user_name")
                    val img_path = it.result!!.getString("img_path")
                    val user_des = it.result!!.getString("user_description")

                    mainUril = Uri.parse(img_path)

                    var placeholder : RequestOptions = RequestOptions.placeholderOf(R.drawable.default_image)

                    GlideApp.with(this)
                        .setDefaultRequestOptions(placeholder).asBitmap()
                        .load(img_path)
                        .into(object: SimpleTarget<Bitmap>(){
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                constraint.setImageBitmap(resource)
                            }
                        })
                    Glide.with(this@Account)
                        .setDefaultRequestOptions(placeholder)
                        .load(img_path)
                        .into(user_profile)

                    tv_name.setText(user_name)
                    tv_des.setText(user_des)
                    Toast.makeText(this,"Data Exists..!! :)",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Error: ${it.exception!!.message}",Toast.LENGTH_SHORT).show()
            }
            setup_progress.visibility = View.GONE
            btn_setup.isEnabled = true
        }



        btn_setup.setOnClickListener {
            setup_progress.visibility = View.VISIBLE
            if (ischanged) {
                if (!TextUtils.isEmpty(tv_name.text) && mainUril != null) {
                    user_id = firebaseAuth.currentUser!!.uid
                    var img_path = storageReference.child("profile_images").child("$user_id.jpg")

                    img_path.putFile(mainUril!!).addOnFailureListener {
                        setup_progress.visibility = View.GONE
                        Toast.makeText(this@Account, "upload failed: " + it.message, Toast.LENGTH_SHORT).show()
                    }.addOnSuccessListener { taskSnapshot ->
                        // success
                        img_path.downloadUrl.addOnCompleteListener { taskSnapshot ->
                            storeDataToFireStore(taskSnapshot)
                        }
                    }
                }
            }else{
                storeDataToFireStore(null)
            }
    }
    }

    private fun storeDataToFireStore(taskSnapShot: Task<Uri>?) {
        val url: Uri

        if (taskSnapShot != null){
            url = taskSnapShot.result!!
        }else{
            url = mainUril!!
        }

        Log.d("FireTAG","$url")
        val items = HashMap<String, Any>()
        items["user_id"] = user_id
        items["img_path"] = url.toString()
        items["user_name"] = tv_name.text.toString()
        items["user_description"] = tv_des.text.toString()
        firestore.collection("users").document(user_id)
            .set(items)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val intent = Intent(this@Account, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    setup_progress.visibility = View.GONE
                    Toast.makeText(this,"Account Updated Successfully..",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@Account, "Fire Store Error: " + it.exception!!.message, Toast.LENGTH_SHORT).show()
                    setup_progress.visibility = View.GONE
                }
            }
    }

    private fun selectImage() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mainUril = result.uri
                user_profile.setImageURI(mainUril)
                val f = File(getRealPathFromURI(mainUril!!))
                val d = Drawable.createFromPath(f.absolutePath)
                constraint.background = d
                ischanged = true
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun getRealPathFromURI(contentURI: Uri): String? {
        val cursor: Cursor? =  contentResolver.query(contentURI, null, null, null, null)
        return if (cursor == null) { // Source is Dropbox or other similar local file path
            contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(idx)
        }
    }

}