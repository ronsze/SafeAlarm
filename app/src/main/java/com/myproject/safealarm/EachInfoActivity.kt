package com.myproject.safealarm

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.util.Base64Utils
import com.myproject.safealarm.databinding.ActivityEachInfoBinding
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.RuntimeException

class EachInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEachInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEachInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setView()

        binding.extra2Text.setOnClickListener {
            extraDialog(intent.getStringExtra("extra2")!!)
        }
    }

    private fun setView(){
        val intent = this.intent
        if(intent != null){
            var name = intent.getStringExtra("name")
            var sex = intent.getStringExtra("sex")
            var age = intent.getStringExtra("age")
            var height = intent.getStringExtra("height")
            var number = intent.getStringExtra("number")
            var looks = intent.getStringExtra("looks")
            var time = intent.getStringExtra("time")
            var loc = intent.getStringExtra("loc")
            var extra1 = intent.getStringExtra("extra1")
            binding.photo.setImageBitmap(loadCacheImg())
            binding.nameText.text = "이름 : ${name} (${age}세, ${sex}, 신장${height}cm"
            binding.phoneText.text = "연락처 : ${number}"
            binding.looksText.text = "인상착의 : ${looks}"
            binding.timeText.text = "실종일자 : ${time}"
            binding.locText.text = "마지막 위치 : ${loc}"
            binding.extra1Text.text = "신체 특징 : ${extra1}"
        }else{
            Log.e("씨발", "좆같네")
        }

    }

    private fun loadCacheImg(): Bitmap{
        try{
            var imgPath = "${cacheDir}/cache.png"
            var bm = BitmapFactory.decodeFile(imgPath)
            return bm
        }catch(e: Exception){
            throw RuntimeException(e)
        }
    }

    private fun extraDialog(extra: String){
        val builder = AlertDialog.Builder(this)

        builder.setTitle("기타 사항")
        builder.setMessage(extra)
        builder.setNegativeButton("확인", null)
        builder.show()
    }

    fun getBitmap(input: String): Bitmap {
        try {
            var arr = Base64Utils.decode(input)
            var bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.size)
            return bitmap
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}