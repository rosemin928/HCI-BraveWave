package com.example.hci

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.hci.databinding.ActivityMainBinding
import com.example.hci.databinding.FragmentHomeBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val button1 = binding.button1
        Log.d("button111", button1.text.toString())
        button1.setOnClickListener {
            Log.d("button1", button1.toString())
            sendPostRequest()
        }
        return binding.root
    }

    private fun sendPostRequest() {
        Log.d("sss", "sss")
        val url = "http://172.20.19.128:5000/run-muse"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(null, ""))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                println("Response: ${response.body?.string()}")
            }
        })
    }
}