package com.example.hci

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrainingReportAdapter(private val reports: List<TrainingReport>) :
    RecyclerView.Adapter<TrainingReportAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.tvTrainingDate)
        val graphImageView: ImageView = view.findViewById(R.id.ivGraph)
        val totalTimeTextView: TextView = view.findViewById(R.id.tvTrainingTime)
        val performanceTextView: TextView = view.findViewById(R.id.tvTrainingPerformance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        holder.dateTextView.text = report.date
        holder.graphImageView.setImageResource(report.graphImageResId)
        holder.totalTimeTextView.text = "총 훈련 시간: ${report.totalTime}"
        holder.performanceTextView.text = "훈련 성과: ${report.performance}"
    }

    override fun getItemCount() = reports.size
}
