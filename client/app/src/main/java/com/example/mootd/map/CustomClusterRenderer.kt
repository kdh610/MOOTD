package com.example.mootd.map

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mootd.R
import com.example.mootd.adapter.GalleryAdapter
import com.example.mootd.fragment.PhotoClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class CustomClusterRenderer(
    private val fragment: Fragment,
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<PhotoClusterItem>
) : DefaultClusterRenderer<PhotoClusterItem>(context, map, clusterManager) {

    init {
        clusterManager.setOnClusterClickListener { cluster ->
            onClusterClicked(cluster)
            true
        }

    }

    // 클러스터가 클릭되었을 때 호출되는 함수
    private fun onClusterClicked(cluster: Cluster<PhotoClusterItem>) {
        val photoList = cluster.items.map {
            Pair(it.title, it.getOriginalImageUrl()) // 클러스터 내 각 항목의 ID와 URL로 리스트 생성
        }

        Log.d("ClusterClicked", "Photo List: $photoList")
        if (photoList.isNotEmpty()) {
            // GalleryAdapter 설정 및 Fragment로 이동
            val galleryAdapter = GalleryAdapter(photoList) { photoId, imageUrl ->

                val bundle = Bundle().apply {
                    putString("photoId", photoId)
                    putString("imageUrl", imageUrl)
                }
                fragment.findNavController().navigate(R.id.action_mapFragment_to_guideDetailFragment, bundle)
            }
        }
    }

    // 클러스터 아이템이 렌더링되기 전, 아이템에 대한 커스텀 마커 설정
    override fun onBeforeClusterItemRendered(item: PhotoClusterItem, markerOptions: MarkerOptions) {

        // 클러스터 아이템이 개별 마커로 렌더링될 때만 이미지 설정
        item.imageBitmap?.let {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(it))
        }
    }

    override fun onBeforeClusterRendered(cluster: Cluster<PhotoClusterItem>, markerOptions: MarkerOptions) {
        val representativeItem = cluster.items.firstOrNull()
        if (representativeItem?.imageBitmap != null) {
            val clusterIcon = createClusterBitmap(representativeItem.imageBitmap!!, cluster.size)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(clusterIcon))
            markerOptions.zIndex(1f) // 클러스터가 위에 표시되도록 설정
        }
    }

    private fun createClusterBitmap(representativeBitmap: Bitmap, clusterSize: Int): Bitmap {
        val width = 200 // 아이콘 너비
        val height = 200 // 아이콘 높이
        val clusterBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(clusterBitmap)

        // 대표 이미지를 클러스터 아이콘 크기에 맞게 조정하여 그리기
        val scaledBitmap = Bitmap.createScaledBitmap(representativeBitmap, width, height, false)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, null)

        // 텍스트 스타일 설정 (기본 텍스트 크기)
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE // 텍스트 색상 하얗게 설정
            textSize = if (clusterSize < 10) 36f else 28f // 10 이상이면 텍스트 크기 줄이기
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // 원형 배경을 그리기 위한 Paint 객체
        val backgroundPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#306B50")
            isAntiAlias = true
        }

        // 텍스트 계산 및 배경 원 그리기
        val text = clusterSize.toString()
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        // 원의 크기 및 위치 계산
        val radius = bounds.height() + 20f // 원의 반지름
        val circleX = width - radius - 5f // 우측 상단 위치
        val circleY = radius + 10f // 상단 위치

        // 원 그리기
        canvas.drawCircle(circleX, circleY, radius, backgroundPaint)

        // 텍스트 위치를 원 중앙에 맞게 조정
        val textX = circleX - bounds.width() / 2f // 원의 중앙에 맞게 텍스트 X 위치 조정
        val textY = circleY + (bounds.height() / 2f) // 원의 중앙에 맞게 텍스트 Y 위치 조정

        // 텍스트 그리기
        canvas.drawText(text, textX, textY, paint)

        return clusterBitmap
    }

}