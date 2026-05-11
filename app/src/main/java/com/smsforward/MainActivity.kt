package com.smsforward

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smsforward.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pref: PreferenceManager

    companion object {
        private const val REQUEST_PERMISSIONS = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = PreferenceManager(this)
        loadSettings()
        setupListeners()
        checkPermissions()
    }

    private fun loadSettings() {
        binding.etTargetPhone.setText(pref.targetPhoneNumber)
        binding.etKeywords.setText(pref.filterKeywords)
        updateServiceButton(pref.serviceEnabled)
    }

    private fun setupListeners() {
        binding.btnToggleService.setOnClickListener {
            if (pref.serviceEnabled) {
                stopService()
            } else {
                saveSettings()
                if (pref.targetPhoneNumber.isEmpty()) {
                    toast("와이프 전화번호를 먼저 입력하세요")
                    return@setOnClickListener
                }
                startService()
            }
        }

        binding.btnSave.setOnClickListener {
            saveSettings()
            toast("설정 저장 완료")
        }

        binding.btnBattery.setOnClickListener {
            val pm = getSystemService(PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
            } else {
                toast("이미 배터리 최적화 제외 설정됨 ✅")
            }
        }
    }

    private fun saveSettings() {
        pref.targetPhoneNumber = binding.etTargetPhone.text.toString().trim()
        pref.filterKeywords = binding.etKeywords.text.toString().trim()
    }

    private fun startService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, SmsForegroundService::class.java).apply {
                action = SmsForegroundService.ACTION_START
            }
        )
        updateServiceButton(true)
        toast("서비스 시작됨")
    }

    private fun stopService() {
        startService(Intent(this, SmsForegroundService::class.java).apply {
            action = SmsForegroundService.ACTION_STOP
        })
        updateServiceButton(false)
        toast("서비스 중지됨")
    }

    private fun updateServiceButton(running: Boolean) {
        binding.btnToggleService.text = if (running) "⏹ 서비스 중지" else "▶ 서비스 시작"
        binding.tvStatus.text = if (running) "🟢 실행 중" else "🔴 중지됨"
    }

    private fun checkPermissions() {
        val needed = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed, REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            toast("⚠️ SMS 권한이 필요합니다")
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
