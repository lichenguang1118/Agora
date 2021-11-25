package com.example.videocall

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.videocall.databinding.ActivityAudioCallBinding
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine

class AudioCallActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AudioCallActivity"
    }

    //是否静音
    private var isMuted = false

    private lateinit var binding: ActivityAudioCallBinding

    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        //错误回调
        override fun onError(err: Int) {
            Log.e(VideoCallActivity.TAG, "errorCode:$err" )
        }

        // 注册 onUserOffline 回调。远端用户离开频道后，会触发该回调。
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Toast.makeText(this@AudioCallActivity, "对方退出通话", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        // 注册 onUserMuteAudio 回调。远端用户静音后，会触发该回调。
        override fun onUserMuteAudio(uid: Int, muted: Boolean) {
            runOnUiThread {
                if (muted) {
                    Toast.makeText(this@AudioCallActivity, "对方已静音", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_audio_call, null, false)
        setContentView(binding.root)

        initAgoraEngineAndJoinChannel()

        binding.ivAudioCallMute.setOnClickListener {
            isMuted = !isMuted
            mRtcEngine?.muteLocalAudioStream(isMuted)
            val res = if (isMuted) R.drawable.btn_unmute else R.drawable.btn_mute
            binding.ivAudioCallMute.setImageResource(res)
        }

        binding.ivAudioCallSpeaker.setOnClickListener {
            mRtcEngine?.setEnableSpeakerphone(!binding.ivAudioCallSpeaker.isSelected)
        }

        binding.ivAudioCallStop.setOnClickListener { finish() }
    }

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        joinChannel()
    }

    // 调用 Agora SDK 的方法初始化 RtcEngine。
    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext,AppConfig.AGORA_APP_ID, mRtcEventHandler)
        } catch (e: Exception) {
            Toast.makeText(this, "音视频引擎初始化失败，请退出重试", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "initializeAgoraEngine: "+e.message)
        }
    }

    //这里需要判断用户是主叫还是被叫，被叫需要用户自己手动加入频道
    private fun joinChannel() {
        // 调用 Agora SDK 的 joinChannel 方法加入频道。未指定 uid，SDK 会自动分配一个。
        mRtcEngine?.joinChannel(AppConfig.AGORA_ACCESS_TOKEN, "demoChannel1", "Extra Optional Data", 0)
        mRtcEngine
    }

    private fun leaveChannel() {
        mRtcEngine?.leaveChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

}