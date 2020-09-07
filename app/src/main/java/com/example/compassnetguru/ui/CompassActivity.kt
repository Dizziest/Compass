package com.example.compassnetguru.ui

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.example.compassnetguru.R
import com.example.compassnetguru.viewmodel.CompassViewModel
import kotlinx.android.synthetic.main.activity_main.*

class CompassActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private lateinit var viewModel: CompassViewModel

    private var currentAzim = 0f
    private var currentBearing = 0f
    private var bearing = 0f
    private var navigation = false

    private val FINE_LOCATION_CODE = 100
    private val COARSE_LOCATION_CODE = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(CompassViewModel::class.java)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_CODE)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), COARSE_LOCATION_CODE)
            return
        }

        subscribeObservers()

        navigateButton.setOnClickListener(View.OnClickListener {
            var latitude = textLatitude.text.toString().toFloat()
            var longitude = textLongitude.text.toString().toFloat()
            viewModel.onDestinationChanged(latitude, longitude)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2.0f, this)
            stopNavButton.visibility = View.VISIBLE
            navigation = true
            progressBar.visibility = View.VISIBLE
        })

        stopNavButton.setOnClickListener(View.OnClickListener {
            locationManager.removeUpdates(this)
            navigation = false
            arrowImage.clearAnimation()
            arrowImage.visibility = View.GONE
            stopNavButton.visibility = View.GONE
            progressBar.visibility = View.GONE
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    private fun subscribeObservers(){
        observeCompass()
        observeArrow()
    }

    private fun observeCompass(){
        viewModel.azimuthLiveData.observe(this){
            animateCompass(currentAzim, it)
            if (navigation){
                animateArrow(it, bearing, currentBearing)
            }
            currentAzim = it
            currentBearing = -currentAzim+bearing
        }
    }

    private fun observeArrow(){
        viewModel.bearingLiveData.observe(this){
            animateArrow(currentAzim, it, currentBearing)
            currentBearing = -currentAzim+it
            bearing = it
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)

    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        viewModel.onSensorChanged(event, compassImage, arrowImage)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onLocationChanged(location: Location?) {
        viewModel.onLocationChanged(location)
        arrowImage.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }

    private fun animateCompass(currentAzim: Float, azim: Float){
        var animation = RotateAnimation(-currentAzim, -azim, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

        animation.setDuration(500)
        animation.setRepeatCount(0)
        animation.setFillAfter(true)
        compassImage.startAnimation(animation)
    }

    private fun animateArrow(currentAzim: Float, bearing: Float, currentBearing: Float){
        var animation = RotateAnimation(currentBearing, -currentAzim+bearing, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

        animation.setDuration(500)
        animation.setRepeatCount(0)
        animation.setFillAfter(true)
        arrowImage.startAnimation(animation)
    }

}
