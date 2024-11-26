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

        // 첫 화면으로 HomeFragment 설정
        if (savedInstanceState == null) { // Activity가 처음 생성된 경우에만 실행
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, HomeFragment())
                .commit()

            // BottomNavigationView에서 홈을 기본 선택으로 설정
            binding.bottomNavigationView.selectedItemId = R.id.homeFragment
        }

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
