package com.wealoha.testvideocompress

/**
 * Created by jichaopeng
 * 2018/12/28
 */
interface RecordAudioInterface{
    fun onSucceed(path:String)
    fun onError(error:String?)
}