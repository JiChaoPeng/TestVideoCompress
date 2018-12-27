//package com.wealoha.textapplication.video
//
//import android.media.*
//import android.os.Build
//import java.io.IOException
//
///**
// * Created by jichaopeng
// * 2018/12/26
// */
//class TextCompress{
//    private var mediaMuxer: MediaMuxer? = null
//    private val MIME_TYPE = "Video/AVC"
//    private lateinit var mEncoder: MediaCodec
//    private lateinit var mediaDecoder: MediaCodec
//    private val bitRate = 65536
//    private var mVideoExtractor = MediaExtractor()
//    fun initMediaCodec() {
//        //MIME_TYPE = "video/avc",H264的MIME类型，宽，高
//        val format = MediaFormat.createVideoFormat(MIME_TYPE, 360, 640)
//        format.setInteger(
//            MediaFormat.KEY_COLOR_FORMAT,
//            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
//        )//设置颜色格式
//        //设置比特率
//        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
//        //设置帧率
//        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
//        //设置关键帧时间
//        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
//
//        try {
//            //创建编码器
//            mediaDecoder = MediaCodec.createEncoderByType(MIME_TYPE)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mediaDecoder.setCallback(object:MediaCodec.Callback(){
//                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
//
//                }
//
//                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
//                    // Subsequent data will conform to new format.
//                    // Can ignore if using getOutputFormat(outputBufferId)
////                    mOutputFormat = format // option B
//                }
//
//                override fun onInputBufferAvailable(codec: MediaCodec, inputBufferId: Int) {
//                    val inputBuffer = codec.getInputBuffer(inputBufferId)
//                    // fill inputBuffer with valid data
//
//                    codec.queueInputBuffer(inputBufferId,)
//                }
//
//                override fun onOutputBufferAvailable(codec: MediaCodec, outputBufferId: Int, info: MediaCodec.BufferInfo) {
//                    val outputBuffer = codec.getOutputBuffer(outputBufferId)
//                    val bufferFormat = codec.getOutputFormat(outputBufferId) // option A
//                    // bufferFormat is equivalent to mOutputFormat
//                    // outputBuffer is ready to be processed or rendered.
//                    codec.releaseOutputBuffer(outputBufferId,true)
//
//                }
//
//            })
//        }
//        mediaDecoder.configure(
//            format, null, null,
//            MediaCodec.CONFIGURE_FLAG_ENCODE
//        )//四个参数，第一个是media格式，第二个是解码器播放的surfaceview，第三个是MediaCrypto，第四个是编码解码的标识
//
//        mediaDecoder.start()
////        mEncodeInputBuffer = mediaDecoder.inputBuffers
////        mEncodeOutputBuffer = mediaDecoder.outputBuffers
//        mVideoExtractor.selectTrack(0)
//
//    }
//    /**
//    //     * 释放资源
//    //     */
//    private fun release() {
//        try {
//            if (mEncoder != null) {
//                mEncoder.stop()
//                mEncoder.release()
//            }
//            if (mediaMuxer != null) {
//                mediaMuxer!!.stop()
//                mediaMuxer!!.release()
//            }
//            if (mediaDecoder != null) {
//                mediaDecoder!!.stop()
//                mediaDecoder!!.release()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }
//
//}