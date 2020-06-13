package com.travelme.driver.fragments.maps_fragment

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.gson.JsonObject
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.*
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
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

    private var permissionsManager: PermissionsManager? = null
    private var mapboxMap: MapboxMap? = null
    private var isInTrackingMode: Boolean = false
    private val mapsViewModel: MapsViewModel by viewModel()

    private var pickup: CarmenFeature? = null
    private var destination: CarmenFeature? = null
    private val symbolIconId : String = "symbolIconId"
    private val geojsonSourceLayerId : String = "geojsonSourceLayerId"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Mapbox.getInstance(requireActivity(), getString(R.string.map_box_access_token))
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapsViewModel.getOrders(Constants.getToken(requireActivity()))
        mapsViewModel.listenToOrders().observe(viewLifecycleOwner, Observer { handleOrder(it) })
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            enabledLocationComponent(style)
        }
    }

    private fun handleOrder(it: List<Order>) {
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
            }
        }
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
            locationComponent.isLocationComponentEnabled = true
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
        mapView!!.onResume()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }
}