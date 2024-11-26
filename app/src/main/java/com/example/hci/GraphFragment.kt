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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
    private lateinit var lineChartStable: LineChart
    private lateinit var lineChartFear: LineChart
    private lateinit var barChart: BarChart

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        return inflater.inflate(R.layout.fragment_graph, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // LineChart 및 BarChart 초기화
        lineChartStable = view.findViewById(R.id.lineChart1)
        lineChartFear = view.findViewById(R.id.lineChart2)
        barChart = view.findViewById(R.id.barChart)

        // CSV 다운로드 요청 관찰
        viewModel.downloadCSVRequest.observe(viewLifecycleOwner) { shouldDownload ->
            if (shouldDownload) {
                downloadCSV("http://192.168.1.102:5000/download-csv1", "eeg_graph_data_stable.csv")
                downloadCSV("http://192.168.1.102:5000/download-csv2", "eeg_graph_data_fear.csv")
                viewModel.downloadCSVRequest.postValue(false) // 요청 초기화
            }
        }

        // CSV 다운로드 및 그래프 준비 완료
        viewModel.isGraphReady.observe(viewLifecycleOwner) { isReady ->
            if (isReady) {
                val stableData = CSVUtils.readCSV(requireContext(), "eeg_graph_data_stable.csv")
                val fearData = CSVUtils.readCSV(requireContext(), "eeg_graph_data_fear.csv")

                val stableMeans = calculateBandMeans(stableData)
                val fearMeans = calculateBandMeans(fearData)

                setupBarChart(barChart, stableMeans, fearMeans)
            }
        }
    }

    private fun downloadCSV(url: String, name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    saveFile(response.body?.byteStream(), name)
                } else {
                    Log.e("Download failed", "Download failed: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("Error", "Error during download: ", e)
            }
        }
    }

    private suspend fun saveFile(inputStream: InputStream?, name: String) {
        inputStream?.let {
            try {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name)
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
                    when (name) {
                        "eeg_graph_data_stable.csv" -> loadCSVToLineChart(file, lineChartStable)
                        "eeg_graph_data_fear.csv" -> loadCSVToLineChart(file, lineChartFear)
                    }

                    // 그래프 준비 완료 알림
                    viewModel.isGraphReady.postValue(true)
                }
            } catch (e: IOException) {
                Log.e("Error", "Error saving file: ", e)
            }
        }
    }

    private fun loadCSVToLineChart(file: File, lineChart: LineChart) {
        val entries = mutableListOf<Entry>()
        try {
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
                circleRadius = 1f
            }

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
            lineChart.invalidate() // 새로고침
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateBandMeans(data: List<Map<String, String>>): Map<String, Float> {
        val bandSums = mutableMapOf<String, Float>()
        val bandCounts = mutableMapOf<String, Int>()

        for (row in data) {
            val band = row["Band"] ?: continue
            val magnitude = row["Magnitude"]?.toFloatOrNull() ?: continue

            bandSums[band] = bandSums.getOrDefault(band, 0f) + magnitude
            bandCounts[band] = bandCounts.getOrDefault(band, 0) + 1
        }

        return bandSums.mapValues { (band, sum) ->
            val count = bandCounts[band] ?: 1
            sum / count
        }
    }

    private fun setupBarChart(barChart: BarChart, stableMeans: Map<String, Float>, fearMeans: Map<String, Float>) {
        val labels = listOf("Theta", "Alpha", "Beta")
        val stableValues = labels.map { stableMeans[it] ?: 0f }
        val fearValues = labels.map { fearMeans[it] ?: 0f }

        val stableEntries = stableValues.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        val fearEntries = fearValues.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }

        val stableDataSet = BarDataSet(stableEntries, "Stable").apply {
            color = Color.BLUE
            setDrawValues(false)
        }
        val fearDataSet = BarDataSet(fearEntries, "Fear").apply {
            color = Color.RED
            setDrawValues(false)
        }

        val barData = BarData(stableDataSet, fearDataSet).apply {
            barWidth = 0.3f
        }

        barChart.data = barData
        barChart.groupBars(0f, 0.2f, 0.005f)
        barChart.invalidate()

        val xAxis = barChart.xAxis
        xAxis.axisMinimum = -0.3f
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
    }
}