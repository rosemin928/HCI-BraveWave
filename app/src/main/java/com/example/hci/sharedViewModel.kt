package com.example.hci

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val isTrainingFinished = MutableLiveData<Boolean>(false)
    val isGraphReady = MutableLiveData<Boolean>(true)
    val chartBitmap = MutableLiveData<Bitmap?>()
    val downloadCSVRequest = MutableLiveData<Boolean>(false) // CSV 다운로드 요청 상태
}