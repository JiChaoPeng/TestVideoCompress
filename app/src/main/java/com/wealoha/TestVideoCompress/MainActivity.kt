package com.wealoha.TestVideoCompress

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.wealoha.TestVideoCompress.video.CompressVideo
import com.wealoha.TestVideoCompress.video.Test
import com.wealoha.TestVideoCompress.video.TestMediaCodec
import com.wealoha.textapplication.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Handler.Callback {
    private val messageWhat = 100
    private var fileName: String? = null
    private val mFilePath = Environment.getExternalStorageDirectory().absolutePath + "/123.mp4"
    private val mOutPath = Environment.getExternalStorageDirectory().absolutePath + "/123_out34.mp4"

    override fun handleMessage(msg: Message?): Boolean {
        when (msg?.what) {
            messageWhat -> {
                time++
                startTime.text = "$time"
                handler.sendEmptyMessageDelayed(messageWhat, 1000)
            }
        }
        return false
    }

    private var isRecording = false
    private var time = 0
    private val handler: Handler = Handler(this@MainActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initTouch()
        initView()

    }

    private fun initView() {
        //开始剪辑
        startClip.onSingleClick {
            val mFilePath = Environment.getExternalStorageDirectory().absolutePath + "/123.mp4"
            TestMediaCodec().decodeVideo(mFilePath,2000000,7000000)
        }
        //开始录音
        startRecording.onSingleClick {
            isRecording = !isRecording
        }
        startCompress.onSingleClick {
            var compressedFile = CompressVideo().getCompressedFile(mFilePath)
        }
        startCom1.onSingleClick {
            Test().videoDecode(mFilePath)
        }
    }

    private fun initTouch() {
        startRecording.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_DOWN) {
                startRecording.text = "结束录音"
                startTime.visibility = View.VISIBLE
                startTime.text = "$time"
                handler.sendEmptyMessageDelayed(messageWhat, 1000)
                fileName = RecordingUtil().startRecording()
            } else if (event?.action == MotionEvent.ACTION_UP) {
                RecordingUtil().stopRecording()
                Log.d("MainActivity", "$fileName")
                if (time < 2) {
                    fileName?.let {
                        val deleteFile = RecordingUtil().deleteFile(it)
                        Log.d("MainActivity", "$deleteFile")
                    }
                    Toast.makeText(this, "不能少于2秒", Toast.LENGTH_SHORT).show()
                }
                startRecording.text = "开始录音"
                startTime.visibility = View.GONE
                time = 0
                handler.removeMessages(messageWhat)
            }
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
