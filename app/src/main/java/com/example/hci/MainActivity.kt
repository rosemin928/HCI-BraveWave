package com.example.hci

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hci.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네비게이션 설정
        setBottomNavigationView()
    }

    private fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, HomeFragment()).commit()
                    true
                }
                R.id.graphFragment -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, GraphFragment()).commit()
                    true
                }
                R.id.reportFragment -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, ReportFragment()).commit()
                    true
                }
                else -> false
            }
        }
    }
}
