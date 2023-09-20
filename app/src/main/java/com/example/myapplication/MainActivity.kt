package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.example.myapplication.Constants
import com.example.myapplication.models.WheaterResponse
import com.example.myapplication.network.WheaterService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!isLocationEnabled()){
            Toast.makeText(
                this,
                "GPS Belum diaktifkan, Aktifkan terlebih dahulu!",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        else{
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener{

                    @RequiresApi(Build.VERSION_CODES.S)
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()){
                            requestLocationData()
                        }
                        if (report.isAnyPermissionPermanentlyDenied){
                            Toast.makeText(
                                this@MainActivity,
                                "Kamu denied untuk akses lokasi, Tolong izinkan untuk akses lokasi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()
        }
    }

    private fun getLocationWheaterDetails(latitude: Double, longitude: Double){
        if(Constants.isNetworkAvailable(this@MainActivity)){
            val retrofit: Retrofit =  Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service : WheaterService = retrofit
                .create<WheaterService>(WheaterService::class.java)

            val listCall: Call<WheaterResponse> = service.getWheater(
                latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
            )

            listCall.enqueue(object : Callback<WheaterResponse>{
                override fun onResponse(
                    call: Call<WheaterResponse>,
                    response: Response<WheaterResponse>
                ) {
                    if(response.isSuccessful){
                        val wheaterList: WheaterResponse? = response.body()
                        Log.i("Response Result", "$wheaterList")
                    }else{
                        val rc = response.code()
                        when(rc){
                            400 -> {
                                Log.e("Error 400", "Bad Connection")
                            }404 -> {
                                Log.e("Error 404", "Not Found")
                            }else ->{
                                Log.e("Error", "Generic Error")

                            }
                        }
                    }
                }

                override fun onFailure(call: Call<WheaterResponse>, t: Throwable) {
                    Log.e("ERROORR", t.message.toString())
                }

            })

        }else{
            Toast.makeText(
                this@MainActivity,
                "Internet Tidak Terhubung",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private fun requestLocationData(){
        val mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority = LocationRequest.QUALITY_HIGH_ACCURACY

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            val latitude = mLastLocation?.latitude
            Log.i("Current Latitude", "$latitude")
            val longitude = mLastLocation?.longitude
            Log.i("Current Longitude", "$longitude")
            if (latitude != null && longitude != null) {
                getLocationWheaterDetails(latitude, longitude)
            }
        }
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("Kamu tidak mengizinkan permsiion dari HP.")
            .setPositiveButton(
                "Go To Settings"
            ){_,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){dialog,
                _ -> dialog.dismiss()
            }.show()
    }

    private fun isLocationEnabled() : Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}