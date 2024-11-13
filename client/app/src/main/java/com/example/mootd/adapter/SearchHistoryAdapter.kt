package com.example.mootd.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mootd.R

class SearchHistoryAdapter(
    private var searchHistory: MutableList<String>,
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_history, parent, false)
        return SearchHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        val query = searchHistory[position]
        holder.bind(query)
    }

    override fun getItemCount(): Int = searchHistory.size

    fun updateSearchHistory(newSearchHistory: MutableList<String>) {
        searchHistory = newSearchHistory
        notifyDataSetChanged()
    }

    inner class SearchHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSearchQuery: TextView = itemView.findViewById(R.id.textHistory)
        private val btnDeleteHistory: ImageButton = itemView.findViewById(R.id.btnDeleteHistory)

        fun bind(query: String) {
            tvSearchQuery.text = query

            // 검색 기록 클릭 이벤트
            itemView.setOnClickListener {
                onItemClick(query)
            }

            // 삭제 버튼 클릭 이벤트
            btnDeleteHistory.setOnClickListener {
                onDeleteClick(query)
            }
        }
    }
}