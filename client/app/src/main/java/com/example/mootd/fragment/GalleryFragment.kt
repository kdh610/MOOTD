package com.example.mootd.fragment

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mootd.R
import com.example.mootd.adapter.GalleryAdapter
import com.example.mootd.databinding.FragmentGalleryBinding


class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val imageList = mutableListOf<String>()
    private lateinit var galleryAdapter: GalleryAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        if (hasStoragePermission()) {
            loadGalleryImages()
        } else {
            requestStoragePermission()
        }
        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }


    }
    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        requestPermissions(permissions, REQUEST_CODE_READ_EXTERNAL_STORAGE)
//        ActivityCompat.requestPermissions(
//            requireActivity(),
//            permissions,
//            REQUEST_CODE_READ_EXTERNAL_STORAGE
//        )
    }

    private fun setupRecyclerView() {
        galleryAdapter = GalleryAdapter(imageList) { imagePath ->
            // 클릭한 이미지 경로를 PictureDetailFragment로 전달하기 위해 Bundle 사용
            val bundle = Bundle().apply {
                putString("imagePath", imagePath)
            }
            findNavController().navigate(R.id.action_galleryFragment_to_pictureDetailFragment, bundle)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = galleryAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadGalleryThumbnails() {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT 50" // 페이징 적용 (최대 50개)
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val imageId = it.getLong(idIndex)
                val thumbnailUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageId
                ).toString()
                imageList.add(thumbnailUri)
            }
            galleryAdapter.notifyDataSetChanged()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                loadGalleryImages()
            } else {
                Toast.makeText(context, "저장소 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadGalleryImages() {
        imageList.clear()

        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                val imagePath = it.getString(dataIndex)
                imageList.add(imagePath)
            }
            galleryAdapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()

        // RecyclerView가 화면에 표시될 때 강제로 레이아웃을 갱신하여 비율 유지
        binding.recyclerView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    // GlobalLayoutListener를 멤버 변수로 선언
    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        binding.recyclerView.invalidate()
        binding.recyclerView.requestLayout()
    }

    override fun onPause() {
        super.onPause()

        // 뷰가 일시정지될 때 리스너 해제
        binding.recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 100
    }
}