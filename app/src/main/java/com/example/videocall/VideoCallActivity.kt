package com.example.videocall

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.videocall.databinding.ActivityVideoCallBinding
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas

class VideoCallActivity : AppCompatActivity() {

    companion object {
        const val TAG = "VideoCallActivity"
    }

    //是否静音
    private var mMuted = false

    private lateinit var binding: ActivityVideoCallBinding

    private var mRtcEngine: RtcEngine? = null

    /**
     * 声网事件回调函数
     */
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        //错误回调
        override fun onError(err: Int) {
            Log.e(TAG, "errorCode:$err" )
        }

        // 注册 onUserJoined 回调。
        // 远端用户成功加入频道时，会触发该回调。
        // 可以在该回调用调用 setupRemoteVideo 方法设置远端视图。
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        // 注册 onUserOffline 回调。远端用户离开频道后，会触发该回调。
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Toast.makeText(this@VideoCallActivity, "对方退出通话", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_video_call, null, false)
        binding.lifecycleOwner = this
        setContentView(binding.root)
        initEngineAndJoinChannel()

        binding.btnSwitchCamera.setOnClickListener { mRtcEngine?.switchCamera() }

        binding.btnMute.setOnClickListener {
            mMuted = !mMuted
            mRtcEngine!!.muteLocalAudioStream(mMuted)
            val res = if (mMuted) R.drawable.btn_mute else R.drawable.btn_unmute
            binding.btnMute.setImageResource(res)
        }

        binding.btnCall.setOnClickListener { finish() }
    }


    //初始化声网相关组件
    private fun initEngineAndJoinChannel() {
        initializeAgoraEngine()
        setupLocalVideo()
        joinChannel()
    }

    // 调用 Agora SDK 的方法初始化 RtcEngine。
    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(this, AppConfig.AGORA_APP_ID, mRtcEventHandler)
        } catch (e: Exception) {
            Toast.makeText(this, "音视频引擎初始化失败，请退出重试", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "initializeAgoraEngine: "+e.message)
        }
    }


    //这里需要判断用户是主叫还是被叫，被叫需要用户自己手动加入频道
    private fun joinChannel() {
        // 调用 joinChannel 方法加入频道。
        mRtcEngine?.joinChannel(
            AppConfig.AGORA_ACCESS_TOKEN,
            "demoChannel1",
            "Extra Optional Data",
            0
        )
    }


    private fun setupLocalVideo() {
        // 启用视频模块。
        mRtcEngine?.enableVideo()
        val container = binding.localVideoViewContainer
        // 创建 SurfaceView。
        val surfaceView = RtcEngine.CreateRendererView(this)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        // 设置本地视图。
        mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FILL, 0))
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = binding.remoteVideoViewContainer
        // 创建一个 SurfaceView 对象。
        val surfaceView = RtcEngine.CreateRendererView(this)
        container.addView(surfaceView)

        // 设置远端视图。
        mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

    private fun leaveChannel() {
        // 离开当前频道。
        mRtcEngine?.leaveChannel()
    }

}