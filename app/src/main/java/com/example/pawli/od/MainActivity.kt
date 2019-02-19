package com.example.pawli.od

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.pawli.od.MongoDB.Classes.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
/* FINGERPRINT */
import android.app.KeyguardManager
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.firstlogin_dialog.view.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity() {

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

    //private var locationPUT: Location? = null
    private var locationPUT: Location = Location(LocationManager.GPS_PROVIDER)


    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        locationPUT.latitude = 52.401950
        locationPUT.longitude = 16.951291

        //locationPUT!!.setLatitude(0.0)

        disableView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissions)) {
                enableView()
            }else{
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        }
        getPrefs()

        sign_out_button.setOnClickListener{
            logOut()
            finish()
            goToLogin2()
        }

        /*buttonFile.setOnClickListener {

        }*/

        if(Global.firstLogin)
            showFirstLoginDialog()
    }

    //region location
    /* LOCATION */
    private fun disableView() {
        buttonFile.isEnabled = false
        buttonFile.alpha = 0.5F
    }

    private fun enableView() {
        buttonFile.isEnabled = true
        buttonFile.alpha = 1F
        buttonFile.setOnClickListener { getLocation()}


        //Toast.makeText(this, "Odległość: $distanceInMeters", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {

            if (hasGps) {
                Log.d("CodeAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object :
                    LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null) {
                            locationGps = location

                            /*tv_result.append("\nGPS ")
                            tv_result.append("\nLatitude : " + locationGps!!.latitude)
                            tv_result.append("\nLongitude : " + locationGps!!.longitude)*/
                            //tv_result.append("\nDystans do PUT : $distanceInKilometers")
                            Log.d("CodeAndroidLocation", " GPS Latitude : " + locationGps!!.latitude)
                            Log.d("CodeAndroidLocation", " GPS Longitude : " + locationGps!!.longitude)


                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String?) {

                    }

                    override fun onProviderDisabled(provider: String?) {

                    }

                })

                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation

                /*val distanceInMeters = locationPUT.distanceTo(locationGps)
                val distanceInKilometers = distanceInMeters.div(1000)
                tv_result.append("\nDystans do PUT : $distanceInKilometers")
                if(distanceInKilometers <= 10.0)
                    Toast.makeText(this@MainActivity, "Uzyskano dostęp", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@MainActivity, "Nie znajdujesz się w pobliżu Politechniki Poznańskiej!", Toast.LENGTH_SHORT).show()*/
            }
            if (hasNetwork) {
                Log.d("CodeAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location?) {
                        if (location != null) {
                            locationNetwork = location
                            /*tv_result.append("\nNetwork ")
                            tv_result.append("\nLatitude : " + locationNetwork!!.latitude)
                            tv_result.append("\nLongitude : " + locationNetwork!!.longitude)*/
                            Log.d("CodeAndroidLocation", " Network Latitude : " + locationNetwork!!.latitude)
                            Log.d("CodeAndroidLocation", " Network Longitude : " + locationNetwork!!.longitude)
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String?) {

                    }

                    override fun onProviderDisabled(provider: String?) {

                    }

                })

                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null)
                    locationNetwork = localNetworkLocation
            }

            if(locationGps!= null && locationNetwork!= null){
                if(locationGps!!.accuracy > locationNetwork!!.accuracy){
                    /*tv_result.append("\nNetwork ")
                    tv_result.append("\nLatitude : " + locationNetwork!!.latitude)
                    tv_result.append("\nLongitude : " + locationNetwork!!.longitude)*/
                    Log.d("CodeAndroidLocation", " Network Latitude : " + locationNetwork!!.latitude)
                    Log.d("CodeAndroidLocation", " Network Longitude : " + locationNetwork!!.longitude)
                }else{
                    /*tv_result.append("\nGPS ")
                    tv_result.append("\nLatitude : " + locationGps!!.latitude)
                    tv_result.append("\nLongitude : " + locationGps!!.longitude)*/
                    Log.d("CodeAndroidLocation", " GPS Latitude : " + locationGps!!.latitude)
                    Log.d("CodeAndroidLocation", " GPS Longitude : " + locationGps!!.longitude)
                }
            }
            var distanceInMeters: Float = 1001f
            if(locationNetwork!= null)
                distanceInMeters = locationPUT.distanceTo(locationNetwork)
            if(locationGps!= null && locationNetwork!= null){
                if(locationGps!!.accuracy > locationNetwork!!.accuracy){
                    distanceInMeters = locationPUT.distanceTo(locationGps)
                }
            }
            else if(locationGps!= null && locationNetwork== null)
                distanceInMeters = locationPUT.distanceTo(locationGps)

            val distanceInKilometers = distanceInMeters.div(1000)
            tv_result.append("\nDystans do PUT : $distanceInKilometers")
            if(distanceInKilometers <= 1.0)
                Toast.makeText(this@MainActivity, "Uzyskano dostęp", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this@MainActivity, "Nie znajdujesz się w pobliżu Politechniki Poznańskiej!", Toast.LENGTH_SHORT).show()
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (allSuccess)
                enableView()

        }
    }
    /* END LOCATION SECTOR */

    //endregion

    //region other
    fun showFirstLoginDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(R.layout.firstlogin_dialog, null)
        builder.setCancelable(false)
        val mPinView = view.pinView_add
        val mAccept = view.accept_btn

        builder.setView(view)
        var dialog: Dialog = builder.create()

        mAccept.setOnClickListener{
            if(!mPinView.value.toString().isEmpty()) {
                if(mPinView.value.toString().length == 4){
                    savePrefs(mPinView.value.toString())
                    Global.userPIN = mPinView.value.toString()
                    dialog.dismiss()
                }
                else
                    Toast.makeText(this, "Pin musi składać się z 4 cyfr", Toast.LENGTH_SHORT).show()

                //findRecipe(mName.text.toString())


            }
            else
                Toast.makeText(this,"Podaj PIN!", Toast.LENGTH_SHORT).show()
        }


        dialog.show()

    }

    fun savePrefs(pin: String){
        val sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        //editor.putString("username", name)
        //editor.putBoolean("isLogged", true)

        editor.putBoolean("firstLogin", false)
        editor.putString("userPIN", pin)


        //var gson = Gson()
        //val json = gson.toJson(fullUser)
        //editor.putString("fullUser", json)

        editor.apply()
    }

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

    fun logOut(){

        /*val sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("username", "")
        editor.putBoolean("isLogged", false)

        var gson = Gson()
        val json = gson.toJson(User())
        editor.putString("fullUser", json)
        editor.apply()

        Global.userName = ""
        Global.isLogged = false
        Global.user = User()*/

        Toast.makeText(this, "Wylogowano!", Toast.LENGTH_SHORT).show()
    }

    fun goToLogin2(){
        val intent = Intent(this, Login2Activity::class.java)
        // To pass any data to next activity
        //intent.putExtra("Oid", item.id.oid)
        // start your next activity
        this?.startActivity(intent)
    }

    //endregion
}
