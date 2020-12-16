package com.amirHusseinSoori.mapboxwithkotlin

import android.content.Context
import android.graphics.Color
import android.location.LocationManager
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
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils


class GetMultiCircleMarkerFragment : Fragment(R.layout.get_multi_circle_marker_fragment)  , com.mapbox.mapboxsdk.maps.OnMapReadyCallback,
MapboxMap.OnMapClickListener,
        PermissionsListener {

    //for curreny location
    var locationlan = 35.678494
    var locationlong = 51.390880


    //for selected location
    var lat = 35.678494
    var long = 51.390880


    private val BASE_CIRCLE_INITIAL_RADIUS = 3.4f
    private val RADIUS_WHEN_CIRCLES_MATCH_ICON_RADIUS = 14f
    private val ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION = 11f
    private val ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON = 12f
    private val FINAL_OPACITY_OF_SHADING_CIRCLE = .5f
    private val BASE_CIRCLE_COLOR = "#FFEB3B"
    private val SHADING_CIRCLE_COLOR = "#FFEB3B"
    private val SOURCE_ID = "SOURCE_ID"
    private val ICON_LAYER_ID = "ICON_LAYER_ID"
    private val BASE_CIRCLE_LAYER_ID = "BASE_CIRCLE_LAYER_ID"
    private val SHADOW_CIRCLE_LAYER_ID = "SHADOW_CIRCLE_LAYER_ID"
    private val ICON_IMAGE_ID = "ICON_ID"

    private var mapboxMap: MapboxMap? = null
    private lateinit var manager: LocationManager
    private var mapView: MapView? = null
    private var permissionsManager: PermissionsManager? = null

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
        val rootView = inflater.inflate(R.layout.get_multi_circle_marker_fragment, container, false)
        mapView = rootView.findViewById(R.id.mapViewCustom)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        return rootView

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.Builder().fromUri(Style.LIGHT)

                // Add images to the map so that the SymbolLayers can reference the images.
                .withImage(ICON_IMAGE_ID, BitmapUtils.getBitmapFromDrawable(
                        ContextCompat.getDrawable(requireActivity(), R.drawable.ic_plus))!!)       // Add GeoJSON data to the GeoJsonSource and then add the GeoJsonSource to the map
                .withSource(GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(initFeatureArray()!!)))
        ) { style ->          // Add the base CircleLayer, which will show small circles when the map is zoomed far enough
                              // away from the map.

            val baseCircleLayer = CircleLayer(BASE_CIRCLE_LAYER_ID, SOURCE_ID).withProperties(
                    circleColor(Color.parseColor(BASE_CIRCLE_COLOR)),
                    circleRadius(
                            interpolate(
                                    linear(), zoom(),
                                    stop(ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION, BASE_CIRCLE_INITIAL_RADIUS),
                                    stop(ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON, RADIUS_WHEN_CIRCLES_MATCH_ICON_RADIUS)
                            )
                    )
            )
            style.addLayer(baseCircleLayer)

                  // Add a "shading" CircleLayer, whose circles' radii will match the radius of the SymbolLayer
                 // circular icon
            val shadowTransitionCircleLayer = CircleLayer(SHADOW_CIRCLE_LAYER_ID, SOURCE_ID)
                    .withProperties(
                            circleColor(Color.parseColor(SHADING_CIRCLE_COLOR)),
                            circleRadius(RADIUS_WHEN_CIRCLES_MATCH_ICON_RADIUS),
                            circleOpacity(
                                    interpolate(
                                            linear(), zoom(),
                                            stop(ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION - .5, 0),
                                            stop(ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION, FINAL_OPACITY_OF_SHADING_CIRCLE)
                                    )
                            )
                    )
            style.addLayerBelow(shadowTransitionCircleLayer, BASE_CIRCLE_LAYER_ID)

            // Add the SymbolLayer
            val symbolIconLayer = SymbolLayer(ICON_LAYER_ID, SOURCE_ID)
            symbolIconLayer.withProperties(
                    iconImage(ICON_IMAGE_ID),
                    iconSize(1.5f),
                    iconIgnorePlacement(true),
                    iconAllowOverlap(true)
            )
            symbolIconLayer.minZoom = ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON
            style.addLayer(symbolIconLayer)
            Toast.makeText(requireContext(),
                    R.string.app_name, Toast.LENGTH_SHORT).show()

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

        private fun initFeatureArray(): ArrayList<Feature>? {
            var list=ArrayList<Feature>()
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







}