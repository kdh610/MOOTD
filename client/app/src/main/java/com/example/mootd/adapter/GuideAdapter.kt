package com.example.mootd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mootd.R


data class UnifiedPhotoData(
    val photoId: String?,
    val originalImageUrl: String,
    val personGuidelineUrl: String?,
    val backgroundGuidelineUrl: String?
)

class GuideAdapter(
    private var photoList: List<UnifiedPhotoData>, // UnifiedPhotoData 리스트
    private val layoutId: Int,
    private val onItemClick: (UnifiedPhotoData) -> Unit
) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return GuideViewHolder(view)
    }


    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val photoData = photoList[position]

        Glide.with(holder.itemView.context)
            .load(photoData.originalImageUrl) // 썸네일 URL
            .centerCrop()
            .into(holder.imageView)


        holder.imageView.post {
            val width = holder.imageView.width
            if (layoutId == R.layout.item_guide_image) {
                holder.imageView.layoutParams.height = (width * 4) / 3 // 4:3 비율 적용
            } else {
                holder.imageView.layoutParams.height = width // 1:1 비율 적용
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(photoData) // photoId가 없으면 originImageUrl 전달
        }
    }

    override fun getItemCount(): Int = photoList.size

    fun updateData(newPhotoList: List<UnifiedPhotoData>) {
        photoList = newPhotoList
        notifyDataSetChanged()
    }

    inner class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}