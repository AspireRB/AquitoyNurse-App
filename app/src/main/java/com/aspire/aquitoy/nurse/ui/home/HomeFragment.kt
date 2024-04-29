package com.aspire.aquitoy.nurse.ui.home


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aspire.aquitoy.nurse.Common
import com.aspire.aquitoy.nurse.R
import com.aspire.aquitoy.nurse.data.ApiService
import com.aspire.aquitoy.nurse.databinding.FragmentHomeBinding
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Suppress("DEPRECATION")
class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    //Location
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var coordinates: LatLng = LatLng(0.0, 0.0)
    private var start: String = ""
    private var end: String = ""

    var poly: Polyline? = null

    //Realtime
    private lateinit var onlineRef: DatabaseReference
    private lateinit var currentUserRef: DatabaseReference
    private lateinit var nurseLocationRef: DatabaseReference
    private lateinit var geoFire: GeoFire

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
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap!!

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
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
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true
                    map.setOnMyLocationButtonClickListener {
                        fusedLocationProviderClient.lastLocation
                            .addOnFailureListener { e ->
                                Snackbar.make(requireView(),e.message!!,
                                    Snackbar.LENGTH_LONG).show()
                            }
                            .addOnSuccessListener { location ->
                                val userLatLng = LatLng(location.latitude,location.longitude)
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,18f))
                            }
                        true
                    }
                    val locationButton = (mapFragment.requireView()!!.findViewById<View>("1".toInt())!!
                        .parent!! as View)
                        .findViewById<View>("2".toInt())
                    val params = locationButton.layoutParams as RelativeLayout.LayoutParams
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                    params.bottomMargin = 50
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Snackbar.make(requireView(),p0!!.permissionName+" Permiso necesario",
                        Snackbar.LENGTH_LONG).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }

            })
            .check()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        createMapFragment()
        init()
        initListeners()

        return root
    }

    private fun init() {
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected")
        nurseLocationRef = FirebaseDatabase.getInstance().getReference(Common
            .NURSE_LOCATION_REFERENCE)
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.NURSE_LOCATION_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        geoFire = GeoFire(nurseLocationRef)
        registerOnlineSystem()

        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setFastestInterval(3000)
        locationRequest.setSmallestDisplacement(10f)
        locationRequest.interval = 5000

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0?.let { super.onLocationResult(it) }
                p0?.lastLocation?.let { location ->
                    val newPos = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 18f))
                    coordinates = newPos

                    //Update Location
                    geoFire.setLocation(
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        GeoLocation(coordinates.longitude, coordinates.latitude)
                    ){ key:String?, error: DatabaseError? ->
                        if (error != null)
                            Snackbar.make(mapFragment.requireView(),error.message,Snackbar
                                .LENGTH_LONG).show()
                        else
                            Snackbar.make(mapFragment.requireView(),"Online",Snackbar
                                .LENGTH_SHORT).show()
                    }
                }
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
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
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        lifecycleScope.cancel()
    }

    private fun initListeners() {
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
        mapFragment = childFragmentManager.findFragmentById(R.id.Map) as SupportMapFragment
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