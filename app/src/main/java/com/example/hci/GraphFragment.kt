package com.example.hci

import android.graphics.Color
import android.nfc.Tag
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.charts.LineChart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class GraphFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    private lateinit var lineChart: LineChart

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        return inflater.inflate(R.layout.fragment_graph, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lineChart = view.findViewById(R.id.lineChart)

        // ViewModel의 CSV 다운로드 요청 상태를 관찰
        viewModel.downloadCSVRequest.observe(viewLifecycleOwner) { shouldDownload ->
            if (shouldDownload) {
                // CSV 다운로드 실행
                downloadCSV("http://192.168.1.102:5000/download-csv")
                viewModel.downloadCSVRequest.postValue(false) // 요청 초기화
            }
        }

        // ViewTreeObserver를 사용하여 LineChart 초기화 후 Bitmap 생성
        lineChart.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // LineChart가 초기화되었는지 확인
                if (lineChart.width > 0 && lineChart.height > 0) {
                    // LineChart의 Bitmap을 ViewModel에 저장
                    viewModel.chartBitmap.postValue(lineChart.chartBitmap)

                    // 리스너 제거
                    lineChart.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // 그래프 준비 완료 상태를 ViewModel에 알림
                    viewModel.isGraphReady.postValue(true)
                }
            }
        })
    }


    private fun loadCSVFromAssets() {
        val entries = mutableListOf<Entry>()

        try {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "real_eeg_data.csv")
            val reader = BufferedReader(FileReader(file))

            var isFirstRow = true
            reader.forEachLine { line ->
                if (isFirstRow) {
                    isFirstRow = false // 첫 번째 행은 헤더
                    return@forEachLine
                }
                val columns = line.split(",")
                val frequency = columns[0].toFloat()
                val magnitude = columns[1].toFloat()
                entries.add(Entry(frequency, magnitude))
            }
            reader.close()

            // LineChart 데이터 설정
            val dataSet = LineDataSet(entries, "EEG Data").apply {
                color = Color.BLUE
                valueTextColor = Color.BLACK
                lineWidth = 2f
                setCircleColor(Color.RED)
                circleRadius = 4f
            }

            val lineChart = requireView().findViewById<LineChart>(R.id.lineChart)
            lineChart.data = LineData(dataSet)

            lineChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }
            lineChart.axisLeft.apply {
                setDrawGridLines(false)
            }
            lineChart.axisRight.isEnabled = false
            lineChart.description.isEnabled = false
            lineChart.invalidate() // 새로고침

            // 그래프 준비 상태 업데이트
            viewModel.isGraphReady.postValue(true)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadCSV(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    saveFile(response.body?.byteStream())
                } else {
                    Log.e("Download failed", "Download failed: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("Error", "Error during download: ", e)
            }
        }
    }

    private suspend fun saveFile(inputStream: InputStream?) {
        inputStream?.let {
            try {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "real_eeg_data.csv")
                val outputStream = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var length: Int
                while (it.read(buffer).also { length = it } != -1) {
                    outputStream.write(buffer, 0, length)
                }

                outputStream.flush()
                outputStream.close()
                it.close()

                withContext(Dispatchers.Main) {
                    loadCSVFromAssets() // 그래프 초기화
                }
            } catch (e: IOException) {
                Log.e("Error", "Error saving file: ", e)
            }
        }
    }

}