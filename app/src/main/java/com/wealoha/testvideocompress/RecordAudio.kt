package com.wealoha.testvideocompress

import android.content.Context
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
class RecordAudio (context: Context){
    private var context:Context=context
    companion object {
        private var mRecorder: MediaRecorder? = null
        private var path:String=""
    }
    init {
        if (mRecorder == null) {
            mRecorder = MediaRecorder()
        }
    }

    fun startRecording() {
        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        //输出格式
        mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR)
        //设置音频编码器
        mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        path = newFileName()
        mRecorder?.setOutputFile(path)
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
    }

    fun stopRecording(inter : RecordAudioInterface) {
        if (mRecorder != null) {
            //added by ouyang start
            try {
                //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
                //报错为：RuntimeException:stop failed
                mRecorder?.setOnErrorListener(null)
                mRecorder?.setOnInfoListener(null)
                mRecorder?.setPreviewDisplay(null)
                mRecorder?.stop()
            } catch (e: Exception) {
                Log.i("Exception", Log.getStackTraceString(e))
                inter.onError(e.message)
            }
            if (mRecorder != null) {
                mRecorder?.release()
                mRecorder = null
            }
            inter.onSucceed(path)
        }
    }

    private fun newFileName(): String {
        val mFilePath = DirectoryExtension.getCacheDirectory(context,android.os.Environment.DIRECTORY_MUSIC)
        val s = SimpleDateFormat("yyyy-MM-dd hhmmss")
            .format(Date())
        return "$mFilePath/audio_$s.mp3"
    }

    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        if (file.isFile && file.exists()) {
            return file.delete()
        }
        return false
    }

}