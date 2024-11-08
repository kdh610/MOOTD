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
    private var imageList: List<String>,
    private val layoutId: Int,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return GuideViewHolder(view)
    }


    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val imageUri = imageList[position]
        Glide.with(holder.itemView.context)
            .load(imageUri)
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
            onItemClick(imageUri)
        }
    }

    fun updateData(newImageList: List<String>) {
        imageList = newImageList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = imageList.size

    inner class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}