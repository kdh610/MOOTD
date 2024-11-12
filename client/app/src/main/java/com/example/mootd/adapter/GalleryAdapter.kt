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

class GalleryAdapter : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private var imageListWithId: List<Pair<String?, String>>? = null
    private var imageListWithoutId: List<String>? = null
    private val onItemClickWithId: ((String?, String) -> Unit)?
    private val onItemClickWithoutId: ((String) -> Unit)?

    constructor(
        imageList: List<Pair<String?, String>>,
        onItemClick: (String?, String) -> Unit
    ) {
        this.imageListWithId = imageList
        this.onItemClickWithId = onItemClick
        this.onItemClickWithoutId = null
    }

    // 생성자 오버로딩: imageUrl만 넘기는 경우
    constructor(
        imageList: List<String>,
        onItemClick: (String) -> Unit
    ) {
        this.imageListWithoutId = imageList
        this.onItemClickWithoutId = onItemClick
        this.onItemClickWithId = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val imageUri = imageListWithId?.get(position)?.second ?: imageListWithoutId?.get(position)

        Glide.with(holder.itemView.context)
            .load(imageUri)
            .override(200, 200)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 설정
            .into(holder.imageView)

        holder.imageView.post {
            val width = holder.imageView.width
            holder.imageView.layoutParams.height = width
        }

        holder.itemView.setOnClickListener {
            imageListWithId?.get(position)?.let { (imageId, imageUri) ->
                onItemClickWithId?.invoke(imageId, imageUri)
            } ?: imageListWithoutId?.get(position)?.let { imageUri ->
                onItemClickWithoutId?.invoke(imageUri)
            }
        }
    }


    override fun getItemCount(): Int = imageListWithId?.size ?: imageListWithoutId?.size ?: 0


    override fun onViewRecycled(holder: GalleryViewHolder) {
        super.onViewRecycled(holder)
        // 뷰가 재활용될 때 이미지의 크기를 강제로 정사각형으로 유지
        holder.imageView.layoutParams.height = holder.imageView.width
    }

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}