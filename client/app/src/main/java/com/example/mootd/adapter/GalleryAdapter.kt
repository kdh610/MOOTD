package com.example.mootd.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mootd.R

class GalleryAdapter(private val imageList: List<String>, private val onItemClick: (String) -> Unit) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val imageUri = imageList[position]
        Glide.with(holder.itemView.context)
            .load(imageUri)
            .override(200, 200) // 이미지 크기 조정 (최적화)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 설정
            .into(holder.imageView)

        holder.imageView.post {
            val width = holder.imageView.width
            holder.imageView.layoutParams.height = width
        }

        holder.itemView.setOnClickListener {
            onItemClick(imageUri)
        }
    }


    override fun getItemCount(): Int = imageList.size

    override fun onViewRecycled(holder: GalleryViewHolder) {
        super.onViewRecycled(holder)
        // 뷰가 재활용될 때 이미지의 크기를 강제로 정사각형으로 유지
        holder.imageView.layoutParams.height = holder.imageView.width
    }

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}