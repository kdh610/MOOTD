package com.example.mootd.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import com.example.mootd.map.CustomClusterRenderer
import com.google.maps.android.clustering.Cluster
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<PhotoClusterItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_map, container, false)

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

        val customClusterRenderer = CustomClusterRenderer(this, requireContext(), mMap, clusterManager)

        clusterManager.renderer = customClusterRenderer

        // 확대/축소 및 카메라 이동 시 클러스터를 다시 렌더링
        mMap.setOnCameraIdleListener {
            clusterManager.cluster() // 클러스터 다시 계산
        }

        mMap.setOnMarkerClickListener(clusterManager)


        clusterManager.setOnClusterClickListener { cluster ->
            openGalleryViewForCluster(cluster)
            true
        }


        enableMyLocation()
    }

    private fun openGalleryViewForCluster(cluster: Cluster<PhotoClusterItem>) {
        val photoUrlsWithId = cluster.items
            .filter { it.getImageId() != null }
            .map { Pair(it.getImageId(), it.getImageUrl()) }

        val photoUrlsWithoutId = cluster.items
            .filter { it.getImageId() == null }
            .map { it.getImageUrl() }

        val bundle = Bundle().apply {
            putSerializable("photoUrlsWithId", ArrayList(photoUrlsWithId))
            putStringArrayList("photoUrlsWithoutId", ArrayList(photoUrlsWithoutId))
        }

        findNavController().navigate(R.id.mapClusterGalleryFragment, bundle)
    }

    private fun openDetailFragment(photoId: String, imageUrl: String) {
        val bundle = Bundle().apply {
            putString("photoId", photoId)
            putString("imageUrl", imageUrl)
        }

        // GuideDetailFragment로 이동
        findNavController().navigate(R.id.action_mapFragment_to_guideDetailFragment, bundle)
    }


    private fun fetchPhotos(latitude: Double, longitude: Double, radius: Int) {
        RetrofitInstance.mapService.getPhotos(latitude, longitude, radius).enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseData ->

                        if (responseData.status == 200) {
                            for (photo in responseData.data) {
                                Log.d("MapFragment", "Adding marker with URL: ${photo.maskImageUrl}")
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

        Glide.with(this)
            .asBitmap()
            .load(photo.maskImageUrl)
            .override(200, 200)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                    val markerBitmap = createBubbleBitmap(resource)

                    val clusterItem = PhotoClusterItem(
                        position,
                        "Photo ID: ${photo.photoId}",
                        "Latitude: ${photo.latitude}, Longitude: ${photo.longitude}",
                        photo.maskImageUrl,
                        photo.photoId,
                        markerBitmap
                    )
                    Log.d("CustomMarker", "maskImageUrl: ${photo.maskImageUrl}")

                    clusterManager.addItem(clusterItem)

                }

                // Glide가 이미지 로드를 중단할 때 호출되는 메서드
                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d("CustomMarker", "Glide onLoadCleared called for photo ID: ${photo.photoId}")
                    if (placeholder == null) {
                        Log.d("CustomMarker", "No placeholder image loaded")
                    } else {
                        Log.d("CustomMarker", "Placeholder image loaded")
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.e("CustomMarker", "Failed to load image for photo ID: ${photo.photoId}")
                    Log.d("ImageURL", "Attempting to load image from URL: ${photo.maskImageUrl}")

                }
            })
    }

    private fun createBubbleBitmap(originalBitmap: Bitmap): Bitmap {

        val size = 200
        val croppedBitmap = cropToSquare(originalBitmap, size)

        val borderWidth = 10
        val offsetY = 25
        val backgroundScaleFactor = 1.2f

        val background = ContextCompat.getDrawable(requireContext(), R.drawable.bubble)
        val backgroundWidth = (croppedBitmap.width * backgroundScaleFactor).toInt() + borderWidth * 2
        val backgroundHeight = (croppedBitmap.height * backgroundScaleFactor).toInt() + borderWidth * 2
        background?.setBounds(0, 0, backgroundWidth, backgroundHeight)

        val bubbleBitmap = Bitmap.createBitmap(
            backgroundWidth,
            backgroundHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bubbleBitmap)

        background?.draw(canvas)
        val left = (backgroundWidth - croppedBitmap.width) / 2f
        val top = (backgroundHeight - croppedBitmap.height) / 2f - offsetY
        canvas.drawBitmap(croppedBitmap, left, top, null)

        return bubbleBitmap
    }

    private fun cropToSquare(originalBitmap: Bitmap, size: Int): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val cropSize = minOf(width, height)

        val xOffset = (width - cropSize) / 2
        val yOffset = (height - cropSize) / 2

        return Bitmap.createBitmap(originalBitmap, xOffset, yOffset, cropSize, cropSize)
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
                    mMap.addMarker(MarkerOptions().position(currentLatLng))

                    Log.d("Location", "Fetching photos for location: Latitude = ${location.latitude}, Longitude = ${location.longitude}")
                    fetchPhotos(location.latitude, location.longitude, 100)
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
