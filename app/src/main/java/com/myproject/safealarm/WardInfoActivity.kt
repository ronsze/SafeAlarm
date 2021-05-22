package com.myproject.safealarm

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.myproject.safealarm.databinding.ActivityWardInfoBinding
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class WardInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWardInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWardInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        spinnerSet()
        if(App.prefs.onWardInfo){
            loadInfo()
        }

        binding.save.setOnClickListener {
            saveInfo()
        }
        binding.cancle.setOnClickListener {
            finish()
        }
        binding.imageView.setOnClickListener {
            var intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(intent, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 101){
            if(resultCode == RESULT_OK){
                if(data != null){
                    var fileuri: Uri = data.data!!
                    var resolver = contentResolver
                    try{
                        var instream = resolver.openInputStream(fileuri)
                        var imgBitmap = BitmapFactory.decodeStream(instream)
                        binding.imageView.setImageBitmap(imgBitmap)
                        instream!!.close()
                        saveBitmapToJpeg(imgBitmap)
                    }catch(e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun saveBitmapToJpeg(bitmap: Bitmap){
        var tempFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${App.prefs.id}.png")
        try{
            tempFile.createNewFile()
            var out = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
        }catch(e: Exception){
            e.printStackTrace()
        }
    }

    private fun spinnerSet(){
        val sAdapter = ArrayAdapter.createFromResource(this, R.array.sex, android.R.layout.simple_spinner_dropdown_item)
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sexSpin.adapter = sAdapter
        if(App.prefs.sex == "남"){
            binding.sexSpin.setSelection(0)
        }else{
            binding.sexSpin.setSelection(1)
        }

    }

    private fun loadInfo(){
        try{
            var imgPath = "${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/${App.prefs.id}.png"
            var bm = BitmapFactory.decodeFile(imgPath)
            binding.imageView.setImageBitmap(bm)
        }catch(e: Exception){
            e.printStackTrace()
        }

        binding.nameEdit.setText(App.prefs.name)
        binding.heightEdit.setText(App.prefs.height)
        binding.ageEdit.setText(App.prefs.age)
        binding.phoneEdit.setText(App.prefs.number)
        binding.extraEdit.setText(App.prefs.extra)
    }

    private fun saveInfo(){
        val image = binding.imageView.drawable.toBitmap()
        val default = getDrawable(R.drawable.loading_img)!!.toBitmap()
        if(binding.nameEdit.text.toString() == ""
            || binding.heightEdit.text.toString() == ""
            || binding.ageEdit.text.toString() == ""
            || binding.phoneEdit.text.toString() == ""
            || image.equals(default)){
            Toast.makeText(this, "기타 특징 외의 모든 정보를\n입력하셔야 저장이 가능합니다.", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
            App.prefs.onWardInfo = true
            App.prefs.name = binding.nameEdit.text.toString()
            App.prefs.height = binding.heightEdit.text.toString()
            App.prefs.age = binding.ageEdit.text.toString()
            App.prefs.number = binding.phoneEdit.text.toString()
            if(binding.sexSpin.selectedItemPosition == 0){
                App.prefs.sex = "남"
            }else{
                App.prefs.sex = "여"
            }
            App.prefs.extra = binding.extraEdit.text.toString()
            finish()
        }
    }
}