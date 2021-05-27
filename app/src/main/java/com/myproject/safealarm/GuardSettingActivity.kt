package com.myproject.safealarm

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.myproject.safealarm.databinding.ActivityGuardSettingBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GuardSettingActivity : AppCompatActivity() {
    lateinit var binding: ActivityGuardSettingBinding
    private var context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.wardInfo.setOnClickListener {
            startActivity(Intent(this, WardInfoActivity::class.java))
        }

        binding.alarmSet.setOnClickListener {
            startActivity(Intent(this, AlarmSetActivity::class.java))
        }

        binding.delInfo.setOnClickListener {
            Log.e("Aaaa", App.prefs.infoRegist.toString())
            if(App.prefs.infoRegist){
                delDialog()
            }else{
                Toast.makeText(context, "게시된 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun delDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage("게시된 실종정보를\n삭제하시겠습니까?")
        var dialog_listener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    DialogInterface.BUTTON_NEGATIVE -> {
                        delInfo()
                    }
                }
            }
        }
        builder.setNegativeButton("확인", dialog_listener)
        builder.setPositiveButton("취소", null)
        builder.show()
    }

    private fun delInfo(){
        Singleton.server.delInfo(App.prefs.id).enqueue(object: Callback<ResponseInfo>{
            override fun onResponse(call: Call<ResponseInfo>, response: Response<ResponseInfo>) {
                Toast.makeText(context, "삭제했습니다.", Toast.LENGTH_SHORT).show()
                App.prefs.infoRegist = false
            }

            override fun onFailure(call: Call<ResponseInfo>, t: Throwable) {
                Toast.makeText(context, "실패했습니다.", Toast.LENGTH_SHORT).show()
            }

        })
    }
}
