package com.example.hci

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
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var textToSpeech: TextToSpeech
    private var isRunning = false
    private var elapsedTime = 0 // 경과 시간 (초 단위)
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

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
            handleButtonClick("고소공포증 훈련 진행중", "고소공포증 대응 훈련을 시작합니다.", requireContext().getColor(R.color.green))
        }

        binding.agoraphobiaBtn.setOnClickListener {
            handleButtonClick("광장공포증 훈련 진행중", "광장공포증 대응 훈련을 시작합니다.", requireContext().getColor(R.color.green))
        }

        binding.claustrophobiaBtn.setOnClickListener {
            handleButtonClick("폐소공포증 훈련 진행중", "폐소공포증 대응 훈련을 시작합니다.", requireContext().getColor(R.color.green))
        }

        return binding.root
    }

    private fun handleButtonClick(programText: String, speechText: String, color: Int) {
        sendPostRequest()
        speakText(speechText)
        startProgram(programText, color)
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

    private fun startProgram(text: String, color: Int) {
        if (!isRunning) {
            isRunning = true
            elapsedTime = 0
            binding.programGuide.text = "$text 00:00"
            binding.programGuide.setTextColor(color) // 색상 변경
            startTimer()
        }
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (isRunning) {
                    elapsedTime++

                    // 시간 계산
                    val minutes = TimeUnit.SECONDS.toMinutes(elapsedTime.toLong())
                    val seconds = elapsedTime % 60
                    binding.programGuide.text =
                        "${binding.programGuide.text} ${String.format("%02d:%02d", minutes, seconds)}"

                    // 2분(120초)에 음성 출력
                    if (elapsedTime == 120) {
                        speakText("2분이 경과했습니다. 기기를 착용해주세요.")
                    }

                    // 4분(240초)에 프로그램 종료
                    if (elapsedTime == 240) {
                        isRunning = false
                        binding.programGuide.text = "프로그램 종료: 훈련이 완료되었습니다."
                        binding.programGuide.setTextColor(requireContext().getColor(R.color.red)) // 종료 시 색상 변경
                        return // 타이머 종료
                    }

                    // 다음 실행 예약
                    handler.postDelayed(this, 1000) // 1초마다 실행
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