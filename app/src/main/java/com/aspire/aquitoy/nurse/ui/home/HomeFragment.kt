package com.aspire.aquitoy.nurse.ui.home


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aspire.aquitoy.nurse.R
import com.aspire.aquitoy.nurse.data.ApiService
import com.aspire.aquitoy.nurse.data.LocationService
import com.aspire.aquitoy.nurse.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private val locationService: LocationService = LocationService()

    private var start: String = ""
    private var end: String = ""

    var poly: Polyline? = null

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        createMarker()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        createMapFragment()
        initListeners()

        return root
    }

    private fun initListeners() {
        _binding!!.btnService.setOnClickListener {
            start = ""
            end = ""
            poly?.remove()
            if (poly != null) {
                poly = null
            }
            if (::map.isInitialized) {
                map.setOnMapClickListener {
                    if (start.isEmpty()) {
                        start = "${it.longitude}, ${it.latitude}"
                    } else if (end.isEmpty()){
                        end = "${it.longitude}, ${it.latitude}"
                    } else {
                        createRoute()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.Map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun createMarker() {
        lifecycleScope.launch {
            val result = locationService.getUserLocation(requireContext())
            result?.let { location ->
                val coordinates = LatLng(location.latitude, location.longitude)
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(coordinates, 18f),
                    4000,
                    null
                )
            }
        }
    }

    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createRoute() {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java).getRoute("5b3ce3597851110001cf62483630a995a4b0492894d42ad8669f8933", start, end,)
            if (call.isSuccessful) {
                drawRoute(call.body())
                Log.i("Aspire", "OK")
            } else {
                Log.i("Aspire", "NO OK")
            }
        }
    }

    private fun drawRoute(routeResponse: RouteResponse?) {
        val polyLineOptions = PolylineOptions()
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polyLineOptions.add(LatLng(it[1], it[0]))
        }
        activity?.runOnUiThread {
            poly = map.addPolyline(polyLineOptions)
        }
    }
}