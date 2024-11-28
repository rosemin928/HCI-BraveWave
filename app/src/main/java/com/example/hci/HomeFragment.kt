package com.example.hci

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hci.databinding.FragmentHomeBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var textToSpeech: TextToSpeech
    private var isRunning = false
    private var elapsedTime = 0 // 경과 시간 (초 단위)
    private lateinit var handler: Handler
    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        handler = Handler(Looper.getMainLooper())

        // TextToSpeech 초기화
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("Language not supported")
                }
            } else {
                println("TTS initialization failed")
            }
        }

        // 버튼 리스너 설정
        binding.acrophobiaBtn.setOnClickListener {
            handleButtonClick("고소공포증 훈련 진행", "고소공포증 대응 훈련을 시작합니다.", requireContext().getColor(R.color.green))
        }

        binding.agoraphobiaBtn.setOnClickListener {
            handleButtonClick("광장공포증 훈련 진행", "광장공포증 대응 훈련을 시작합니다.", requireContext().getColor(R.color.green))
        }

        binding.claustrophobiaBtn.setOnClickListener {
            handleButtonClick("폐소공포증 훈련 진행", "폐소공포증 대응 훈련을 시작합니다.", requireContext().getColor(R.color.green))
        }

        return binding.root
    }

    private fun saveTrainingReport(programTitle: String, graphBitmap: Bitmap) {
        val database = Firebase.database.reference.child("training_reports")
        val storage = FirebaseStorage.getInstance().reference.child("graphs")

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val graphRef = storage.child("$date-${programTitle}.png")

        val baos = ByteArrayOutputStream()
        val isCompressed = graphBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        if (!isCompressed) {
            Log.e("SaveTrainingReport", "Bitmap compression failed")
            activity?.runOnUiThread {
                binding.programGuide.text = "이미지 압축에 실패했습니다."
            }
            return
        }
        val data = baos.toByteArray()

        graphRef.putBytes(data).addOnSuccessListener {
            graphRef.downloadUrl.addOnSuccessListener { uri ->
                val trainingReport = TrainingReport(
                    date = date,
                    graphImageUrl = uri.toString(),
                    title = programTitle,
                    performance = "우수"
                )
                database.push().setValue(trainingReport).addOnSuccessListener {
                    Log.d("SaveTrainingReport", "Training report saved successfully.")
                    activity?.runOnUiThread {
                        binding.programGuide.text = "훈련 보고서가 성공적으로 저장되었습니다."
                    }
                }.addOnFailureListener { e ->
                    Log.e("SaveTrainingReport", "Failed to save report to database", e)
                    activity?.runOnUiThread {
                        binding.programGuide.text = "데이터베이스 저장에 실패했습니다."
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("SaveTrainingReport", "Failed to get download URL", e)
                activity?.runOnUiThread {
                    binding.programGuide.text = "다운로드 URL 가져오기에 실패했습니다."
                }
            }
        }.addOnFailureListener { e ->
            Log.e("SaveTrainingReport", "Failed to upload image", e)
            activity?.runOnUiThread {
                binding.programGuide.text = "이미지 업로드에 실패했습니다."
            }
        }
    }

    private fun handleButtonClick(programText: String, speechText: String, color: Int) {
        speakText(speechText)
        startProgram(programText, color)

        // 서버에 훈련 시작 요청
        sendPostRequest()

        // 훈련 종료 상태 관찰
        viewModel.isTrainingFinished.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished) {
                viewModel.downloadCSVRequest.postValue(true) // CSV 다운로드 요청
            }
        }

        // 그래프 준비 상태 관찰
        viewModel.isGraphReady.observe(viewLifecycleOwner) { isReady ->
            if (isReady) {
                viewModel.chartBitmap.value?.let { bitmap ->
                    saveTrainingReport(programText, bitmap)
                }
            }
        }
    }

    private fun sendPostRequest() {
        val url = "http://192.168.1.102:5000/run-muse"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(null, ""))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    binding.programGuide.text = "프로그램 시작 실패: 네트워크 오류"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                println("Response: ${response.body?.string()}")
            }
        })
    }

    private fun speakText(text: String) {
        if (::textToSpeech.isInitialized) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun startProgram(programText: String, color: Int) {
        if (!isRunning) {
            isRunning = true
            elapsedTime = 0
            binding.programGuide.text = "$programText 00:00" // 초기 텍스트 설정
            binding.programGuide.setTextColor(color) // 색상 변경
            startTimer(programText) // 프로그램 이름 전달
        }
    }

    private fun startTimer(programText: String) {
        handler.post(object : Runnable {
            override fun run() {
                if (isRunning) {
                    elapsedTime++

                    // 시간 업데이트
                    val minutes = TimeUnit.SECONDS.toMinutes(elapsedTime.toLong())
                    val seconds = elapsedTime % 60
                    binding.programGuide.text =
                        "$programText ${String.format("%02d:%02d", minutes, seconds)}"

                    // 2분 경과 알림
                    if (elapsedTime == 60) {
                        speakText("2분이 경과했습니다. 기기를 착용해주세요.")
                    }

                    // 4분 후 종료
                    if (elapsedTime == 120) {
                        isRunning = false
                        binding.programGuide.text = "프로그램 종료: 훈련이 완료되었습니다."
                        binding.programGuide.setTextColor(requireContext().getColor(R.color.red))

                        // 훈련 종료 상태 업데이트
                        viewModel.isTrainingFinished.postValue(true)
                        return
                    }

                    // 다음 실행 예약
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}