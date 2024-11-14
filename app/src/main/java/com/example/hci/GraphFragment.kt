package com.example.hci

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.math.BigDecimal

class GraphFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_graph, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 임의의 데이터 엔트리 생성
        val entries = listOf(
            Entry(1f, 5f),
            Entry(2f, 8f),
            Entry(3f, 12f),
            Entry(4f, 5f),
            Entry(5f, 10f),
            Entry(6f, 12f)
        )

        // 목표 몸무게 설정 (임의의 값)
        val targetWeight = BigDecimal(10)

        // LineChart 업데이트
        updateLineChartWithWeight(entries, targetWeight)
    }

    // 그래프 업데이트 함수
    private fun updateLineChartWithWeight(entries: List<Entry>, targetWeight: BigDecimal?) {
        val lineChart = requireView().findViewById<LineChart>(R.id.lineChart)

        // entries가 비어있으면 기본값으로 처리
        val mutableEntries = if (entries.isEmpty()) mutableListOf(Entry(0f, 0f)) else entries.toMutableList()

        val dataSet = LineDataSet(mutableEntries, "뇌파").apply {
            color = Color.parseColor("#FFBF00")
            setCircleColor(Color.parseColor("#FFBF00"))
            circleRadius = 5f
            lineWidth = 3f
            mode = LineDataSet.Mode.LINEAR
        }

        // x축 설정
        lineChart.xAxis.apply {
            axisMinimum = 0f
            axisMaximum = 6f
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setLabelCount(7, true)
            valueFormatter = IndexAxisValueFormatter(arrayOf("1", "2", "3", "4", "5", "6", "7"))
        }

        // y축 설정 및 목표 몸무게 라인 추가
        lineChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 15f
            removeAllLimitLines()

            targetWeight?.let { weight ->
                val limitLine = LimitLine(weight.toFloat(), "기준 뇌파").apply {
                    lineColor = Color.parseColor("#B40404")
                    lineWidth = 2f
                }
                addLimitLine(limitLine)
            }
        }

        // 오른쪽 y축 숨기기
        lineChart.axisRight.isEnabled = false

        // LineData 설정
        lineChart.data = LineData(dataSet)

        // 그래프 업데이트
        lineChart.invalidate()
    }
}
