package com.travelme.driver.ui.maps

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Display
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.travelme.driver.R
import com.travelme.driver.extensions.gone
import com.travelme.driver.extensions.visible
import com.travelme.driver.models.Order
import com.travelme.driver.utilities.Constants
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.bottom_sheet_maps.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

    private var permissionsManager: PermissionsManager? = null
    private var mapboxMap: MapboxMap? = null
    private val mapsViewModel: MapsViewModel by viewModel()

    private val symbolIconId : String = "symbolIconId"
    private val geojsonSourceLayerId : String = "geojsonSourceLayerId"
    private val pulauJawa: LatLng = LatLng(-7.614529, 110.712247)

    private lateinit var markerManager : MarkerViewManager
    private var locationComponent : LocationComponent? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.map_box_access_token))
        setContentView(R.layout.activity_maps)
        supportActionBar?.hide()
        rv_bottom_sheet.apply {
            adapter = BottomSheetMapsAdapter(mutableListOf(), this@MapsActivity, mapsViewModel)
            layoutManager = LinearLayoutManager(this@MapsActivity)
        }

        mapView.onCreate(savedInstanceState)
        mapsViewModel.listenToState().observer(this, Observer { handleState(it) })
        mapView.getMapAsync(this)
        BottomSheetBehavior.from(layoutBottomSheet)
        //sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
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
                mapsViewModel.getOrders(Constants.getToken(this))
            }

            is MapsState.SuccessDone -> {
                toast("anda berada di lokasi tujuan")
                mapsViewModel.getOrders(Constants.getToken(this))
            }
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pulauJawa, 6.0))
//        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//            LatLng(locationComponent!!.lastKnownLocation!!.latitude,
//                locationComponent!!.lastKnownLocation!!.longitude), 8.5))
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            //            symbolManager = SymbolManager(mapView, mapboxMap, style)
//            symbolManager.iconAllowOverlap = true
//            symbolManager.textAllowOverlap = true

            style.addSource(GeoJsonSource(geojsonSourceLayerId))
            setupLayer(style)

            enabledLocationComponent(style)
            markerManager = MarkerViewManager(mapView, mapboxMap)
            mapsViewModel.listenToOrders().observe(this, Observer { handleOrder(it) })
        }
    }

    private fun handleOrder(it: List<Order>){
//        val symbols = mutableListOf<Symbol>()
//        val symbolArray: LongSparseArray<Symbol> = symbolManager.annotations
//        for (i in 0 until symbolArray.size()) {
//            symbols.add(symbolArray.valueAt(i))
//        }
//        symbolManager.delete(symbols)

        rv_bottom_sheet.adapter?.let { adapter ->
            if (adapter is BottomSheetMapsAdapter){
                adapter.changelist(it)
            }
        }

        it.forEach { order ->
            println(order.arrived)
            addMarker(LatLng(order.lat_pickup_point!!.toDouble(), order.lng_pickup_point!!.toDouble()), order, order.pickup_point!!)
            addMarker(LatLng(order.lat_destination_point!!.toDouble(), order.lng_destination_point!!.toDouble()), order, order.destination_point!!)
//            mapboxMap!!.setStyle(Style.MAPBOX_STREETS) { style ->
//                //enabledLocationComponent(style)
//                style.addSource(GeoJsonSource(geojsonSourceLayerId))
//                setupLayer(style)
//
//                //pickup point marker
//                toast("ashiap")
//                println("shiap")
//
//
//
//                val originPoint  = Point.fromLngLat(locationComponent!!.lastKnownLocation!!.longitude,
//                    locationComponent!!.lastKnownLocation!!.latitude)
//
//                val destinationPoint  = Point.fromLngLat(order.lng_pickup_point!!.toDouble(), order.lat_pickup_point!!.toDouble())
//
//                val source = style.getSourceAs<GeoJsonSource>(geojsonSourceLayerId)
//                source?.setGeoJson(Feature.fromGeometry(destinationPoint))
//                getRoute(originPoint, destinationPoint)
//
//                btn_start_navigation.setOnClickListener {
//                    val options = NavigationLauncherOptions.builder()
//                        .directionsRoute(currentRoute)
//                        .shouldSimulateRoute(true)
//                        .build()
//                    NavigationLauncher.startNavigation(this, options)
//                }
//            }
        }
//        rv_bottom_sheet.adapter?.let {adapter ->
//            if (adapter is BottomSheetMapsAdapter){
//                adapter.changelist(it)
//            }
//        }
    }

    private fun getScreenInfo() : android.graphics.Point{
        val display: Display = this.windowManager.defaultDisplay
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

    private fun addMarker(latlng: LatLng, order: Order, addressName : String){
        val parent = LinearLayout(this)
        parent.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        parent.orientation = LinearLayout.VERTICAL

        val size = getScreenInfo()
        val imageView = ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT)
            setImageBitmap(BitmapFactory.decodeResource(this.resources, R.drawable.mapbox_marker_icon_default))
        }

        val rel = LinearLayout(this)
        rel.orientation = LinearLayout.VERTICAL
        rel.layoutParams = ViewGroup.LayoutParams(size.x/2, ViewGroup.LayoutParams.WRAP_CONTENT)
        rel.setPadding(16)
        rel.setBackgroundColor(ContextCompat.getColor(this, R.color.mapboxGrayLight))

        val user = TextView(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            maxLines = 1
            text = order.user.name.toString()
            setTextColor(ContextCompat.getColor(this@MapsActivity, R.color.colorPrimary))
        }
        val orderId = TextView(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            maxLines = 1
            text = addressName
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
        style.addLayer(
            SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                PropertyFactory.iconImage(symbolIconId),
                PropertyFactory.iconOffset(arrayOf(0f, -8f))
            ))
    }

    private fun enabledLocationComponent(@NonNull style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val customLocationComponentOptions = LocationComponentOptions
                .builder(this)
                .elevation(5F)
                .accuracyAlpha(.6F)
                .accuracyColor(Color.CYAN)
                //.foregroundDrawable(R.drawable.mapbox_marker_icon_default)
                .build()

            locationComponent = mapboxMap!!.locationComponent
            val locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(this, style)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            locationComponent!!.activateLocationComponent(locationComponentActivationOptions)
            locationComponent!!.isLocationComponentEnabled = true
            locationComponent!!.cameraMode = CameraMode.TRACKING
            locationComponent!!.renderMode = RenderMode.COMPASS
            my_location.setOnClickListener {
                locationComponent!!.cameraMode = CameraMode.TRACKING
                locationComponent!!.zoomWhileTracking(16.0)
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this)
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
            //toast(resources.getString(R.string.user_location_permission_not_granted))
            this.finish()
        }
    }


    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    //lifecycle mapbox
    override fun onResume() {
        super.onResume()
        mapsViewModel.getOrders(Constants.getToken(this))
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
