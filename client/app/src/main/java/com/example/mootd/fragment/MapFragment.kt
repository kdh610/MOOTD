package com.example.mootd.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.example.mootd.api.ResponseData
import com.example.mootd.api.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager


class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<PhotoClusterItem>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_map, container, false)


        // 지도 초기화
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 클러스터 매니저 초기화
        clusterManager = ClusterManager(requireContext(), mMap)
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        enableMyLocation()
    }

    private fun fetchPhotos(latitude: Double, longitude: Double, radius: Int) {
        RetrofitInstance.mapService.getPhotos(latitude, longitude, radius).enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseData ->
                        if (responseData.status == 200) {
                            Log.d("API Response", "Successfully fetched photos: ${responseData.data.size} photos found")
                            for (photo in responseData.data) {
                                addCustomMarker(photo)
                            }
                        } else {
                            Toast.makeText(requireContext(), "Error: ${responseData.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load photos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                Log.e("NetworkError", "Error occurred during network request", t)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addCustomMarker(photo: PhotoResponse) {
        val position = LatLng(photo.latitude, photo.longitude)

        // 이미지 다운로드 및 마커 설정
        Glide.with(this)
            .asBitmap()  // 비트맵 형식으로 이미지 로드
            .load(photo.maskImageUrl)  // 이미지 URL을 사용하여 로드
            .into(object : CustomTarget<Bitmap>() {
                // 비트맵이 준비되었을 때 호출되는 메서드
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Log.d("CustomMarker", "Bitmap loaded successfully for photo ID: ${photo.photoId}")

                    // 클러스터 아이템 생성
                    val clusterItem = PhotoClusterItem(
                        position,
                        "Photo ID: ${photo.photoId}",
                        "Latitude: ${photo.latitude}, Longitude: ${photo.longitude}",
                        photo.maskImageUrl
                    )

                    // 클러스터 매니저에 아이템 추가
                    clusterManager.addItem(clusterItem)

                    // 클러스터 업데이트
                    clusterManager.cluster()
                    Log.d("CustomMarker", "Cluster item added for photo ID: ${photo.photoId}")
                }

                // Glide가 이미지 로드를 중단할 때 호출되는 메서드
                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d("CustomMarker", "Glide onLoadCleared called for photo ID: ${photo.photoId}")
                    // 이미지 로드가 클리어되었을 때 아무 작업도 하지 않음
                }
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    // 이미지 로드 실패 시 로그 추가
                    Log.e("CustomMarker", "Failed to load image for photo ID: ${photo.photoId}")
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
                    Log.d("Location", "Fetching photos for location: Latitude = ${location.latitude}, Longitude = ${location.longitude}")
                    fetchPhotos(location.latitude, location.longitude, 10)
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
