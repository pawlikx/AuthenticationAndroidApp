package com.example.pawli.od

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.pawli.od.MongoDB.Classes.User
import com.example.pawli.od.MongoDB.Engine.Common
import com.example.pawli.od.MongoDB.Engine.HTTPDataHandler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private var mAuthTask: UserLoginTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        getPrefs()

        if(Global.isLogged){
            //Toast.makeText(this@LoginActivity, "isLogged == TRUE!", Toast.LENGTH_LONG).show()
            finish()
            goToLogin2()
        }



        email_sign_in_button.setOnClickListener {
            // Store values at the time of the login attempt.
            val loginStr = login.text.toString()
            val passwordStr = password.text.toString()
            // Reset errors.
            login.error = null
            password.error = null

            var cancel = false
            var focusView: View? = null

            // Check for a valid password, if the user entered one.
            if (TextUtils.isEmpty(passwordStr)) {
                password.error = getString(R.string.error_invalid_password)
                focusView = password
                cancel = true
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(loginStr)) {
                login.error = getString(R.string.error_field_required)
                focusView = login
                cancel = true
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView?.requestFocus()
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true)
                mAuthTask = UserLoginTask(loginStr, passwordStr)
                mAuthTask!!.execute(Common.getAddressAPI("users"))
            }

        }

        go_to_register_button.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            // To pass any data to next activity
            //intent.putExtra("Oid", item.id.oid)
            // start your next activity
            this?.startActivity(intent)
        }

    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    fun goToMain(){
        val intent = Intent(this, MainActivity::class.java)
        // To pass any data to next activity
        //intent.putExtra("Oid", item.id.oid)
        // start your next activity
        this?.startActivity(intent)
    }
    fun goToLogin2(){
        val intent = Intent(this, Login2Activity::class.java)
        // To pass any data to next activity
        //intent.putExtra("Oid", item.id.oid)
        // start your next activity
        this?.startActivity(intent)
    }
    /**
     * Functions that use SharedPrefs.
     */
    fun getPrefs(){
        val sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)

        Global.isLogged = sharedPreferences.getBoolean("isLogged", false)
        Global.userName = sharedPreferences.getString("username", "")
        Global.firstLogin = sharedPreferences.getBoolean("firstLogin", true)

        var gson = Gson()
        val result = sharedPreferences.getString("fullUser", gson.toJson(User()))
        Global.user = gson.fromJson(result, object : TypeToken<User>() {}.type)
    }

    fun savePrefs(name: String, fullUser: User){
        val sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("username", name)
        editor.putBoolean("isLogged", true)


        var gson = Gson()
        val json = gson.toJson(fullUser)
        editor.putString("fullUser", json)

        editor.apply()
    }

    fun String.sha256(): String {
        //return this.hashWithAlgorithm("SHA-512")
        return this.hashWithAlgorithm("SHA-256")
    }

    private fun String.hashWithAlgorithm(algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val bytes = digest.digest(this.toByteArray(Charsets.UTF_8))
        return bytes.fold("", { str, it -> str + "%02x".format(it) })
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mLogin: String, private val mPassword: String) : AsyncTask<String, Void, Boolean>() {

        var logged = false
        override fun doInBackground(vararg params: String?): Boolean? {
            var stream: String? = null
            var urlString = params[0]

            var http = HTTPDataHandler()
            stream = http.GetHTTPData(urlString)

            var gson = Gson()

            Global.usersList = gson.fromJson(stream, object : TypeToken<ArrayList<User>>() {}.type)


            for(user in Global.usersList){
                if(user.login == mLogin){
                    logged = user.password == mPassword.sha256()
                }
                if(logged){
                    Global.userName = user.login
                    Global.user = user
                    savePrefs(mLogin, user)
                    break
                }
            }
            return logged
        }

        override fun onPostExecute(success: Boolean?) {
            showProgress(false)

            if (success!!) {
                Global.isLogged = true

                finish()

                goToMain()
                Toast.makeText(this@LoginActivity, "Zalogowano!", Toast.LENGTH_SHORT).show()
            } else {
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
            }
        }

        override fun onCancelled() {
            showProgress(false)
        }
    }
}
