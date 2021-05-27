package com.myproject.safealarm

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.myproject.safealarm.databinding.ActivityGuardHelpBinding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URLEncoder
import java.util.*

class GuardHelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuardHelpBinding
    private lateinit var locationText: String
    private var context = this
    private lateinit var proDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardHelpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        proDialog = ProgressDialog(this)
        locationText = cngLocation(App.prefs.s_lat.toDouble(), App.prefs.s_lng.toDouble())
        binding.locText.setText(locationText)

        binding.okBtn.setOnClickListener {
            showOkDialog()
        }

        binding.cancleBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun showOkDialog(){
        val builder = AlertDialog.Builder(this)

        builder.setTitle("실종자 정보 등록")
        builder.setMessage("기존에 저장해 둔 실종자 정보와\n" +
                "위에 명시된 정보가 앱에 게시됩니다.\n" +
                "다른 사용자가 이를 확인할 수 있습니다.\n" +
                "동의하시면 확인을 눌러주세요.")
        var dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int){
                when(which){
                    DialogInterface.BUTTON_NEGATIVE -> {
                        proDialog.myDig()
                        sendInfo(binding.lookEdit.text.toString(), binding.extraEdit.text.toString(), locationText)
                    }
                }
            }
        }
        builder.setNegativeButton("확인", dialog_listener)
        builder.setPositiveButton("취소", null)
        builder.show()
    }

    private fun sendInfo(looks: String, extra2: String, loc: String){
        var file = File("${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/${App.prefs.id}.png")
        var requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        var body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        var json = JSONObject()
        try{
            json.put("name", App.prefs.name)
            json.put("age", App.prefs.age)
            json.put("sex", App.prefs.sex)
            json.put("height", App.prefs.height)
            json.put("number", App.prefs.number)
            json.put("extra", App.prefs.extra)
            json.put("looks", looks)
            json.put("extra2", extra2)
            json.put("loc", loc)
        }catch (e: JSONException){
            e.printStackTrace()
        }
        Singleton.server.postMissingInfo(App.prefs.id, json).enqueue(object : Callback<ResponseDC> {
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Singleton.server.postPhoto(body).enqueue(object : Callback<ResponseDC> {
                    override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                        Toast.makeText(context, "등록되었습니다.", Toast.LENGTH_SHORT).show()
                        App.mSocket.emit("postMissing")
                        App.prefs.infoRegist = true
                        finish()
                    }

                    override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                        Log.d("이미지 정보", "실패")
                        Toast.makeText(context, "실패했습니다..", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                })
            }

            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.d("피보호자 정보", "실패")
                Toast.makeText(context, "실패했습니다..", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun cngLocation(latitude: Double, longitude: Double): String{           //위도, 경도를 주소로 변경
        var lat_s = latitude.toString() ; var lng_s = longitude.toString()
        var lat = lat_s.toDouble()
        var lng = lng_s.toDouble()
        if(lat_s.length >= 7){
            lat_s.substring(0, 6).toDouble()
        }
        if(lng_s.length >= 9){
            lng = lng_s.substring(0, 8).toDouble()
        }
        val mGeocoder = Geocoder(this, Locale.KOREAN)
        var mResultList: List<Address>? = null
        var currentLocation = ""
        try{
            mResultList = mGeocoder.getFromLocation(
                lat, lng, 1
            )
        }catch(e: IOException){
            e.printStackTrace()
        }
        if(mResultList != null){
            currentLocation = mResultList[0].getAddressLine(0)
            currentLocation = currentLocation.substring(5)
        }
        return currentLocation
    }

    override fun onDestroy() {
        proDialog.dismiss()
        super.onDestroy()
    }
}