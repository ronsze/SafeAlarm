package com.myproject.safealarm

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.myproject.safealarm.databinding.ActivityGuardHelpBinding

class GuardHelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuardHelpBinding
    private val context = this
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
                "위에 명시된 정보가 앱에 등록됩니다.\n" +
                "다른 사용자들이 이를 확인할 수 있습니다.")
        var dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int){
                when(which){
                    DialogInterface.BUTTON_NEGATIVE -> {
                        sendInfo()
                    }
                }
            }
        }
        builder.setNegativeButton("확인", dialog_listener)
        builder.setPositiveButton("취소", null)
        builder.show()
    }

    private fun sendInfo(){
    }
}