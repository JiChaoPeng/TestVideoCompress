package com.wealoha.testvideocompress

import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by jichaopeng
 * 2018/12/25
 */
class RecordingUtil {

    private var mRecorder: MediaRecorder? = null

    init {
        if (mRecorder == null) {
            mRecorder = MediaRecorder()
        }
    }

    fun startRecording(): String {

        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        //输出格式
        mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR)
        //设置音频编码器
        mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        val newFileName = newFileName()
        mRecorder?.setOutputFile(newFileName)

        try {
            // 准备好开始录音
            mRecorder?.prepare()

            mRecorder?.start()
        } catch (e: IllegalStateException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return newFileName
    }

    fun stopRecording() {
        if (mRecorder != null) {
            //added by ouyang start
            try {
                //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
                //报错为：RuntimeException:stop failed
                mRecorder?.setOnErrorListener(null)
                mRecorder?.setOnInfoListener(null)
                mRecorder?.setPreviewDisplay(null)
                mRecorder?.stop()
            } catch (e: IllegalStateException) {
                Log.i("Exception", Log.getStackTraceString(e))
            } catch (e: RuntimeException) {
                Log.i("Exception", Log.getStackTraceString(e))
            } catch (e: Exception) {
                Log.i("Exception", Log.getStackTraceString(e))
            }
            //added by ouyang end
            if (mRecorder != null) {
                mRecorder?.release()
                mRecorder = null
            }
//            upRecord()
        }
    }

    private fun newFileName(): String {
        val mFileName = Environment.getExternalStorageDirectory().absolutePath
        val s = SimpleDateFormat("yyyy-MM-dd hhmmss")
            .format(Date())
        return "$mFileName/rcd_$s.mp3"
    }

    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        if (file.isFile && file.exists()) {
            return file.delete()
        }
        return false
    }

}