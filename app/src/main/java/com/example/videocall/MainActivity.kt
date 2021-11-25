package com.example.videocall

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.videocall.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val videoPermissions =
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
    private val audioPermissions = Manifest.permission.RECORD_AUDIO

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_main, null, false)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        binding.btnVideo.setOnClickListener {
            videoLauncher.launch(videoPermissions)
        }

        binding.btnAudio.setOnClickListener {
            audioLauncher.launch(audioPermissions)
        }
    }

    private val videoLauncher = registerForActivityResult(
        RequestMultiplePermissions()
    ) { it ->
        //通过的权限
        val grantedList = it.filterValues { it }.mapNotNull { it.key }
        //是否所有权限都通过
        if (grantedList.size == it.size) {
            startActivity(Intent(this, VideoCallActivity::class.java))
        } else {
            Toast.makeText(this, "请打开相应权限", Toast.LENGTH_SHORT).show()
        }
    }

    private val audioLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startActivity(Intent(this, AudioCallActivity::class.java))
        } else {
            Toast.makeText(this, "请打开相应权限", Toast.LENGTH_SHORT).show()
        }
    }
}