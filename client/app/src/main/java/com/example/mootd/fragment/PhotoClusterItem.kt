package com.example.mootd.fragment
import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class PhotoClusterItem(
    private val position: LatLng,
    private val title: String,
    private val snippet: String,
    private val imageUrl: String,
    private val photoId: String,
    val imageBitmap: Bitmap? = null
) : ClusterItem {

    override fun getPosition(): LatLng = position
    override fun getTitle(): String = title
    override fun getSnippet(): String = snippet
    override fun getZIndex(): Float = Float.NaN
    fun getImageUrl(): String = imageUrl
    fun getImageId(): String? = photoId
}
