package com.amirHusseinSoori.mapboxwithkotlin


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.Style.OnStyleLoaded
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource


class ZoomBasedIconSwitchFragment : Fragment(R.layout.zoom_based_icon_switch), PermissionsListener, OnMapReadyCallback {

    private val ZOOM_LEVEL_FOR_SWITCH = 12f
    private val BLUE_PERSON_ICON_ID = "blue-car-icon-marker-icon-id"
    private val BLUE_PIN_ICON_ID = "blue-marker-icon-marker-icon-id"
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var permissionsManager: PermissionsManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token));
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.zoom_based_icon_switch, container, false)
        mapView = rootView.findViewById(R.id.mapViewZoom)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        return rootView

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.Builder().fromUri(Style.OUTDOORS)
                .withImage(BLUE_PERSON_ICON_ID, ContextCompat.getDrawable(requireActivity(), R.drawable.ic_plus)!!)

                .withImage(BLUE_PIN_ICON_ID, ContextCompat.getDrawable(requireActivity(), R.drawable.ic_pin)!!)
                .withSource(GeoJsonSource("source-id", FeatureCollection.fromFeatures(initFeatureArray()!!)

                ))

        ) { style ->
            var singleLayer = SymbolLayer("symbol-layer-id", "source-id");
            singleLayer.setProperties(
                    iconImage(step(zoom(), literal(BLUE_PERSON_ICON_ID),
                            stop(ZOOM_LEVEL_FOR_SWITCH, BLUE_PIN_ICON_ID))),
                    iconIgnorePlacement(true),
                    iconAllowOverlap(true));
            style.addLayer(singleLayer);

      
        }


    }

    private fun initFeatureArray(): ArrayList<Feature>? {
        var list = ArrayList<Feature>()
        list.add(Feature.fromGeometry(Point.fromLngLat(51.380884, 35.694668)))
        list.add(Feature.fromGeometry(Point.fromLngLat(51.360271, 35.702196)))
        list.add(Feature.fromGeometry(Point.fromLngLat(51.440319, 35.712789)))
        list.add(Feature.fromGeometry(Point.fromLngLat(51.434478, 35.702196)))
        list.add(Feature.fromGeometry(Point.fromLngLat(51.343780, 35.692158)))
        list.add(Feature.fromGeometry(Point.fromLngLat(51.479140, 35.721988)))
        list.add(Feature.fromGeometry(Point.fromLngLat(51.391175, 35.689649)))
        list.add(Feature.fromGeometry(Point.fromLngLat(51.485652, 35.714462)))
        list.add(Feature.fromGeometry(Point.fromLngLat(51.445796, 35.702753)))
        return list


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

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
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

            }
        } else {
//            Toast.makeText(requireContext(), R.string.app_name, Toast.LENGTH_LONG).show()
        }
    }




}