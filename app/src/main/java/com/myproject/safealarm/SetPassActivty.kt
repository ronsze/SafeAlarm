package com.myproject.safealarm

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.myproject.safealarm.databinding.ActivitySetPassActivtyBinding

class SetPassActivty : AppCompatActivity() {
    lateinit var binding: ActivitySetPassActivtyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetPassActivtyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        showDialog()

        if(App.prefs.pass != "0000"){
            binding.passText.setText("이미 등록된 패스워드가 있습니다.")
        }else{
            binding.passText.setText("등록된 패스워드가 없습니다.\n기본 패스워드는 0000입니다.")
        }

        binding.ok.setOnClickListener {
            if (binding.inputPass.text.toString() != "") {
                App.prefs.pass = binding.inputPass.text.toString()
            }
            Toast.makeText(this, "등록되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.cancle.setOnClickListener {
            finish()
        }
    }

    fun showDialog() {                       //피보호자 다이어로그
        val builder = AlertDialog.Builder(this)
        builder.setMessage("이미 패스워드를 등록하셨으면\n덮어씌우는 형태로 저장됩니다.")
        builder.setNegativeButton("확인", null)
        builder.setPositiveButton("취소", null)
        builder.show()
    }
}