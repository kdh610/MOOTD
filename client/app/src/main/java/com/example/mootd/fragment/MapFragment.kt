package com.example.mootd.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.mootd.R
import com.example.mootd.api.MapService
import com.example.mootd.api.PhotoResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mapService: MapService
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://k11a105.p.ssafy.io:8081")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        MapService = retrofit.create(MapService::class.java)

        // Google Maps 초기화
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()
    }

    private fun fetchPhotos(latitude: Double, longitude: Double, radius: Int) {
        MapService.getPhotos(latitude, longitude, radius).enqueue(object : Callback<List<PhotoResponse>> {
            override fun onResponse(call: Call<List<PhotoResponse>>, response: Response<List<PhotoResponse>>) {
                if (response.isSuccessful) {
                    response.body()?.let { photos ->
                        for (photo in photos) {
                            addCustomMarker(photo)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load photos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<PhotoResponse>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addCustomMarker(photo: PhotoResponse) {
        val position = LatLng(photo.latitude, photo.longitude)

        // 이미지 다운로드 및 마커 설정
        Glide.with(this)
            .asBitmap()  // 비트맵 형식으로 이미지 로드
            .load(photo.imageUrl)  // 이미지 URL을 사용하여 로드
            .into(object : CustomTarget<Bitmap>() {
                // 비트맵이 준비되었을 때 호출되는 메서드
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val markerOptions = MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromBitmap(resource))  // 비트맵을 마커 아이콘으로 설정
                        .title("Photo ID: ${photo.id}")

                    // 마커 추가
                    mMap.addMarker(markerOptions)
                }

                // Glide가 이미지 로드를 중단할 때 호출되는 메서드
                override fun onLoadCleared(placeholder: Drawable?) {
                    // 이미지 로드가 클리어되었을 때 아무 작업도 하지 않음
                }
            })
    }


    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.isMyLocationEnabled = true
                getDeviceLocation()
            } catch (e: SecurityException) {
                // SecurityException 발생 시 예외 처리
                Toast.makeText(requireContext(), "Location permission error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 위치 권한 요청
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun getDeviceLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("My Location"))

                    fetchPhotos(location.latitude, location.longitude, 5)
                } else {
                    Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            // SecurityException 발생 시 예외 처리
            Toast.makeText(requireContext(), "Location access error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val DEFAULT_ZOOM = 15f
    }
}
