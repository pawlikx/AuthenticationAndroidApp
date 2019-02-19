package com.example.pawli.od

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
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
import kotlinx.android.synthetic.main.activity_register.*
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {
    private var mAuthTask: UserRegisterTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        email_create_account_button.setOnClickListener {
            // Store values at the time of the login attempt.
            val loginStr = login_reg.text.toString()
            val passwordStr = password_reg.text.toString()
            val password2Str = password2_reg.text.toString()
            // Reset errors.
            login_reg.error = null
            password_reg.error = null
            password2_reg.error = null

            var cancel = false
            var focusView: View? = null

            // Check for a valid password, if the user entered one.
            if (TextUtils.isEmpty(passwordStr)) {
                password_reg.error = getString(R.string.error_invalid_password)
                focusView = password_reg
                cancel = true
            }
            // Check for a valid password, if the user entered one.
            else if (TextUtils.isEmpty(password2Str)) {
                password_reg.error = getString(R.string.error_invalid_password)
                focusView = password_reg
                cancel = true
            }
            else if (password2Str != passwordStr) {
                password_reg.error = getString(R.string.error_invalid_password2)
                focusView = password_reg
                cancel = true
            }
            // Check for a valid email address.
            else if (TextUtils.isEmpty(loginStr)) {
                login_reg.error = getString(R.string.error_field_required)
                focusView = login_reg
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
                mAuthTask = UserRegisterTask(loginStr, passwordStr)
                mAuthTask!!.execute(Common.getAddressAPI("users"))
            }

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

            register_form.visibility = if (show) View.GONE else View.VISIBLE
            register_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            register_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            register_progress.visibility = if (show) View.VISIBLE else View.GONE
            register_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            register_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            register_progress.visibility = if (show) View.VISIBLE else View.GONE
            register_form.visibility = if (show) View.GONE else View.VISIBLE
        }
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
    inner class UserRegisterTask internal constructor(private val mLogin: String, private val mPassword: String) : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg params: String?): Boolean? {
            if(Global.usersList.isEmpty())
                GetUsersTask().execute(Common.getAddressAPI("users"))

            for(user in Global.usersList){
                if(user.login == mLogin)
                    return false
            }

            var urlString = params[0]

            val hh = HTTPDataHandler()

            val json = "{\"login\": \"$mLogin\", \"password\": \"${mPassword.sha256()}\", \"favourites\": [], \"admin\": false}"

            hh.PostHTTPData(urlString, json)

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            showProgress(false)

            if (success!!) {
                finish()
                Toast.makeText(this@RegisterActivity, "Zarejestrowano!", Toast.LENGTH_SHORT).show()
            } else {
                login_reg.error = getString(R.string.login_in_use)
                login_reg.requestFocus()
            }
        }

        override fun onCancelled() {
            showProgress(false)
        }
    }


    inner class GetUsersTask internal constructor() : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg params: String?): Boolean? {
            var stream: String? = null
            var urlString = params[0]

            var http = HTTPDataHandler()
            stream = http.GetHTTPData(urlString)

            var gson = Gson()

            Global.usersList = gson.fromJson(stream, object : TypeToken<ArrayList<User>>() {}.type)


            return true
        }

        override fun onPostExecute(success: Boolean?) {

        }

        override fun onCancelled() {
            //showProgress(false)
        }
    }

}
