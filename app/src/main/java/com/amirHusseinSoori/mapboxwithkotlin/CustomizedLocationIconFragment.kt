package com.amirHusseinSoori.mapboxwithkotlin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.*
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.customized_location_icon_fragment.*


class CustomizedLocationIconFragment : Fragment(R.layout.customized_location_icon_fragment),
    OnMapReadyCallback,
    OnLocationClickListener, PermissionsListener, OnCameraTrackingChangedListener {
    private var permissionsManager: PermissionsManager? = null
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var locationComponent: LocationComponent? = null
    private var isInTrackingMode = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token));
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(
            R.layout.customized_location_icon_fragment,
            container,
            false
        )
        mapView = rootView.findViewById(R.id.mapViewCustomLocation)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        return rootView

    }

    private fun enableLocationComponent(loadedMapStyle: Style) {
              // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {

             // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(requireContext())
                .elevation(5f)
                .accuracyAlpha(.6f)
                .accuracyColor(Color.BLUE)
                .foregroundDrawable(R.drawable.ic_cusrrent_location)
                .build()

                // Get an instance of the component
            locationComponent = mapboxMap!!.locationComponent
            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()

                   // Activate with options
            locationComponent!!.activateLocationComponent(locationComponentActivationOptions)

                  // Enable to make component visible
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationComponent!!.isLocationComponentEnabled = true

          // Set the component's camera mode
            locationComponent!!.cameraMode = CameraMode.TRACKING

          // Set the component's render mode
            locationComponent!!.renderMode = RenderMode.COMPASS

           // Add the location icon click listener
            locationComponent!!.addOnLocationClickListener(this)

           // Add the camera tracking listener. Fires if the map camera is manually moved.
            locationComponent!!.addOnCameraTrackingChangedListener(this)
            back_to_camera_tracking_mode.setOnClickListener(View.OnClickListener {
                if (!isInTrackingMode) {
                    isInTrackingMode = true
                    locationComponent!!.cameraMode = CameraMode.TRACKING
                    locationComponent!!.zoomWhileTracking(16.0)

                } else {
//                    Toast.makeText(
//                        requireActivity(),
//                        "hello",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            })
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(requireActivity())
        }
    }





    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(
            Style.LIGHT
        ) { style -> enableLocationComponent(style) }
    }

    override fun onLocationComponentClick() {
        if (locationComponent != null) {
            if (locationComponent!!.lastKnownLocation != null) {


                Toast.makeText(
                    requireContext(),
                    locationComponent!!.lastKnownLocation!!.latitude.toString() + "  " + locationComponent!!.lastKnownLocation!!.longitude,
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        TODO("Not yet implemented")
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mapboxMap!!.getStyle {
                enableLocationComponent(it);


            }

        } else{
            Toast.makeText(requireContext(), "not enable", Toast.LENGTH_LONG).show();
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onCameraTrackingDismissed() {
        isInTrackingMode = false;
    }

    override fun onCameraTrackingChanged(currentMode: Int) {

    }


     override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

  override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState);
    }

     override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

}