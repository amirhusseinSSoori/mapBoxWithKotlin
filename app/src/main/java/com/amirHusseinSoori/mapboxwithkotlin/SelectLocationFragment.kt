package com.amirHusseinSoori.mapboxwithkotlin
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.core.exceptions.ServicesException
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.BuildConfig
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.mapbox.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class SelectLocationFragment:Fragment(R.layout.select_location),
    com.mapbox.mapboxsdk.maps.OnMapReadyCallback,
    MapboxMap.OnMapClickListener,
    PermissionsListener {

    var locationlan = 35.678494
    var locationlong = 51.390880
    private lateinit var manager: LocationManager
    private var mapView: MapView? = null
    private val DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID"
    private var mapboxMap: MapboxMap? = null
    private var permissionsManager: PermissionsManager? = null
    private var hoveringMarker: ImageView? = null
    private var droppedMarkerLayer: Layer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
        super.onCreate(savedInstanceState)
        manager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager;
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.select_location, container, false)
        mapView = rootView.findViewById(R.id.mapViewChose)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        return rootView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS){style->
            enableCurrentLocation(style)


            //remove logo
            mapboxMap.uiSettings.isAttributionEnabled = false;
            mapboxMap.uiSettings.isLogoEnabled = false;

            //change my marker drawable
            hoveringMarker = ImageView(requireContext())
            hoveringMarker!!.setBackgroundResource(R.drawable.ic_placeholder)


            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER
            );

            hoveringMarker!!.layoutParams = params
            mapView!!.addView(hoveringMarker)

            fab_choseLocation.setOnClickListener {


                //if gps Not Active
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    val dialogMessage = Dialog_message(requireContext())
                    dialogMessage.show()

                } else {
                    enableCurrentLocation(style)
                    //route current location
                    val position = CameraPosition.Builder()
                        .target(LatLng(locationlan, locationlong))
                        .zoom(17.0)
//                    .bearing(180.0)
                        .tilt(30.0)
                        .build()

                    //camera update
                    mapboxMap.animateCamera(
                        CameraUpdateFactory
                            .newCameraPosition(position), 7000
                    )
                }
            }

            initDroppedMarker(style)




            btn_choseLocationF_seeBasig.setOnClickListener {
                if (hoveringMarker!!.visibility == View.VISIBLE) {

                    var mapTargetLatLng = mapboxMap!!.cameraPosition.target;

                    hoveringMarker!!.visibility = View.INVISIBLE

                    // Transform the appearance of the button to become the cancel button
                    btn_choseLocationF_seeBasig.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.mapbox_blue)
                    )
                    btn_choseLocationF_seeBasig.text = "موقعیت  شما "


                    // Show the SymbolLayer icon to represent the selected map location
                    if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                        val source = style.getSourceAs<GeoJsonSource>("dropped-marker-source-id")
                        source?.setGeoJson(
                                Point.fromLngLat(
                                        mapTargetLatLng.longitude,
                                        mapTargetLatLng.latitude
                                )
                        )
                        droppedMarkerLayer =
                                style.getLayer(DROPPED_MARKER_LAYER_ID)
                        if (droppedMarkerLayer != null) {
                            droppedMarkerLayer!!.setProperties(PropertyFactory.visibility(Property.VISIBLE))
                        }
                    }

                    // Use the map camera target's coordinates to make a reverse geocoding search


//                    reverseGeocode(
//                            Point.fromLngLat(
//                                    mapTargetLatLng.longitude,
//                                    mapTargetLatLng.latitude
//                            )
//                    )


                    Toast.makeText(
                            requireContext(),
                            mapTargetLatLng.longitude.toString() + "" + mapTargetLatLng.latitude,
                            Toast.LENGTH_SHORT
                    ).show()

                } else {


                    // Switch the button appearance back to select a location.
                    btn_choseLocationF_seeBasig.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.mapbox_plugins_navy)
                    )
                    btn_choseLocationF_seeBasig.text = "موقعیت  شما "


                    hoveringMarker!!.visibility = View.VISIBLE



                    // Hide the selected location SymbolLayer
                    droppedMarkerLayer =
                            style.getLayer(DROPPED_MARKER_LAYER_ID)
                    if (droppedMarkerLayer != null) {
                        droppedMarkerLayer!!.setProperties(PropertyFactory.visibility(Property.NONE))
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        mapView!!
            .onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onMapClick(point: LatLng): Boolean {
        return false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
//        Toast.makeText(requireContext(), R.string.app_name, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted && mapboxMap != null) {
            val style = mapboxMap!!.style
            style?.let {
                enableCurrentLocation(it)
            }
        } else {
//            Toast.makeText(requireContext(), R.string.app_name, Toast.LENGTH_LONG).show()
        }
    }
    private fun enableCurrentLocation(loadedMapStyle: Style){
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            val locationComponent = mapboxMap!!.locationComponent
            //active current location
            val locationActive=LocationComponentActivationOptions.builder(requireContext(),loadedMapStyle).build()
            locationComponent.activateLocationComponent(locationActive)

            //active Permission location
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            locationComponent.isLocationComponentEnabled = true

            //active camera location
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.NORMAL
            if (BuildConfig.DEBUG && locationComponent.lastKnownLocation == null) {
                error("Assertion failed")
            }


            //if gps Not Active
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val dialogMessage = Dialog_message(requireContext())
                dialogMessage.show()
            } else {
                locationlan = locationComponent.lastKnownLocation?.latitude ?: 35.678494
                locationlong = locationComponent.lastKnownLocation?.longitude ?: 51.390880
            }

        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(requireActivity())
        }
    }
    private fun initDroppedMarker(loadedMapStyle: Style) {
//        ContextCompat.getDrawable(getActivity(), R.drawable.name);
        loadedMapStyle.addImage("dropped-icon-image", ContextCompat.getDrawable(requireActivity(), R.drawable.ic_pin)!!)
        loadedMapStyle.addSource(GeoJsonSource("dropped-marker-source-id"))
        loadedMapStyle.addLayer(
            SymbolLayer(
                DROPPED_MARKER_LAYER_ID,
                "dropped-marker-source-id"
            ).withProperties(
                PropertyFactory.iconImage("dropped-icon-image"),
                PropertyFactory.visibility(Property.NONE),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true)
            )
        )
    }
    private fun reverseGeocode(point: Point) {
        try {
            val client = MapboxGeocoding.builder()
                .accessToken(getString(R.string.mapbox_access_token))
                .query(Point.fromLngLat(point.longitude(), point.latitude()))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .build()
            client.enqueueCall(object : Callback<GeocodingResponse?> {
                override fun onResponse(
                    call: Call<GeocodingResponse?>,
                    response: Response<GeocodingResponse?>
                ) {
                    if (response.body() != null) {
                        val results = response.body()!!.features()
                        if (results.size > 0) {
                            val feature = results[0]


                            mapboxMap!!.getStyle { style ->
                                if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                                    /*Toast.makeText(
                                        requireContext(),
                                        feature.placeName(),
                                        Toast.LENGTH_SHORT
                                    ).show()*/
                                }
                            }
                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                getString(R.string.app_name), Toast.LENGTH_SHORT
//                            ).show()
                        }
                    }
                }


                override fun onFailure(call: Call<GeocodingResponse?>, throwable: Throwable) {
                    Timber.e("Geocoding Failure: %s", throwable.message)
                }
            })
        } catch (servicesException: ServicesException) {
            Timber.e("Error geocoding: %s", servicesException.toString())
            servicesException.printStackTrace()
        }
    }
}