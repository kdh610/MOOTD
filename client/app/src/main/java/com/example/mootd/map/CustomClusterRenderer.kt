package com.example.mootd.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.example.mootd.fragment.PhotoClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class CustomClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<PhotoClusterItem>
) : DefaultClusterRenderer<PhotoClusterItem>(context, map, clusterManager) {

    // 클러스터 아이템이 렌더링되기 전, 아이템에 대한 커스텀 마커 설정
    override fun onBeforeClusterItemRendered(item: PhotoClusterItem, markerOptions: MarkerOptions) {
        // 클러스터 아이템이 개별 마커로 렌더링될 때만 이미지 설정
        markerOptions.zIndex(0f)
        item.imageBitmap?.let {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(it))
        }
    }

    // 클러스터 자체가 렌더링될 때의 처리
    override fun onBeforeClusterRendered(cluster: Cluster<PhotoClusterItem>, markerOptions: MarkerOptions) {
        val representativeItem = cluster.items.firstOrNull()

        representativeItem?.imageBitmap?.let { bitmap ->
            val clusterIcon = createClusterBitmap(bitmap, cluster.size)
            markerOptions.zIndex(1f)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(clusterIcon))
        }
    }

    // 클러스터 아이콘 생성 함수
    private fun createClusterBitmap(representativeBitmap: Bitmap, clusterSize: Int): Bitmap {
        val width = 200 // 아이콘 너비
        val height = 200 // 아이콘 높이
        val clusterBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(clusterBitmap)

        // 대표 이미지를 클러스터 아이콘 크기에 맞게 조정하여 그리기
        val scaledBitmap = Bitmap.createScaledBitmap(representativeBitmap, width, height, false)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, null)

        // 텍스트 스타일 설정
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // 텍스트 위치 계산 및 그리기
        val text = clusterSize.toString()
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val x = width - bounds.width() - 10f // 우측 상단
        val y = bounds.height() + 10f
        canvas.drawText(text, x, y, paint)

        return clusterBitmap
    }
}