package com.amirHusseinSoori.mapboxwithkotlin

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.search_fragment.*


class SearchFragment : Fragment(R.layout.search_fragment), OnMapReadyCallback {
    private val REQUEST_CODE_AUTOCOMPLETE = 1

    private var mapboxMap: MapboxMap? = null
    private var home: CarmenFeature? = null
    private var work: CarmenFeature? = null
    private val geojsonSourceLayerId = "geojsonSourceLayerId"
    private val symbolIconId = "symbolIconId"


    override fun onCreate(savedInstanceState: Bundle?) {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token));
        super.onCreate(savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        searchMap!!.onCreate(savedInstanceState)
        searchMap!!.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            initSearchFab();
            addUserLocations();

          // Create an empty GeoJSON source using the empty feature collection
           style.addImage("موقیعت شما", ContextCompat.getDrawable(requireActivity(), R.drawable.ic_pin)!!)



            setUpSource(style)
           // Set up a new symbol layer for displaying the searched location's feature coordinates
            setupLayer(style);

        }

    }


    private fun initSearchFab() {
        fab_location_search!!.setOnClickListener {

            val intent = PlaceAutocomplete.IntentBuilder()
                    .accessToken((if (Mapbox.getAccessToken() != null) Mapbox.getAccessToken() else getString(R.string.mapbox_access_token))!!)
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#EEEEEE"))
                            .limit(10)
                            .addInjectedFeature(home)
                            .addInjectedFeature(work)
                            .build(PlaceOptions.MODE_CARDS))
                    .build(requireActivity())
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)
        }


    }

    private fun addUserLocations() {
        home = CarmenFeature.builder().text("Mapbox SF Office")
                .geometry(Point.fromLngLat(-122.3964485, 37.7912561))
                .placeName("50 Beale St, San Francisco, CA")
                .id("mapbox-sf")
                .properties(JsonObject())
                .build()
        work = CarmenFeature.builder().text("Mapbox DC Office")
                .placeName("740 15th Street NW, Washington DC")
                .geometry(Point.fromLngLat(-77.0338348, 38.899750))
                .id("mapbox-dc")
                .properties(JsonObject())
                .build()
    }


    private fun setUpSource(loadedMapStyle: Style) {
        loadedMapStyle.addSource(GeoJsonSource(geojsonSourceLayerId))
    }

    private fun setupLayer(loadedMapStyle: Style) {
        loadedMapStyle.addLayer(SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage("symbolIconId"),
                iconOffset(arrayOf(0f, -8f))
        ))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

             // Retrieve selected location's CarmenFeature
            val selectedCarmenFeature = PlaceAutocomplete.getPlace(data)

              // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
             // Then retrieve and update the source designated for showing a selected location's symbol layer icon
            if (mapboxMap != null) {
                val style = mapboxMap!!.style
                if (style != null) {
                    val source = style.getSourceAs<GeoJsonSource>(geojsonSourceLayerId)
                    source?.setGeoJson(FeatureCollection.fromFeatures(arrayOf(Feature.fromJson(selectedCarmenFeature.toJson()))))

                    // Move map camera to the selected location
                    mapboxMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                    .target(LatLng((selectedCarmenFeature.geometry() as Point?)!!.latitude(),
                                            (selectedCarmenFeature.geometry() as Point?)!!.longitude()))
                                    .zoom(14.0)
                                    .build()), 4000)
                }
            }
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    override fun onResume() {
        super.onResume()
        searchMap!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        searchMap!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        searchMap!!.onStop()
    }

    override fun onPause() {
        super.onPause()
        searchMap!!.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        searchMap!!.onLowMemory()
    }

     override fun onDestroy() {
        super.onDestroy()
//         searchMap!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        searchMap!!.onSaveInstanceState(outState)
    }

}
