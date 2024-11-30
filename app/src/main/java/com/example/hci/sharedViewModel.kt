package com.example.hci

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val isTrainingFinished = MutableLiveData<Boolean>(false)
    val isGraphReady = MutableLiveData<Boolean>(true)
    val chartBitmap = MutableLiveData<Bitmap?>().apply {
        value = null // 초기 상태 명시
    }
    val downloadCSVRequest = MutableLiveData<Boolean>(false)
}