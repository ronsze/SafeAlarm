package com.myproject.safealarm

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.common.util.Base64Utils
import com.google.gson.internal.LinkedTreeMap
import com.myproject.safealarm.databinding.ActivityMissingInfoBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.RuntimeException

class MissingInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMissingInfoBinding
    private lateinit var adapter: BaseAdapter
    private lateinit var globalInfo: List<Map<*, *>>
    private val context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissingInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        getInfo()

        binding.refresh.setOnClickListener {
            getInfo()
        }

        binding.list.setOnItemClickListener { parent, view, position, id ->
            try{
                var moveIntent = Intent(context, EachInfoActivity::class.java)

                var data: Map<*, *> = parent.getItemAtPosition(position) as Map<*, *>
                var str = (data["photo"] as LinkedTreeMap<*, *>)["data"] as ArrayList<Double>
                saveCacheImg(getBitmap(str))
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

    private fun getInfo() {
        val proDialog = ProgressDialog(this)
        proDialog.myDig()
        Singleton.server.getInfo().enqueue(object : Callback<ResponseInfo> {
            override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                var res = response.body()!!.result as List<Map<*, *>>
                globalInfo = res
                adapter = InfoListAdapter(context, res)
                binding.list.adapter = adapter
                proDialog.dismiss()
            }

            override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                Log.e("실종자 정보", "실패")
                proDialog.dismiss()
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

    fun exByte(list: ArrayList<Double>): ByteArray {
        var list2: MutableList<Byte> = mutableListOf()
        for (i in 0..list.size - 1) {
            list2.add(list[i].toInt().toByte())
        }
        var arr = list2.toByteArray()
        return arr
    }

    fun getBitmap(input: ArrayList<Double>): Bitmap {
        var arr = exByte(input)

        try {
            var bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.size)
            return bitmap
        } catch (e: Exception) {
            throw RuntimeException(e)
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

            val photo = view.findViewById<ImageView>(R.id.ms_photo)
            val name = view.findViewById<TextView>(R.id.ms_name)
            val sex = view.findViewById<TextView>(R.id.ms_sex)
            val age = view.findViewById<TextView>(R.id.ms_age)
            val height = view.findViewById<TextView>(R.id.ms_height)
            val time = view.findViewById<TextView>(R.id.ms_time)
            val loc = view.findViewById<TextView>(R.id.ms_loc)

            val info = infoArr[position]
            var str = (info["photo"] as LinkedTreeMap<*, *>)["data"] as ArrayList<Double>
            photo.setImageBitmap(getBitmap(str))
            name.text = ("이름 : ${info["name"] as String}")
            sex.text = ("성별 : ${info["sex"] as String}")
            age.text = "나이 : ${info["age"] as String}세"
            height.text = "키 : ${info["height"] as String}cm"
            if (info["time"] == null) {
                time.text = ("실종시간\nnull")
            } else {
                time.text = ("실종 시간\n${info["time"] as String}")
            }
            loc.text = ("${info["loc"] as String}")

            return view
        }
    }
}