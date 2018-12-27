package com.wealoha.testvideocompress.video

import android.media.MediaExtractor
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.nio.ByteBuffer
import android.media.MediaCodec.MetricsConstants.MIME_TYPE
import android.media.MediaCodecInfo

/**
 * Created by jichaopeng
 * 2018/12/26
 */
class TestCompressMediaCodec {
    private val TAG = "VideoDecoder"
    private var mediaDecoder: MediaCodec? = null
    private var mediaExtractor: MediaExtractor? = null
    private lateinit var mediaFormat: MediaFormat
    private lateinit var mediaMuxer: MediaMuxer
    private var mime: String?=null

    fun decodeVideo(url: String, clipPoint: Long, clipDuration: Long): Boolean {
        var videoTrackIndex = -1
        var audioTrackIndex = -1
        var videoMaxInputSize = 0
        var audioMaxInputSize = 0
        var sourceVTrack = 0
        var sourceATrack = 0
        var videoDuration: Long = 0
        var audioDuration: Long = 0
        //创建分离器
        mediaExtractor = MediaExtractor()
        mediaExtractor?.let {
            try {
                //设置文件路径
                it.setDataSource(url)
                //创建合成器
                mediaMuxer =
                        MediaMuxer(
                            url.substring(0, url.lastIndexOf(".")) + "_output2.mp4",
                            MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                        )
            } catch (e: Exception) {
                Log.e(TAG, "error path" + e.message)
            }
            initMediaCodec()
            //获取每个轨道的信息
            for (i in 0 until it.trackCount) {
                try {
                    mediaFormat = it.getTrackFormat(i)
                    mime= mediaFormat.getString(MediaFormat.KEY_MIME)
                    mime?.let { it ->
                        if (it.startsWith("video/")) {
                            sourceVTrack = i
                            val width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH)
                            val height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)
                            mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE).let { it ->
                                videoMaxInputSize = it
                            }
                            mediaFormat.getLong(MediaFormat.KEY_DURATION).let { it ->
                                videoDuration = it
                            }
                            val format = MediaFormat.createVideoFormat(MIME_TYPE, 640, 360)//MIME_TYPE = "video/avc",H264的MIME类型，宽，高
                            format.setInteger(
                                MediaFormat.KEY_COLOR_FORMAT,
                                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                            )//设置颜色格式
//                            format.setInteger(MediaFormat.KEY_BIT_RATE, 30)//设置比特率
                            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)//设置帧率
                            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)//设置关键帧时间
                            videoTrackIndex = mediaMuxer.addTrack(format)
                        } else if (it.startsWith("audio/")) {
                            sourceATrack = i
                            val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            val channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                            audioMaxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                            audioDuration = mediaFormat.getLong(MediaFormat.KEY_DURATION)
                            Log.d(
                                TAG, "sampleRate is " + sampleRate
                                        + ";channelCount is " + channelCount
                                        + ";audioMaxInputSize is " + audioMaxInputSize
                                        + ";audioDuration is " + audioDuration
                            )
                            audioTrackIndex = mediaMuxer.addTrack(mediaFormat)
                        }
                    }

                    Log.d(TAG, "file mime is $mime")
                } catch (e: Exception) {
                    Log.e(TAG, " read error " + e.message)
                }

            }
            //分配缓冲
            val inputBuffer = ByteBuffer.allocate(videoMaxInputSize)
            //根据官方文档的解释MediaMuxer的start一定要在addTrack之后
            mediaMuxer.start()
            //视频处理部分
            it.selectTrack(sourceVTrack)
            val videoInfo = MediaCodec.BufferInfo()
            videoInfo.presentationTimeUs = 0
            val videoSampleTime: Long
            //获取源视频相邻帧之间的时间间隔。(1)
            run {
                it.readSampleData(inputBuffer, 0)
                //skip first I frame
                if (it.sampleFlags == MediaExtractor.SAMPLE_FLAG_SYNC)
                    it.advance()
                it.readSampleData(inputBuffer, 0)
                val firstVideoPTS = it.sampleTime
                it.advance()
                it.readSampleData(inputBuffer, 0)
                val secondVideoPTS = it.sampleTime
                videoSampleTime = Math.abs(secondVideoPTS - firstVideoPTS)
                Log.d(TAG, "videoSampleTime is $videoSampleTime")
            }
            //选择起点
            it.seekTo(clipPoint, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            while (true) {
                val sampleSize = it.readSampleData(inputBuffer, 0)
                if (sampleSize < 0) {
                    //这里一定要释放选择的轨道，不然另一个轨道就无法选中了
                    it.unselectTrack(sourceVTrack)
                    break
                }
                val trackIndex = it.sampleTrackIndex
                //获取时间戳
                val presentationTimeUs = it.sampleTime
                //获取帧类型，只能识别是否为I帧
                val sampleFlag = it.sampleFlags
                Log.d(
                    TAG, "trackIndex is " + trackIndex
                            + ";presentationTimeUs is " + presentationTimeUs
                            + ";sampleFlag is " + sampleFlag
                            + ";sampleSize is " + sampleSize
                )
                //剪辑时间到了就跳出
                if (clipDuration != 0L && presentationTimeUs > clipPoint + clipDuration) {
                    it.unselectTrack(sourceVTrack)
                    break
                }
                it.advance()
                videoInfo.offset = 0
                videoInfo.size = sampleSize
                videoInfo.flags = sampleFlag
                mediaMuxer.writeSampleData(videoTrackIndex, inputBuffer, videoInfo)
                videoInfo.presentationTimeUs += videoSampleTime//presentationTimeUs;
            }

            it.seekTo(clipPoint, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            while (true) {
                val sampleSize = it.readSampleData(inputBuffer, 0)
                if (sampleSize < 0) {
                    it.unselectTrack(sourceATrack)
                    break
                }
                val trackIndex = it.sampleTrackIndex
                val presentationTimeUs = it.sampleTime
                Log.d(
                    TAG, "trackIndex is " + trackIndex
                            + ";presentationTimeUs is " + presentationTimeUs
                )
                if (clipDuration != 0L && presentationTimeUs > clipPoint + clipDuration) {
                    it.unselectTrack(sourceATrack)
                    break
                }
                it.advance()
//                audioInfo.offset = 0
//                audioInfo.size = sampleSize
//                mediaMuxer.writeSampleData(audioTrackIndex, inputBuffer, audioInfo)
//                audioInfo.presentationTimeUs += audioSampleTime//presentationTimeUs;
            }
            //全部写完后释放MediaMuxer和MediaExtractor
            mediaMuxer.stop()
            mediaMuxer.release()
            it.release()
            mediaExtractor = null
            return true
        }
    }

    private fun initMediaCodec() {
        val format = MediaFormat.createVideoFormat(MIME_TYPE, 640, 360)//MIME_TYPE = "video/avc",H264的MIME类型，宽，高
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )//设置颜色格式
        format.setInteger(MediaFormat.KEY_BIT_RATE, 30)//设置比特率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)//设置帧率
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)//设置关键帧时间

        mediaDecoder = MediaCodec.createEncoderByType(MIME_TYPE)//创建编码器
        mediaDecoder?.configure(
            format,
            null,
            null,
            MediaCodec.CONFIGURE_FLAG_ENCODE
        )//四个参数，第一个是media格式，第二个是解码器播放的surfaceview，第三个是MediaCrypto，第四个是编码解码的标识
        mediaDecoder?.start()
    }
}
