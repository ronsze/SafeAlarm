package com.myproject.safealarm.feature.missing.info

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.internal.LinkedTreeMap
import com.myproject.safealarm.feature.guard.each_info.EachInfoActivity
import com.myproject.safealarm.R
import com.myproject.safealarm.ResponseInfo
import com.myproject.safealarm.Singleton
import com.myproject.safealarm.util.arrayListToBitmap
import com.myproject.safealarm.databinding.ActivityMissingInfoBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MissingInfoActivity : AppCompatActivity() {
    private val context = this

    private lateinit var binding: ActivityMissingInfoBinding
    private lateinit var adapter: BaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissingInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadInfo()

        binding.refresh.setOnClickListener {
            loadInfo()
        }

        binding.list.setOnItemClickListener { parent, view, position, id ->
            try{
                var moveIntent = Intent(context, EachInfoActivity::class.java)

                var data: Map<*, *> = parent.getItemAtPosition(position) as Map<*, *>
                var str = (data["photo"] as LinkedTreeMap<*, *>)["data"] as ArrayList<Double>
                saveCacheImg(arrayListToBitmap(str))
                var name = data["name"] as String ; var sex = data["sex"] as String
                var age = data["age"] as String ; var height = data["height"] as String
                var number = data["number"] as String ; var extra = data["extra"] as String
                var extra2 = data["extra2"] as String ; var time = data["time"] as String
                var loc = data["loc"] as String ; var looks = data["looks"] as String
                moveIntent.putExtra("name", name)
                moveIntent.putExtra("sex", sex)
                moveIntent.putExtra("age", age)
                moveIntent.putExtra("height", height)
                moveIntent.putExtra("number", number)
                moveIntent.putExtra("extra", extra)
                moveIntent.putExtra("extra2", extra2)
                moveIntent.putExtra("time", time)
                moveIntent.putExtra("loc", loc)
                moveIntent.putExtra("looks", looks)

                startActivity(moveIntent)
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    private fun loadInfo() {
        val loadingDlog = LoadingDialog(this)
        loadingDlog.show()
        Singleton.server.getInfo().enqueue(object : Callback<ResponseInfo> {
            override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                var info = response.body()!!.result as List<Map<*, *>>
                adapter = InfoListAdapter(context, info)
                binding.list.adapter = adapter
                loadingDlog.dismiss()
            }

            override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                Log.e("실종자 정보", "실패")
                loadingDlog.dismiss()
            }

        })
    }

    private fun saveCacheImg(bitmap: Bitmap){
        var tempFile = File(cacheDir, "cache.png")
        try{
            tempFile.createNewFile()
            var out = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
        }catch(e: Exception){
            e.printStackTrace()
        }
    }

    inner class InfoListAdapter(val context: Context, val infoArr: List<Map<*, *>>) :
        BaseAdapter() {
        override fun getCount(): Int {
            return infoArr.size
        }

        override fun getItem(position: Int): Any {
            return infoArr[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View = LayoutInflater.from(context).inflate(R.layout.ms_info_list, null)

            val photoView = view.findViewById<ImageView>(R.id.ms_photo)
            val nameView = view.findViewById<TextView>(R.id.ms_name)
            val sexView = view.findViewById<TextView>(R.id.ms_sex)
            val ageView = view.findViewById<TextView>(R.id.ms_age)
            val heightView = view.findViewById<TextView>(R.id.ms_height)
            val timeView = view.findViewById<TextView>(R.id.ms_time)
            val locView = view.findViewById<TextView>(R.id.ms_loc)

            val info = infoArr[position]
            var str = (info["photo"] as LinkedTreeMap<*, *>)["data"] as ArrayList<Double>
            photoView.setImageBitmap(arrayListToBitmap(str))
            nameView.text = ("이름 : ${info["name"] as String}")
            sexView.text = ("성별 : ${info["sex"] as String}")
            ageView.text = ("나이 : ${info["age"] as String}세")
            heightView.text = ("키 : ${info["height"] as String}cm")
            timeView.text = ("실종 시간\n${info["time"] as String}")
            locView.text = ("${info["loc"] as String}")

            return view
        }
    }
}