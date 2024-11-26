package com.example.hci

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TrainingReportAdapter(private val reportList: List<TrainingReport>) :
    RecyclerView.Adapter<TrainingReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.trainingDate)
        val graphImageView: ImageView = itemView.findViewById(R.id.eegGraph)
        val trainingTitleTextView: TextView = itemView.findViewById(R.id.trainingTitle)
        val performanceTextView: TextView = itemView.findViewById(R.id.trainingPerformance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]
        holder.dateTextView.text = report.date
        holder.trainingTitleTextView.text = report.title
        holder.performanceTextView.text = report.performance

        // Firebase Storage 이미지 로드
        Glide.with(holder.itemView.context)
            .load(report.graphImageUrl)
            .into(holder.graphImageView)
    }

    override fun getItemCount(): Int = reportList.size
}