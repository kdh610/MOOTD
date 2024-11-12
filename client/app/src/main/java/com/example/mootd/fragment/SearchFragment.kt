package com.example.mootd.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mootd.R
import com.example.mootd.adapter.GalleryAdapter
import com.example.mootd.databinding.FragmentSearchBinding
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mootd.api.RetrofitInstance
import com.example.mootd.api.SearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var galleryAdapter: GalleryAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.etSearchQuery.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchQuery, InputMethodManager.SHOW_IMPLICIT)

        binding.recyclerViewSearchResults.layoutManager = GridLayoutManager(requireContext(), 3)

        binding.btnSearch.setOnClickListener {
            performSearch()
        }


        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // RecyclerView 설정
        binding.recyclerViewSearchResults.layoutManager = GridLayoutManager(context, 3)
    }

    private fun performSearch() {
        val query = binding.etSearchQuery.text.toString()
        if (query.isNotEmpty()) {
            searchPhotos(query)
            hideKeyboard()
        } else {
            Toast.makeText(context, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearchQuery.windowToken, 0)
    }

    private fun searchPhotos(query: String) {
        RetrofitInstance.guideSearchService.searchPhotosByTag(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val photoList = response.body()?.data?.map {
                        Pair(it.id, it.originImageUrl) // imageId와 originImageUrl을 Pair로 저장
                    } ?: emptyList()

                    if (photoList.isNotEmpty()) {
                        // 검색 결과가 있을 때
                        galleryAdapter = GalleryAdapter(photoList) { photoId, imageUrl ->
                            val bundle = Bundle().apply {
                                putString("photoId", photoId)
                                putString("imageUrl", imageUrl)
                            }
                            findNavController().navigate(R.id.action_searchFragment_to_guideDetailFragment, bundle)
                        }
                        binding.recyclerViewSearchResults.adapter = galleryAdapter
                        binding.recyclerViewSearchResults.visibility = View.VISIBLE
                        binding.emptyTextView.visibility = View.GONE
                    } else {
                        // 검색 결과가 없을 때
                        binding.recyclerViewSearchResults.visibility = View.GONE
                        binding.emptyTextView.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(context, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}