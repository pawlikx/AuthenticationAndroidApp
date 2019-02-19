package com.example.pawli.od

import android.content.Context
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.provider.Settings.Secure.getString
import android.support.annotation.RequiresApi
import android.widget.TextView
import android.app.Activity
import android.graphics.Color
import android.support.v7.app.AlertDialog
import android.widget.Toast
import kotlinx.android.synthetic.main.finger_dialog.view.*


@RequiresApi(Build.VERSION_CODES.M)
class FingerprintHandler(private val tv: TextView, private val mContext : Context?, private val mActivity: Login2Activity) : FingerprintManager.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        //tv.text = tv.context.getString(R.string.aut_fail)

        mActivity.changeDialogText("Skaner tymczasowo zablokowany, wprowadź PIN aby się zalogować!", Color.RED)

        Toast.makeText(mContext, "Skaner tymczasowo zablokowany, wprowadź PIN aby się zalogować!", Toast.LENGTH_SHORT).show()

    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
        super.onAuthenticationHelp(helpCode, helpString)
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        //tv.text = tv.context.getString(R.string.auth_success)
        //tv.setTextColor(tv.context.resources.getColor(android.R.color.holo_green_light))
        (mContext as Activity).finish()
        mContext?.startActivity(Intent(mContext, MainActivity::class.java))
        //(activity as MainActivity).showChangePasswordFragment()
        //Login2Activity.showFingerPrintDialog()


    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()

        /*val dialogView = (mContext as Activity).layoutInflater.inflate(R.layout.finger_dialog, null)
        dialogView.finger_message.text = tv.context.getString(R.string.aut_fail)*/

        //val messageView = finger_message
        mActivity.changeDialogText("Spróbuj ponownie!", Color.RED)
        //Toast.makeText(mContext, "Spróbuj ponownie!", Toast.LENGTH_SHORT).show()
        //(mContext as Activity).finish()
    }

    fun doAuth(manager: FingerprintManager,
               obj: FingerprintManager.CryptoObject) {
        val signal = CancellationSignal()
        try {
            manager.authenticate(obj, signal, 0, this, null)
        } catch (sce: SecurityException) {
        }

    }
}