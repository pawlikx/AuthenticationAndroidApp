package com.example.pawli.od

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.*
import com.example.pawli.od.MongoDB.Classes.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.android.synthetic.main.finger_dialog.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class Login2Activity : AppCompatActivity() {
    lateinit var builder : AlertDialog.Builder


    lateinit var dialogG: AlertDialog
    /* FINGERPRINT */
    private val KEY_NAME:String="mykey"
    private val TAG:String="FigurePrintAuth"

    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private val textView: TextView? = null
    private var cryptoObject: FingerprintManager.CryptoObject? = null
    private var fingerprintManager: FingerprintManager? = null
    private var success = 0


    var message: TextView?=null


    /* FINGERPRINT */

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        getPrefs()


        sign_in_button.setOnClickListener {
            if(pinView.value.toString().equals(Global.userPIN)){
                finish()
                goToMain()
            }
            else
                Toast.makeText(this,"Niepoprawny PIN!", Toast.LENGTH_SHORT).show()

        }
        deleteDataBtn_id.setOnClickListener {
            logOut()
            finish()
            goToLogin()
        }

        /* FINGERPRINT */
        message = findViewById<TextView>(R.id.msg)
        val btn = findViewById<ImageView>(R.id.fingerprint_btn)
        val fph = FingerprintHandler(message!!, this, this)
        if (!checkFinger()) {
            Log.d(TAG,"checkFinger return"+!checkFinger())
            btn.isEnabled = false
        } else {
            // We are ready to set up the cipher and the key
            Log.d(TAG,"checkFinger return else"+!checkFinger())
            generateKey()
            val cipher = generateCipher()
            cryptoObject = FingerprintManager.CryptoObject(cipher)
            message?.text=getString(R.string.msg)
            /*Toast.makeText(this, message?.text, Toast.LENGTH_SHORT).show()
            if(success == 1){
                finish()
                goToMain()
            }*/


        }

        fingerprint_btn.setOnClickListener {
            showFingerPrintDialog()
            message!!.text="Touch the fingerprint scanner to authorize"
            fph.doAuth(this.fingerprintManager!!, this!!.cryptoObject!!)
        }
        /* FINGERPRINT */


    }

/*
fun showSearchDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val view: View = layoutInflater.inflate(R.layout.dialog_search, null)
        builder.setCancelable(true)
        val mName = view.et_Dialog_Name
        val mSearch = view.btn_Dialog_Search

        builder.setView(view)
        var dialog: Dialog = builder.create()

        mSearch.setOnClickListener{
            if(!mName.text.toString().isEmpty()) {
                Toast.makeText(activity, "Szukam", Toast.LENGTH_SHORT).show()
                /* TUTAJ ALGORYTM SZUKANIA */
                findRecipe(mName.text.toString())
                dialog.dismiss()

            }
            else
                Toast.makeText(activity,"Wpisz nazwę drinka", Toast.LENGTH_SHORT).show()
        }


        dialog.show()

    }
 */

    fun getPrefs(){
        val sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)

        Global.isLogged = sharedPreferences.getBoolean("isLogged", false)
        Global.userName = sharedPreferences.getString("username", "")
        Global.firstLogin = sharedPreferences.getBoolean("firstLogin", true)
        Global.userPIN = sharedPreferences.getString("userPIN", "0000")

        var gson = Gson()
        val result = sharedPreferences.getString("fullUser", gson.toJson(User()))
        Global.user = gson.fromJson(result, object : TypeToken<User>() {}.type)
    }

    fun showFingerPrintDialog(){
        //val builder = AlertDialog.Builder(this)
        builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.finger_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        builder.setNegativeButton("Zamknij", { dialogInterface: DialogInterface, i: Int -> })

        //builder.show()

        //val dialog = builder.create()


        dialogG = builder.create()


        var feedback = getString(R.string.msg)
        if(success == 1)
            dialogG.dismiss()
        dialogG.show()

        //changeDialogText("blablbla", Color.RED)
    }

    fun changeDialogText(text: String, color: Int){
        //val dialogText: TextView = (dialogG.window.findViewById(R.id.finger_message))as TextView
        val dialogText: TextView = dialogG.window.findViewById<TextView>(R.id.finger_message)


        //finger_message.setText("blabla ELO") does not work
        dialogText.text = text
        dialogText.setTextColor(color)
    }

    /* FINGERPRINT */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkFinger(): Boolean {
        // Keyguard Manager
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        // Fingerprint Manager
        fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
        try {
            // Check if the fingerprint sensor is present
            if (!fingerprintManager!!.isHardwareDetected) {
                // Update the UI with a message
                message?.text = getString(R.string.fingerprint_not_supported)
                return false
            }
            if (!fingerprintManager!!.hasEnrolledFingerprints()) {
                message?.text = getString(R.string.no_fingerprint_configured)
                return false
            }
            if (!keyguardManager.isKeyguardSecure) {
                message?.text = getString(R.string.secure_lock_not_enabled)
                return false
            }
        } catch (se: SecurityException) {
            se.printStackTrace()
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateKey() {

        // Get the reference to the key store
        keyStore = KeyStore.getInstance("AndroidKeyStore")
        // Key generator to generate the key
        keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore")
        keyStore?.load(null)
        keyGenerator?.init(
            KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build())
        keyGenerator?.generateKey()


    }


    private fun generateCipher(): Cipher {

        val cipher = Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        val key = keyStore?.getKey(KEY_NAME,
            null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    /* ***************** */




    fun logOut(){
        val sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("username", "")
        editor.putBoolean("isLogged", false)
        editor.putBoolean("firstLogin", true)

        var gson = Gson()
        val json = gson.toJson(User())
        editor.putString("fullUser", json)
        editor.apply()

        Global.userName = ""
        Global.isLogged = false
        Global.firstLogin = true

        Global.user = User()

        Toast.makeText(this, "Usunięto dane!", Toast.LENGTH_SHORT).show()
    }

    fun goToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        // To pass any data to next activity
        //intent.putExtra("Oid", item.id.oid)
        // start your next activity
        this?.startActivity(intent)
    }
    fun goToMain(){
        val intent = Intent(this, MainActivity::class.java)
        // To pass any data to next activity
        //intent.putExtra("Oid", item.id.oid)
        // start your next activity
        this?.startActivity(intent)
    }
}
