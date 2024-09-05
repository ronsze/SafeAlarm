package com.myproject.safealarm.feature.loading

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import com.myproject.safealarm.R

class LoadingDialog(val context: Context) {
    private val dialog = Dialog(context)

    fun show() {
        dialog.setContentView(R.layout.progress_dialog)

        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.show()
    }

    fun dismiss(){
        dialog.dismiss()
    }
}