package com.example.mootd.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GuideOverlayViewModel : ViewModel() {

    private val _originalImageUrl = MutableLiveData<String?>()
    val originalImageUrl: LiveData<String?> get() = _originalImageUrl

    private val _personGuideImageUrl = MutableLiveData<String?>()
    val personGuideImageUrl: LiveData<String?> get() = _personGuideImageUrl

    private val _backgroundGuideImageUrl = MutableLiveData<String?>()
    val backgroundGuideImageUrl: LiveData<String?> get() = _backgroundGuideImageUrl

    private val _showOriginal = MutableLiveData<Boolean>(true)
    val showOriginal: LiveData<Boolean> get() = _showOriginal

    private val _showPerson = MutableLiveData<Boolean>(false)
    val showPerson: LiveData<Boolean> get() = _showPerson

    private val _showBackground = MutableLiveData<Boolean>(false)
    val showBackground: LiveData<Boolean> get() = _showBackground

    fun setGuideImages(originalUrl: String?, personUrl: String?, backgroundUrl: String?) {
        _originalImageUrl.value = originalUrl
        _personGuideImageUrl.value = personUrl
        _backgroundGuideImageUrl.value = backgroundUrl
        _showOriginal.value = true
        _showPerson.value = false
        _showBackground.value = false
    }

    fun clearGuideImages() {
        _originalImageUrl.value = null
        _personGuideImageUrl.value = null
        _backgroundGuideImageUrl.value = null
    }

    fun toggleShowOriginal() {
        _showOriginal.value = _showOriginal.value?.not()
    }

    fun toggleShowPerson() {
        _showPerson.value = _showPerson.value?.not()
    }

    fun toggleShowBackground() {
        _showBackground.value = _showBackground.value?.not()
    }
}
