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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
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
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.travelme.driver.R
import com.travelme.driver.extensions.gone
import com.travelme.driver.extensions.visible
import com.travelme.driver.models.Order
import com.travelme.driver.utilities.Constants
import kotlinx.android.synthetic.main.bottom_sheet_maps.*
import kotlinx.android.synthetic.main.fragment_maps.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapsFragment : Fragment(), OnMapReadyCallback, PermissionsListener {

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

    private var navigationMapRoute : NavigationMapRoute? = null
    private var currentRoute: DirectionsRoute? = null
    private var locationComponent : LocationComponent? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Mapbox.getInstance(requireActivity(), getString(R.string.map_box_access_token))
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_bottom_sheet.apply {
            adapter = BottomSheetMapsAdapter(mutableListOf(), requireActivity(), mapsViewModel)
            layoutManager = LinearLayoutManager(requireActivity())
        }
        //mapsViewModel.getOrders(Constants.getToken(requireActivity()))
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        BottomSheetBehavior.from(layoutBottomSheet)
        mapsViewModel.listenToState().observer(viewLifecycleOwner, Observer { handleState(it) })
    }

    private fun handleState(it : MapsState){
        when(it){
            is MapsState.IsLoading -> {
                if (it.state){
                    pb_bottom_sheet.visible()
                }else{
                    pb_bottom_sheet.gone()
                }
            }

            is MapsState.ShowToast -> toast(it.message)
            is MapsState.SuccessArrived -> {
                toast("anda berada di lokasi penjemputan")
                mapsViewModel.getOrders(Constants.getToken(requireActivity()))
            }

            is MapsState.SuccessDone -> {
                toast("anda berada di lokasi tujuan")
                mapsViewModel.getOrders(Constants.getToken(requireActivity()))
            }
        }
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
                //enabledLocationComponent(style)
                style.addSource(GeoJsonSource(geojsonSourceLayerId))
                setupLayer(style)

                //pickup point marker
                addMarkerPickup(LatLng(order.lat_pickup_point!!.toDouble(), order.lng_pickup_point!!.toDouble()), order)
                addMarkerDestination(LatLng(order.lat_destination_point!!.toDouble(), order.lng_destination_point!!.toDouble()), order)

                val originPoint  = Point.fromLngLat(locationComponent!!.lastKnownLocation!!.longitude,
                    locationComponent!!.lastKnownLocation!!.latitude)

                val destinationPoint  = Point.fromLngLat(order.lng_pickup_point!!.toDouble(), order.lat_pickup_point!!.toDouble())

                val source = style.getSourceAs<GeoJsonSource>(geojsonSourceLayerId)
                source?.setGeoJson(Feature.fromGeometry(destinationPoint))
                getRoute(originPoint, destinationPoint)

                btn_start_navigation.setOnClickListener {
                    val options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(true)
                        .build()
                    NavigationLauncher.startNavigation(requireActivity(), options)
                }
            }
        }

        rv_bottom_sheet.adapter?.let {adapter ->
            if (adapter is BottomSheetMapsAdapter){
                adapter.changelist(it)
            }
        }
    }

    private fun getRoute(origin: Point, destination : Point){
        NavigationRoute.builder(requireActivity())
            .accessToken(getString(R.string.map_box_access_token))
            .origin(origin)
            .destination(destination)
            .build()
            .getRoute(object : Callback <DirectionsResponse>{
                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    println("failure : ${t.message}")
                }

                override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                    if (response.isSuccessful){
                        val body = response.body()
                        if (body == null){
                            println("No routes found, check right user and access token")
                            return
                        }else if (body.routes().size == 0){
                            println("no routes found")
                            return
                        }
                        println("current route : ${response.body()!!.routes().get(0)}")

                        currentRoute = response.body()!!.routes().get(0)
                        btn_start_navigation.isEnabled = true
                        btn_start_navigation.setBackgroundResource(R.color.mapboxBlue)

                        if (navigationMapRoute != null){
                            navigationMapRoute!!.removeRoute()
                        }else {
                            navigationMapRoute = NavigationMapRoute(null, mapView, mapboxMap!!, R.style.NavigationMapRoute)
                        }
                        navigationMapRoute!!.addRoute(currentRoute)
                    }else{
                        println("r : ${response.message()}")
                    }
                }

            })
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

    private fun addMarkerPickup(latlng: LatLng, order: Order){
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

        if (order.arrived == true){
            marker.let { markerManager.removeMarker(it) }
        }else if (order.done == true){
            marker.let { markerManager.removeMarker(it) }
        }else{
            markerManager.addMarker(marker)
        }

    }

    private fun addMarkerDestination(latlng: LatLng, order: Order){
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
            text = order.destination_point.toString()
        }
        rel.addView(orderId)
        rel.addView(user)
        parent.addView(rel)
        parent.addView(imageView)

        val marker = MarkerView(latlng, parent)
        if (order.arrived == true){
            marker.let { markerManager.removeMarker(it) }
        }else if (order.done == true){
            marker.let { markerManager.removeMarker(it) }
        }else{
            markerManager.addMarker(marker)
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
                .accuracyColor(Color.CYAN)
                //.foregroundDrawable(R.drawable.mapbox_marker_icon_default)
                .build()

            locationComponent = mapboxMap!!.locationComponent
            val locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(requireActivity(), style)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            locationComponent!!.activateLocationComponent(locationComponentActivationOptions)
            locationComponent!!.isLocationComponentEnabled = true
            locationComponent!!.cameraMode = CameraMode.TRACKING
            locationComponent!!.renderMode = RenderMode.COMPASS
            locationComponent!!.addOnLocationClickListener {
                /*if (locationComponent.lastKnownLocation != null) {

                    origin = Point.fromLngLat(locationComponent.lastKnownLocation!!.longitude, locationComponent.lastKnownLocation!!.latitude)

                    toast(
                        resources.getString(
                            R.string.current_location,
                            locationComponent.lastKnownLocation!!.latitude,
                            locationComponent.lastKnownLocation!!.longitude
                        )
                    )
                }*/
            }
            locationComponent!!.addOnCameraTrackingChangedListener(object :
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
                    locationComponent!!.cameraMode = CameraMode.TRACKING
                    locationComponent!!.zoomWhileTracking(16.0)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) = toast(resources.getString(R.string.user_location_permission_explanation))

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
        mapsViewModel.getOrders(Constants.getToken(requireActivity()))
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