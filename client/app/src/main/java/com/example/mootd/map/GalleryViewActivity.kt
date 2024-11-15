package com.example.mootd.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mootd.R

class GalleryViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_view)

        // XML에서 ImageView를 찾아 설정
        val imageView = findViewById<ImageView>(R.id.imageView)

        // MapFragment에서 전달받은 photoUrl을 Intent로 가져옴
        val photoUrl = intent.getStringExtra("photoUrl")

        // Glide로 이미지 로드
        photoUrl?.let {
            Glide.with(this)
                .load(it)
                .into(imageView)
        }
    }
}