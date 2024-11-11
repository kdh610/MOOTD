package com.example.mootd.fragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class PhotoClusterItem(
    private val position: LatLng,
    private val title: String,
    private val snippet: String,
    private val imageUrl: String
) : ClusterItem {
    override fun getPosition(): LatLng = position
    override fun getTitle(): String = title
    override fun getSnippet(): String = snippet
    override fun getZIndex(): Float = Float.NaN
}
