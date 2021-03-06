package com.amirHusseinSoori.mapboxwithkotlin

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.BuildConfig
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource


class GiveLocationFragment : DialogFragment(), com.mapbox.mapboxsdk.maps.OnMapReadyCallback,
        MapboxMap.OnMapClickListener,
        PermissionsListener {

    //for curreny location
    var locationlan = 35.678494
    var locationlong = 51.390880


    //for selected location
    var lat = 35.678494
    var long = 51.390880


    private var mapboxMap: MapboxMap? = null
    private lateinit var manager: LocationManager
    private var mapView: MapView? = null
    private var permissionsManager: PermissionsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
        manager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager;


    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.mylocation_fragment, container, false)
        mapView = rootView.findViewById(R.id.getLocationMap)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        return rootView

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            mapboxMap.uiSettings.isAttributionEnabled = false
            mapboxMap.uiSettings.isLogoEnabled = false
            enableCurrentLocation(style)


            //move camera
            val position = CameraPosition.Builder()
                    .target(LatLng(lat, long))
                    .zoom(17.0)
//                    .bearing(180.0)
                    .tilt(30.0)
                    .build()
            mapboxMap!!.animateCamera(
                    CameraUpdateFactory
                            .newCameraPosition(position), 7000
            )

            //create Custom Marker
            val symbolManager = SymbolManager(mapView!!, mapboxMap, style)
            symbolManager.iconAllowOverlap = true
            style.addImage("موقیعت شما", ContextCompat.getDrawable(requireActivity(), R.drawable.ic_pin)!!)
            symbolManager.create(SymbolOptions()
                    .withLatLng(LatLng(lat, long))
                    .withIconImage("موقیعت شما"))


        }
        mapboxMap.addOnMapClickListener(this)
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

    private fun enableCurrentLocation(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            val locationComponent = mapboxMap!!.locationComponent
            //active current location
            val locationActive = LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle).build()
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

    private fun initFeaturePointList(): List<Feature?>? {
        val symbolLayerIconFeatureList: ArrayList<Feature> = ArrayList()
        symbolLayerIconFeatureList.add(
                Feature.fromGeometry(Point.fromLngLat(66.298729, 42.928145))
        )
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        77.28305,
                        39.854244)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        61.544321,
                        46.026979)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        57.691611,
                        36.108244)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        53.756929,
                        32.797029)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        80.725897,
                        42.203685)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        76.217407,
                        44.876952)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        72.77456,
                        38.584169)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        74.414011,
                        31.687743)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        64.659278,
                        47.153569)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        70.807219,
                        46.649501)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        76.053462,
                        49.973477)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        68.757905,
                        43.940044)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        68.757905,
                        30.776557)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        59.740925,
                        29.714372)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        66.216756,
                        34.772613)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        64.659278,
                        50.497793)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        70.561301,
                        52.137028)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        54.576655,
                        49.443387)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        65.233086,
                        38.263068)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        80.561952,
                        48.202166)))
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(
                        58.593309,
                        41.101246)))
        return symbolLayerIconFeatureList
    }


}