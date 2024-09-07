package com.myproject.safealarm.feature.missing.each_info

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.myproject.safealarm.databinding.ActivityEachInfoBinding
import com.myproject.safealarm.feature.missing.map.MissingMapActivity
import java.lang.Exception
import java.lang.RuntimeException

class EachInfoActivity : AppCompatActivity() {
    private var context = this

    private lateinit var binding: ActivityEachInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEachInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setView()

        binding.locText.setOnClickListener {
            var mIntent = Intent(context, MissingMapActivity::class.java)
            val loc = intent.getStringExtra("loc")
            mIntent.putExtra("loc", loc)
            startActivity(mIntent)
        }

        binding.extra2Text.setOnClickListener {
            extraDialog(intent.getStringExtra("extra2")!!)
        }
    }

    private fun setView(){
        val intent = this.intent
        if(intent != null){
            val name = intent.getStringExtra("name")
            val sex = intent.getStringExtra("sex")
            val age = intent.getStringExtra("age")
            val height = intent.getStringExtra("height")
            val number = intent.getStringExtra("number")
            val looks = intent.getStringExtra("looks")
            val time = intent.getStringExtra("time")
            val loc = intent.getStringExtra("loc")
            val extra1 = intent.getStringExtra("extra")
            binding.photo.setImageBitmap(loadCacheImg())
            binding.nameText.text = "이름 : ${name} (${age}세, ${sex}, 신장${height}cm)"
            binding.phoneText.text = "연락처 : ${number}"
            binding.looksText.text = "인상착의 : ${looks}"
            binding.timeText.text = "실종일자 : ${time}"
            binding.locText.text = "마지막 위치 : ${loc}"
            binding.extra1Text.text = "신체 특징 : ${extra1}"
        }else{
            Log.e("setView", "실패")
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
}