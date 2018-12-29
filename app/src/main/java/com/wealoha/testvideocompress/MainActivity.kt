package com.wealoha.testvideocompress

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.vincent.videocompressor.VideoCompress
import com.wealoha.testvideocompress.video.CompressVideo
import com.wealoha.testvideocompress.video.Test
import com.wealoha.testvideocompress.video.TestMediaCodec
import com.wealoha.textapplication.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Handler.Callback {
    private val messageWhat = 100
    private var fileName: String? = null
    private val mFilePath = Environment.getExternalStorageDirectory().absolutePath + "/1234.mp4"
    private val mOutPath = Environment.getExternalStorageDirectory().absolutePath + "/1234_out.mp4"

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
        ActivityCompat.requestPermissions(this,
             arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO), 1)
    }

    private fun initView() {



        //开始剪辑
        startClip.onSingleClick {
            val mFilePath = Environment.getExternalStorageDirectory().absolutePath + "/123.mp4"
            TestMediaCodec().decodeVideo(mFilePath, 2000000, 7000000)
        }
        //开始录音
        startRecording.onSingleClick {
            isRecording = !isRecording
        }
        startCompress.onSingleClick {
            CompressVideo().getCompressedFile(mFilePath)
        }
        startCom1.onSingleClick {
                   Test().videoDecode(mFilePath)
        }
        startCompressor.onSingleClick {
           val task= VideoCompress.compressVideoLow(mFilePath,mOutPath,object :VideoCompress.CompressListener{
                override fun onFail() {

                    Log.e("VideoCompress",":: onFail")
                }

                override fun onProgress(percent: Float) {
                    Log.e("VideoCompress","::::::::    $percent")
                }

                override fun onSuccess() {
                    Log.e("VideoCompress",":: onSuccess")
                }

                override fun onStart() {
                    Log.e("VideoCompress",":: onStart")
                }

            })

        }
        startTranscoder.onSingleClick {  }
    }

    private fun initTouch() {
        startRecording.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_DOWN) {
                startRecording.text = "结束录音"
                startTime.visibility = View.VISIBLE
                startTime.text = "$time"
                handler.sendEmptyMessageDelayed(messageWhat, 1000)
                 RecordAudio(this).startRecording()
            } else if (event?.action == MotionEvent.ACTION_UP) {
                RecordAudio(this).stopRecording(object :RecordAudioInterface{
                    override fun onSucceed(path: String) {
                        fileName =path
                        Log.e("MainActivity","path$path")
                    }

                    override fun onError(error: String?) {
                        Log.e("MainActivity","error$error")
                    }
                })

                Log.d("MainActivity", "$fileName")
                if (time < 2) {
                    fileName?.let {
                        val deleteFile = RecordAudio(this).deleteFile(it)
                        Log.d("MainActivity", "ww$deleteFile")
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
