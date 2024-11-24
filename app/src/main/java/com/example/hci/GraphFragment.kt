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
import android.widget.Toast
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_graph, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // assets 폴더에서 CSV 파일 읽어와 그래프 표시
        //loadCSVFromAssets()
        downloadCSV("http://192.168.1.102:5000/download-csv")
    }

    private fun loadCSVFromAssets() {
        val entries = mutableListOf<Entry>()

        try {
            // assets 폴더에서 파일 읽기
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "real_eeg_data.csv")
            val reader = BufferedReader(FileReader(file))
           // val inputStream = requireContext().assets.open("eeg_graph_data_stable.csv")
           // val reader = BufferedReader(InputStreamReader(inputStream))

            var isFirstRow = true
            reader.forEachLine { line ->
                if (isFirstRow) {
                    isFirstRow = false // 첫 번째 행은 헤더
                    return@forEachLine
                }
                val columns = line.split(",") // CSV의 각 열 분리
                val frequency = columns[0].toFloat() // 첫 번째 열: Frequency
                val magnitude = columns[1].toFloat() // 두 번째 열: Magnitude
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

            // LineChart 스타일 설정
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
            lineChart.invalidate() // 그래프 새로고침

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadCSV(url: String) {
        // 코루틴을 사용하여 비동기 다운로드
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // OkHttp 클라이언트 생성
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()

                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    // 파일을 /sdcard/Download/real_eeg_data.csv에 저장
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
                // 다운로드 받은 파일을 저장할 경로
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

                // UI 업데이트: 파일 다운로드 성공
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "CSV 파일 다운로드 완료!", Toast.LENGTH_LONG).show()
                }
                loadCSVFromAssets()

            } catch (e: IOException) {
                Log.e("Error", "Error saving file: ", e)
            }
        }
    }
}