package com.travelme.driver.fragments.maps_fragment

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.collection.LongSparseArray
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.gson.JsonObject
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.travelme.driver.R
import com.travelme.driver.models.Order
import com.travelme.driver.utilities.Constants
import kotlinx.android.synthetic.main.fragment_maps.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapsFragment : Fragment(), OnMapReadyCallback, PermissionsListener {
    companion object {
        const val TOKET = "Bearer blO7QbcV8N7pCUF3mwIrfwQ1OXqnGK9mZ8En2VFXlQZFB6Fqf4rU1R2iFPPQ7k5ymuQwrFs39uwdPYBi"
    }

    private var permissionsManager: PermissionsManager? = null
    private var mapboxMap: MapboxMap? = null
    private var isInTrackingMode: Boolean = false
    private val mapsViewModel: MapsViewModel by viewModel()

    private var pickup: CarmenFeature? = null
    private var destination: CarmenFeature? = null
    private val symbolIconId : String = "symbolIconId"
    private val geojsonSourceLayerId : String = "geojsonSourceLayerId"

    private lateinit var markerManager : MarkerViewManager
    private lateinit var symbolManager : SymbolManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Mapbox.getInstance(requireActivity(), getString(R.string.map_box_access_token))
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapsViewModel.getOrders(Constants.getToken(requireActivity()))
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            symbolManager = SymbolManager(mapView, mapboxMap, style)
            symbolManager.iconAllowOverlap = true
            symbolManager.textAllowOverlap = true

            enabledLocationComponent(style)
            markerManager = MarkerViewManager(mapView, mapboxMap)
            mapsViewModel.listenToOrders().observe(viewLifecycleOwner, Observer { handleOrder(it) })
        }
    }

    private fun handleOrder(it: List<Order>) {
        val symbols = mutableListOf<Symbol>()
        val symbolArray: LongSparseArray<Symbol> = symbolManager.annotations
        for (i in 0 until symbolArray.size()) {
            symbols.add(symbolArray.valueAt(i))
        }
        symbolManager.delete(symbols)
        it.map { order ->
            mapboxMap!!.setStyle(Style.MAPBOX_STREETS) { style ->
                enabledLocationComponent(style)

                val pickup = CarmenFeature.builder()
                    .geometry(Point.fromLngLat(order.lng_pickup_point!!.toDouble(), order.lat_pickup_point!!.toDouble()))
                    .properties(JsonObject())
                    .build()

                val destination = CarmenFeature.builder()
                    .geometry(Point.fromLngLat(order.lng_destination_point!!.toDouble(), order.lat_destination_point!!.toDouble()))
                    .properties(JsonObject())
                    .build()
                style.addSource(GeoJsonSource(geojsonSourceLayerId))
                setupLayer(style)

                println(pickup)
                println(destination)

                //pickup point marker
                addMarker(LatLng(order.lat_pickup_point!!.toDouble(), order.lng_pickup_point!!.toDouble()), order)
            }
        }
    }

    private fun getScreenInfo() : android.graphics.Point{
        val display: Display = requireActivity().windowManager.defaultDisplay
        val size = android.graphics.Point()
        try {
            display.getRealSize(size)
        } catch (err: NoSuchMethodError) {
            display.getSize(size)
        }
        val width: Int = size.x
        val height: Int = size.y
        return size
    }

    private fun addMarker(latlng: LatLng, order: Order){
        val parent = LinearLayout(requireActivity())
        parent.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        parent.orientation = LinearLayout.VERTICAL

        val size = getScreenInfo()
        val imageView = ImageView(requireActivity()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT)
            setImageBitmap(BitmapFactory.decodeResource(requireActivity().resources, R.drawable.mapbox_marker_icon_default))
        }

        val rel = LinearLayout(requireActivity())
        rel.orientation = LinearLayout.VERTICAL
        rel.layoutParams = ViewGroup.LayoutParams(size.x/2, ViewGroup.LayoutParams.WRAP_CONTENT)
        rel.setPadding(16)
        rel.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.mapboxGrayLight))

        val user = TextView(requireActivity()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            maxLines = 1
            text = order.user.name.toString()
            setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimary))
        }
        val orderId = TextView(requireActivity()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            maxLines = 1
            text = order.pickup_point.toString()
        }
        rel.addView(orderId)
        rel.addView(user)
        parent.addView(rel)
        parent.addView(imageView)

        val marker = MarkerView(latlng, parent)
        markerManager.addMarker(marker)

    }

    private fun setupLayer(@NonNull style: Style){
        style.addLayer(SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
            iconImage(symbolIconId),
            iconOffset(arrayOf(0f, -8f))
        ))
    }

    private fun enabledLocationComponent(@NonNull style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(requireActivity())) {
            val customLocationComponentOptions = LocationComponentOptions
                .builder(requireActivity())
                .elevation(5F)
                .accuracyAlpha(.6F)
                .accuracyColor(Color.RED)
                .foregroundDrawable(R.drawable.mapbox_marker_icon_default)
                .build()

            val locationComponent = mapboxMap!!.locationComponent
            val locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(requireActivity(), style)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            locationComponent.activateLocationComponent(locationComponentActivationOptions)
//            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
            locationComponent.addOnLocationClickListener {
                if (locationComponent.lastKnownLocation != null) {
                    toast(
                        resources.getString(
                            R.string.current_location,
                            locationComponent.lastKnownLocation!!.latitude,
                            locationComponent.lastKnownLocation!!.longitude
                        )
                    )
                }
            }
            locationComponent.addOnCameraTrackingChangedListener(object :
                OnCameraTrackingChangedListener {
                override fun onCameraTrackingChanged(currentMode: Int) {
                    toast(currentMode.toString())
                }

                override fun onCameraTrackingDismissed() {
                    isInTrackingMode = false
                }

            })
            my_location.setOnClickListener {
                if (!isInTrackingMode) {
                    isInTrackingMode = true
                    locationComponent.cameraMode = CameraMode.TRACKING
                    locationComponent.zoomWhileTracking(16.0)
                    toast(resources.getString(R.string.tracking_enabled))
                } else {
                    toast(resources.getString(R.string.tracking_already_enabled))
                }
            }

        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(requireActivity())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        toast(resources.getString(R.string.user_location_permission_explanation))
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mapboxMap!!.getStyle { style ->
                enabledLocationComponent(style)
            }
        } else {
            toast(resources.getString(R.string.user_location_permission_not_granted))
            requireActivity().finish()
        }
    }


    private fun toast(message: String) = Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()

    //lifecycle mapbox
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        markerManager.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
}