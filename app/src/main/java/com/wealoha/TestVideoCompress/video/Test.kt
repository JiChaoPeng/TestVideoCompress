package com.wealoha.TestVideoCompress.video

import android.annotation.TargetApi
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.*
import android.os.Build
import android.util.Log

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by jichaopeng
 * 2018/12/27
 */
class Test {

    private val decodeColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
    private val MIME_TYPE = "video/avc"
    private var outputImageFileType = -1
    private var OUTPUT_DIR: String? = null
    private lateinit var mediaMuxer: MediaMuxer
    private lateinit var decoder: MediaCodec

    fun initMuxer(url: String) {
        try {
            //设置文件路径

            //创建合成器
            mediaMuxer = MediaMuxer(
                url.substring(0, url.lastIndexOf(".")) + "_output2.mp4",
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            )

        } catch (e: Exception) {
            Log.e(TAG, "error path" + e.message)
        }
    }

    private fun initMediaCodec( ) {
        val format = MediaFormat.createVideoFormat(MIME_TYPE, 640, 360)//MIME_TYPE = "video/avc",H264的MIME类型，宽，高
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )//设置颜色格式
        format.setInteger(MediaFormat.KEY_BIT_RATE, 30)//设置比特率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)//设置帧率
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)//设置关键帧时间

        decoder = MediaCodec.createEncoderByType(MIME_TYPE)//创建编码器
        decoder.configure(
            format,
            null,
            null,
            MediaCodec.CONFIGURE_FLAG_ENCODE
        )//四个参数，第一个是media格式，第二个是解码器播放的surfaceview，第三个是MediaCrypto，第四个是编码解码的标识
        decoder.start()
    }

    @Throws(IOException::class)
    fun videoDecode(videoFilePath: String) {
        var extractor: MediaExtractor? = null
        var decoder: MediaCodec? = null
        initMuxer(videoFilePath)
//        try {
        val videoFile = File(videoFilePath)
        extractor = MediaExtractor()
        extractor.setDataSource(videoFile.toString())
        val trackIndex = selectTrack(extractor)
        if (trackIndex < 0) {
            throw RuntimeException("No video track found in $videoFilePath")
        }
        extractor.selectTrack(trackIndex)

        val mediaFormat = extractor.getTrackFormat(trackIndex)
        decoder = MediaCodec.createDecoderByType(MIME_TYPE)
//            showSupportedColorFormat(decoder!!.codecInfo.getCapabilitiesForType(mime))

        val format = MediaFormat.createVideoFormat(MIME_TYPE, mediaFormat.getInteger(MediaFormat.KEY_WIDTH), mediaFormat.getInteger(MediaFormat.KEY_HEIGHT))//MIME_TYPE = "video/avc",H264的MIME类型，宽，高



        if (isColorFormatSupported(decodeColorFormat, decoder.codecInfo.getCapabilitiesForType(MIME_TYPE))) {
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, decodeColorFormat)
            Log.i(TAG, "set decode color format to type $decodeColorFormat")
        } else {
            Log.i(TAG, "unable to set decode color format, color format type $decodeColorFormat not supported")
        }
        initMediaCodec( )
        mediaMuxer.addTrack(mediaFormat)
        mediaMuxer.start()
        decodeFramesToImage(extractor, format)
//            decoder.stop()
//        } catch (e: java.lang.Exception) {
//            Log.i(TAG, "AA:${e.message}${e.cause}")
//
//        } finally {
//            if (decoder != null) {
//                decoder.stop()
//                decoder.release()
//                decoder = null
//            }
//            if (extractor != null) {
//                extractor.release()
//                extractor = null
//            }
//        }
    }

    private fun showSupportedColorFormat(caps: MediaCodecInfo.CodecCapabilities) {
        print("supported color format: ")
        for (c in caps.colorFormats) {
            print(c.toString() + "\t")
        }
        println()
    }

    private fun isColorFormatSupported(colorFormat: Int, caps: MediaCodecInfo.CodecCapabilities): Boolean {
        for (c in caps.colorFormats) {
            if (c == colorFormat) {
                return true
            }
        }
        return false
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun decodeFramesToImage(extractor: MediaExtractor, mediaFormat: MediaFormat) {
        val info = MediaCodec.BufferInfo()
        var sawInputEOS = false
        var sawOutputEOS = false

        val width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH)
        val height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)
        var outputFrameCount = 0
        while (!sawOutputEOS) {
            if (!sawInputEOS) {
                val inputBufferId = decoder.dequeueInputBuffer(DEFAULT_TIMEOUT_US)
                if (inputBufferId >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputBufferId)
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(inputBufferId, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        sawInputEOS = true
                    } else {
                        val presentationTimeUs = extractor.sampleTime
                        info.offset = 0
                        info.size = sampleSize
                        info.flags = extractor.sampleFlags
                        mediaMuxer.writeSampleData(inputBufferId, inputBuffer, info)
                        decoder.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTimeUs, 0)
                        extractor.advance()
                    }
                }
            }
            val outputBufferId = decoder.dequeueOutputBuffer(info,
                DEFAULT_TIMEOUT_US
            )
            if (outputBufferId >= 0) {
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    sawOutputEOS = true
                }
                val doRender = info.size != 0
                if (doRender) {
                    outputFrameCount++
                    decoder.releaseOutputBuffer(outputBufferId, true)
                }
            }
        }
        //全部写完后释放MediaMuxer和MediaExtractor
        mediaMuxer.stop()
        mediaMuxer.release()
    }


    companion object {
        private val TAG = "VideoToFrames"
        private val VERBOSE = true
        private val DEFAULT_TIMEOUT_US: Long = 10000000

        private val COLOR_FormatI420 = 1
        private val COLOR_FormatNV21 = 2

        val FILE_TypeI420 = 1
        val FILE_TypeNV21 = 2
        val FILE_TypeJPEG = 3

        private fun selectTrack(extractor: MediaExtractor): Int {
            val numTracks = extractor.trackCount
            for (i in 0 until numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime.startsWith("video/")) {
                    if (VERBOSE) {
                        Log.d(TAG, "Extractor selected track $i ($mime): $format")
                    }
                    return i
                }
            }
            return -1
        }

        private fun isImageFormatSupported(image: Image): Boolean {
            val format = image.format
            when (format) {
                ImageFormat.YUV_420_888, ImageFormat.NV21, ImageFormat.YV12 -> return true
            }
            return false
        }
    }

}
