package com.example.mootd.fragment

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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mootd.api.RetrofitInstance
import com.example.mootd.api.SearchResponse
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mootd.adapter.GuideAdapter
import com.example.mootd.adapter.SearchHistoryAdapter
import com.example.mootd.adapter.UnifiedPhotoData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private var searchHistory: MutableList<String> = mutableListOf()

    private lateinit var guideAdapter: GuideAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSearchHistory()

        binding.etSearchQuery.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchQuery, InputMethodManager.SHOW_IMPLICIT)

        binding.recyclerViewSearchResults.layoutManager = GridLayoutManager(requireContext(), 3)


        searchHistoryAdapter = SearchHistoryAdapter(searchHistory,
            onItemClick = { query -> setSearchQueryAndPerformSearch(query) },
            onDeleteClick = { query -> deleteSearchHistory(query) }
        )
        binding.recyclerViewSearchHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSearchHistory.adapter = searchHistoryAdapter

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

        binding.btnRetry.setOnClickListener { performSearch() }
    }

    private fun performSearch() {
        val query = binding.etSearchQuery.text.toString()
        if (query.isNotEmpty()) {
//            if (!searchHistory.contains(query)) {
            searchHistory.remove(query)
            searchHistory.add(0, query)
            saveSearchHistory()  // 검색 기록 저장
            searchHistoryAdapter.notifyDataSetChanged() // 어댑터 갱신
//            }
            searchPhotos(query)
            hideKeyboard()
        } else {
            Toast.makeText(context, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSearchQueryAndPerformSearch(query: String) {
        binding.etSearchQuery.setText(query)
        performSearch()
    }

    private fun deleteSearchHistory(query: String) {
        searchHistory.remove(query)
        saveSearchHistory()  // 삭제 후 검색 기록 저장
        searchHistoryAdapter.notifyDataSetChanged()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearchQuery.windowToken, 0)
    }

    private fun searchPhotos(query: String) {
        binding.errorLayout.visibility = View.GONE
        binding.recyclerViewSearchHistory.visibility = View.GONE
        RetrofitInstance.guideSearchService.searchPhotosByTag(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val photoList = response.body()?.data?.map { photo ->
                        UnifiedPhotoData(
                            photoId = photo.id,
                            originalImageUrl = photo.maskImageUrl,
                            personGuidelineUrl = photo.personGuidelineUrl,
                            backgroundGuidelineUrl = photo.backgroundGuidelineUrl
                        )
                    } ?: emptyList()

                    if (photoList.isNotEmpty()) {
                        // 검색 결과가 있을 때
                        guideAdapter = GuideAdapter(photoList, R.layout.item_gallery_image) { photoData ->
                            val bundle = Bundle().apply {
                                putString("photoId", photoData.photoId)
                                putString("originalImageUrl", photoData.originalImageUrl)
                                putString("personGuidelineUrl", photoData.personGuidelineUrl)
                                putString("backgroundGuidelineUrl", photoData.backgroundGuidelineUrl)
                            }
                            findNavController().navigate(R.id.action_searchFragment_to_guideDetailFragment, bundle)
                        }
                        binding.recyclerViewSearchResults.adapter = guideAdapter
                        binding.recyclerViewSearchResults.visibility = View.VISIBLE
                        binding.emptyTextView.visibility = View.GONE
                    } else {
                        // 검색 결과가 없을 때
                        binding.recyclerViewSearchResults.visibility = View.GONE
                        binding.emptyTextView.visibility = View.VISIBLE
                    }
                } else {
                    showNetworkErrorMessage()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                showNetworkErrorMessage()
            }
        })
    }

    private fun saveSearchHistory() {
        val sharedPreferences = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(searchHistory) // 리스트를 JSON 문자열로 변환
        editor.putString("search_history", json)
        editor.apply()
    }

    private fun loadSearchHistory() {
        val sharedPreferences = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("search_history", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            searchHistory = Gson().fromJson(json, type) // JSON 문자열을 리스트로 변환
        }
    }

    private fun showNetworkErrorMessage() {
        binding.errorLayout.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}