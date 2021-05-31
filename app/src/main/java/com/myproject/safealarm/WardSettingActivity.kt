package com.myproject.safealarm

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.myproject.safealarm.databinding.ActivityWardSettingBinding

class WardSettingActivity : AppCompatActivity() {
    private val context = this

    private lateinit var binding: ActivityWardSettingBinding

    companion object {
        val mSocket = App.mSocket
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWardSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.certiPass.setOnClickListener {
            if(App.prefs.cert){
                verifyPassDialog()
            }else{
                Toast.makeText(context, "보호자로부터의 인증요청이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.setPass.setOnClickListener {
            showSetPassDialog(this)
        }
    }

    private fun verifyPassDialog() {                       //피보호자 다이어로그
        val builder = AlertDialog.Builder(this)
        val et = EditText(this)
        builder.setMessage("패스워드를 입력하세요.")
        builder.setView(et)
        var dialogListener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    DialogInterface.BUTTON_NEGATIVE -> {
                        progressVerify(et.text.toString())

                    }
                }
            }
        }
        builder.setNegativeButton("확인", dialogListener)
        builder.setPositiveButton("취소", null)
        builder.show()
    }

    private fun progressVerify(inputPass: String){
        if(checkPassword(inputPass)){
            sendVerifyDone()
        }else{
            Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPassword(inputPass: String): Boolean{
        return inputPass == App.prefs.pass
    }

    private fun sendVerifyDone(){
        Toast.makeText(context, "인증되었습니다.", Toast.LENGTH_SHORT).show()
        mSocket.emit("callbackCert")
        App.prefs.cert = false
    }

    private fun showSetPassDialog(context: Context){
        val builder = AlertDialog.Builder(this)
        val et = EditText(this)
        et.inputType = InputType.TYPE_NUMBER_VARIATION_PASSWORD
        builder.setTitle("설정할 패스워드를 입력하세요")
        builder.setMessage(checkExistPass())
        builder.setView(et)
        var dialogListener = object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when(which){
                    DialogInterface.BUTTON_NEGATIVE -> {
                        savePassword(et.text.toString())
                    }
                }
            }
        }
        builder.setNegativeButton("확인", dialogListener)
        builder.setPositiveButton("취소", null)
        builder.show()
    }

    private fun savePassword(password: String){
        if (password != "") {
            App.prefs.pass = password
            Toast.makeText(context, "등록되었습니다.", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "유효한 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun checkExistPass(): String{
        var str = ""
        if(App.prefs.pass != "0000"){
            str = "이미 등록된 패스워드가 있습니다."
        }else{
            str = "등록된 패스워드가 없습니다.\n기본 패스워드는 0000입니다."
        }
        return str
    }
}

