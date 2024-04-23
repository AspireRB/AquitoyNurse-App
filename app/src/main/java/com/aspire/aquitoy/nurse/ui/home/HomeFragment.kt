package com.aspire.aquitoy.nurse.ui.home


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aspire.aquitoy.nurse.Common
import com.aspire.aquitoy.nurse.R
import com.aspire.aquitoy.nurse.data.ApiService
import com.aspire.aquitoy.nurse.data.LocationService
import com.aspire.aquitoy.nurse.databinding.FragmentHomeBinding
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private val locationService: LocationService = LocationService()

    private var coordinates: LatLng = LatLng(0.0, 0.0)
    private var start: String = ""
    private var end: String = ""

    var poly: Polyline? = null

    private lateinit var onlineRef: DatabaseReference
    private lateinit var currentUserRef: DatabaseReference
    private lateinit var nurseLocationRef: DatabaseReference
    private lateinit var geoFire: GeoFire
    private lateinit var mapFragment: SupportMapFragment

    private val onlineValueEventListener = object: ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
            Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_SHORT).show()
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists())
                currentUserRef.onDisconnect().removeValue()
        }
    }

    override fun onDestroy() {
        geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        onlineRef.removeEventListener(onlineValueEventListener)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        registerOnlineSystem()
    }

    private fun registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinates, 18f),
            4000,
            null
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        createMapFragment()
        lifecycleScope.launch {
            // Inicializar coordinates antes de llamar a initListeners()
            coordinates = obtainUserLocation() ?: LatLng(0.0, 0.0)
            initListeners()
        }
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected")
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.NURSE_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        nurseLocationRef = FirebaseDatabase.getInstance().getReference(Common.NURSE_INFO_REFERENCE)
        geoFire = GeoFire(nurseLocationRef)
        registerOnlineSystem()

        return root
    }

    private suspend fun obtainUserLocation(): LatLng? {
        return suspendCoroutine { continuation ->
            lifecycleScope.launch {
                val result = locationService.getUserLocation(requireContext())
                result?.let { location ->
                    continuation.resume(LatLng(location.latitude, location.longitude))
                } ?: run {
                    continuation.resume(null)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        lifecycleScope.cancel()
    }

    private fun initUI() {
        createMapFragment()
        initListeners()
    }

    private fun initListeners() {
        geoFire.setLocation(
            FirebaseAuth.getInstance().currentUser!!.uid,
            GeoLocation(coordinates.longitude, coordinates.latitude)
        )
        _binding!!.btnService.setOnClickListener {
            start = "${coordinates.longitude}, ${coordinates.latitude}"
            Toast.makeText(requireContext(), "coordenadas: $start", Toast.LENGTH_SHORT).show()
            end = ""
            poly?.remove()
            if (poly != null) {
                poly = null
            }
            if (::map.isInitialized) {
                map.setOnMapClickListener {
                    if (end.isEmpty()){
                        end = "${it.longitude}, ${it.latitude}"
                    } else {
                        createRoute()
                    }
                }
            }
        }
    }

    private fun createMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.Map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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