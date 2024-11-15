package com.example.mootd.utils

import android.view.View
import android.widget.TextView

object MessageUtils {
    fun showNetworkErrorMessage(errorMessageView: TextView, retryButton: View) {
        errorMessageView.text = "인터넷 연결이 불안정합니다"
        errorMessageView.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
    }

    fun showNullErrorMessage(messageView: TextView) {
        messageView.text = "사용한 가이드라인이 없습니다."
        messageView.visibility = View.VISIBLE
    }

}