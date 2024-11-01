package com.example.mootd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mootd.R

class GuideAdapter(
    private val imageList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guide_image, parent, false)
        return GuideViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val imageUri = imageList[position]
        Glide.with(holder.itemView.context)
            .load(imageUri)
            .centerCrop()
            .override(200, 200)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
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

    inner class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}