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
            .load(photoData.originalImageUrl)
            .centerCrop()
            .into(holder.imageView)

        val screenWidth = holder.itemView.context.resources.displayMetrics.widthPixels
        val targetWidth = (screenWidth * 0.35).toInt()


        holder.imageView.post {
            if (layoutId == R.layout.item_guide_image) {
//                holder.imageView.layoutParams.width = targetWidth
//                holder.imageView.layoutParams.height = (targetWidth * 4) / 3 // 4:3 비율 적용
                val recyclerViewHeight = (holder.itemView.parent as RecyclerView).height
                holder.imageView.layoutParams.height = recyclerViewHeight
                holder.imageView.layoutParams.width = (recyclerViewHeight * 3) / 4 // 4:3 비율 설정
            } else {
                val width = holder.imageView.width
                holder.imageView.layoutParams.height = width // 1:1 비율 적용
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(photoData)
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