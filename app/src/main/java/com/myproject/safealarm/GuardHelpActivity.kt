package com.myproject.safealarm

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.myproject.safealarm.databinding.ActivityGuardHelpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception

class GuardHelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuardHelpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardHelpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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
                        sendInfo(binding.lookEdit.text.toString(), binding.extraEdit.text.toString(), "eirth")
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
                Log.d("피보호자 정보", "성공")
            }

            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.d("피보호자 정보", "실패")
            }

        })

        Singleton.server.postPhoto(body).enqueue(object : Callback<ResponseDC> {
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Log.d("이미지 정보", "성공")
            }

            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.d("이미지 정보", "실패")
            }

        })
    }
}