package com.myproject.safealarm

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import java.io.File

class GuardHelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuardHelpBinding
    private lateinit var locationText: String
    private var context = this
    private lateinit var loadingDlog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardHelpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadingDlog = LoadingDialog(this)
        locationText = locationToText(context)
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
        val dialogListener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int){
                when(which){
                    DialogInterface.BUTTON_NEGATIVE -> {
                        loadingDlog.show()
                        postInfo(binding.lookEdit.text.toString(), binding.extraEdit.text.toString(), locationText)
                    }
                }
            }
        }
        builder.setNegativeButton("확인", dialogListener)
        builder.setPositiveButton("취소", null)
        builder.show()
    }

    private fun postInfo(looks: String, extra2: String, loc: String){
        val photoBody = getPhotoBody()
        val json = getInfoJson(looks, extra2, loc)

        Singleton.server.postMissingInfo(App.prefs.id, json).enqueue(object : Callback<ResponseDC> {
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Singleton.server.postPhoto(photoBody).enqueue(object : Callback<ResponseDC> {
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

    private fun getPhotoBody(): MultipartBody.Part{
        var file = File("${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/${App.prefs.id}.png")
        var requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        var body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        return body
    }

    private fun getInfoJson(looks: String, extra2: String, loc: String): JSONObject{
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
        return json
    }

    override fun onDestroy() {
        loadingDlog.dismiss()
        super.onDestroy()
    }
}