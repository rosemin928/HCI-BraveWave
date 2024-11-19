package com.example.hci

import android.graphics.Color
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.charts.LineChart
import java.io.BufferedReader
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
        loadCSVFromAssets()
    }

    private fun loadCSVFromAssets() {
        val entries = mutableListOf<Entry>()

        try {
            // assets 폴더에서 파일 읽기
            val inputStream = requireContext().assets.open("eeg_graph_data_stable.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

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
}