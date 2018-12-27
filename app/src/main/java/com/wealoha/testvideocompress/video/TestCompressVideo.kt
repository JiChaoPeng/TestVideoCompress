//package com.wealoha.textapplication.video
//
//import android.media.MediaCodec
//import android.media.MediaCodecInfo
//import android.media.MediaExtractor
//import android.media.MediaFormat
//import android.media.MediaMuxer
//import android.media.MediaCodec.BufferInfo
//import android.os.Environment
//import android.util.Log
//
//import java.io.IOException
//import java.nio.ByteBuffer
//
///**
// * Created by jichaopeng
// * 2018/12/26
// * jichaopeng
// * @author jichaopeng
// */
//class TestCompressVideo {
//    companion object {
//        private val MIME_TYPE = "Video/AVC"
//        private val MP3_TYPE = 0
//        private val AAC_TYPE = 1
//        private val M4A_TYPE = 2
//        //不用重新编解码
//        private val NOT_DC_EC_TYPE = 3
//        //不支持类型
//        private val NOT_SUPPORT_TYPE = 4
//        private val TAG = "TestCompressVideo"
//    }
//    private var mediaMuxer: MediaMuxer? = null
//    private var mVideoExtractor = MediaExtractor()
////    private val mAudioExtractor = MediaExtractor()
//    private val mAudioTrackIndex = -1
//    private val mVideoTrackIndex = -1
//    private val MAX_INPUT_SIZE = 10240
//    //512 * 1024
//    private val VIDEO_READ_SAMPLE_SIZE = 524288
//    // 64 * 1024
//    private val BIT_RATE = 65536
//    // acc sample rate
//    private val SAMPLE_RATE = 44100
//    private val TIMEOUT_US = 1000L
//    private val mMaxTimeStamp: Long = 0
//    private val mVideoFormat: MediaFormat? = null
//    private val mAudioFormat: MediaFormat? = null
//    private var mediaDecoder: MediaCodec? = null
//    private val mEncoder: MediaCodec? = null
//    private var mDecodeInputBuffer: Array<ByteBuffer>? = null
//    private var encoderOutputBuffers: Array<ByteBuffer>? = null
//    private var mDecodeOutputBuffer: Array<ByteBuffer>? = null
//    private var codecOutputBuffers: Array<ByteBuffer>? = null
//    private var mEncodeInputBuffer: Array<ByteBuffer>? = null
//    private var mEncodeOutputBuffer: Array<ByteBuffer>? = null
//    private val mAudioDEType = 0
//
//    private val mVideoPath = Environment.getExternalStorageDirectory().absolutePath + "/123.mp4"
//
//
//    /**
//     * 输入要解码的buffer
//     *
//     * @return 输入是否成功
//     */
////    private fun decodeInputBuffer() {
////        while (true) {
////            val inputBufIndex = mediaDecoder!!.dequeueInputBuffer(TIMEOUT_US)
////            if (inputBufIndex >= 0) {
////                mDecodeInputBuffer!![inputBufIndex].clear()
////                val sampleSize = mAudioExtractor.readSampleData(mDecodeInputBuffer[inputBufIndex], 0)
////                if (sampleSize > 0) {
////                    if (presentationTimeUs > mMaxTimeStamp) {
////                        return //超过最大时间戳的音频不处理
////                    }
////                    mediaDecoder!!.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, 0)
////                    mAudioExtractor.advance()
////                    decodeOutputBuffer()
////                }
////            }
////        }
////    }
//
//    /**
//     * 编码PCM数据
//     *
//     * @param input              pcm数据
//     * @param presentationTimeUs 时间戳
//     */
//    @Synchronized
//    private fun encodData(input: ByteArray, presentationTimeUs: Long) {
//        val inputBufferIndex = mEncoder!!.dequeueInputBuffer(-1)
//        if (inputBufferIndex >= 0) {
//            val inputBuffer = mEncodeInputBuffer!![inputBufferIndex]
//            inputBuffer.clear()
//            inputBuffer.put(input)
//            mEncoder.queueInputBuffer(inputBufferIndex, 0, input.size, presentationTimeUs, 0)
//        }
//
//        val bufferInfo = BufferInfo()
//        var outputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 0)
//        while (outputBufferIndex >= 0) {
//            val outBitsSize = bufferInfo.size
//            val outputBuffer = mEncodeOutputBuffer!![outputBufferIndex]
//            outputBuffer.position(bufferInfo.offset)
//            outputBuffer.limit(bufferInfo.offset + outBitsSize)
//            outputBuffer.position(bufferInfo.offset)
//            mediaMuxer!!.writeSampleData(mAudioTrackIndex, outputBuffer, bufferInfo)
//            mEncoder.releaseOutputBuffer(outputBufferIndex, false)
//            outputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 0)
//        }
//    }
//
//    /**
//     * 获取输出的解码后的buffer
//     */
//    private fun decodeOutputBuffer() {
//        val info = MediaCodec.BufferInfo()
//        val outputIndex = mediaDecoder!!.dequeueOutputBuffer(info, -1)
//        if (outputIndex >= 0) {
//            val chunk = ByteArray(info.size)
//            mDecodeOutputBuffer!![outputIndex].position(info.offset)
//            mDecodeOutputBuffer[outputIndex].limit(info.offset + info.size)
//            mDecodeOutputBuffer[outputIndex].get(chunk)
//            mDecodeOutputBuffer[outputIndex].clear()
//            mediaDecoder!!.releaseOutputBuffer(outputIndex, false)
//            if (chunk.size > 0) {
//                encodData(chunk, info.presentationTimeUs)
//            }
//        }
//    }
//
//    /**
//     * 开始合成
//     */
//    fun start() {
//        if (mAudioDEType == MP3_TYPE) {
//            initMediaCodec()
//            if (initMuxer()) {
//                if (handleTrack(mediaMuxer, mVideoTrackIndex)) {
//                    decodeInputBuffer()//解码--->编码--->合成输出文件
//                }
//            }
//        } else if (mAudioDEType == NOT_DC_EC_TYPE) {//直接合成文件
//            if (initMuxer()) {
//                handleTrack(mediaMuxer, mVideoTrackIndex)
//                handleTrack(mediaMuxer, mAudioTrackIndex)
//            }
//        }
//        release()
//        Log.e(TAG, "finished~~~~~~~~~~~~~~~~~~~~")
//    }
//
//    /**
//     * 初始化混合器
//     */
//    private fun initMuxer(): Boolean {
//        //创建分离器
//        mVideoExtractor = MediaExtractor()
//        try {
//            //设置文件路径
//            mVideoExtractor.setDataSource(mVideoPath)
//            //创建合成器
//            mediaMuxer = MediaMuxer(
//                mVideoPath.substring(0, mVideoPath.lastIndexOf(".")) + "_output6.mp4",
//                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
//            )
//            return true
//        } catch (e: Exception) {
//            Log.e(TAG, "error path" + e.message)
//
//        }
//
//        return false
//    }
//
//    /**
//     * 写入提取的数据到目标文件
//     * @param mediaMuxer 数据合成器
//     * @param trackIndex  数据轨道
//     * @param extractor   数据提取器
//     */
//    private fun handleTrack(mediaMuxer: MediaMuxer?, trackIndex: Int, extractor: MediaExtractor?): Boolean {
//        if (mediaMuxer ==
//            null || trackIndex < 0 || extractor == null
//        ) {
//            return false
//        }
//        val inputBuffer = ByteBuffer.allocate(VIDEO_READ_SAMPLE_SIZE)
//        val info = BufferInfo()
//        val sampleSize = extractor.readSampleData(inputBuffer, 0)
//        while (sampleSize > 0) {
//            info.offset = 0
//            info.size = sampleSize
//            info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
//            info.presentationTimeUs = extractor.sampleTime
//            if (mMaxTimeStamp < info.presentationTimeUs) {
//                break
//            }
//            extractor.advance()
//            mediaMuxer.writeSampleData(trackIndex, inputBuffer, info)
//        }
//        return true
//    }
//
//    /**
//     * 释放资源
//     */
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
//    private fun initMediaCodec() {
//        //MIME_TYPE = "video/avc",H264的MIME类型，宽，高
//        val format = MediaFormat.createVideoFormat(MIME_TYPE, 360, 640)
//        format.setInteger(
//            MediaFormat.KEY_COLOR_FORMAT,
//            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
//        )//设置颜色格式
//        //设置比特率
//        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
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
//
//        mediaDecoder!!.configure(
//            format, null, null,
//            MediaCodec.CONFIGURE_FLAG_ENCODE
//        )//四个参数，第一个是media格式，第二个是解码器播放的surfaceview，第三个是MediaCrypto，第四个是编码解码的标识
//        mediaDecoder!!.start()
//        mEncodeInputBuffer = mediaDecoder?.inputBuffers
//        mEncodeOutputBuffer = mediaDecoder?.outputBuffers
//        mVideoExtractor.selectTrack(0)
//
//        var sawInputEOS = false
//        var sawOutputEOS = false
//        var sawOutputEOS2 = false
//        val info = MediaCodec.BufferInfo()
//        val encoderInfo = MediaCodec.BufferInfo()
//
////        while (!sawInputEOS || !sawOutputEOS || !sawOutputEOS2) {
////            if (!sawInputEOS) {
////                sawInputEOS = decodeInput(mVideoExtractor, mediaDecoder, mEncodeInputBuffer)
////            }
////
////            if (!sawOutputEOS) {
////                val outputBufIndex = mediaDecoder?.dequeueOutputBuffer(info, 0)
////                if (outputBufIndex != 0) {
////                    sawOutputEOS = decodeEncode(
////                        extractor,
////                        mediaDecoder,
////                        encoder,
////                        codecOutputBuffers,
////                        encoderInputBuffers,
////                        info,
////                        outputBufIndex
////                    )
////                } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
////                    Log.d(TAG, "decoding INFO_OUTPUT_BUFFERS_CHANGED")
////                    codecOutputBuffers = mediaDecoder?.outputBuffers
////                } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
////                    val oformat = mediaDecoder?.outputFormat
////                    Log.d(TAG, "decoding Output format has changed to $oformat")
////                } else if (outputBufIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
////                    Log.d(TAG, "decoding dequeueOutputBuffer timed out!")
////                }
////            }
////
////            if (!sawOutputEOS2) {
////                val encodingOutputBufferIndex = mediaDecoder?.dequeueOutputBuffer(encoderInfo, 0)
////                if (encodingOutputBufferIndex != null) {
////                    if (encodingOutputBufferIndex >= 0) {
////                        sawOutputEOS2 = encodeOuput(
////                            outputStream,
////                            mediaDecoder,
////                            encoderOutputBuffers,
////                            encoderInfo,
////                            encodingOutputBufferIndex
////                        )
////                    } else if (encodingOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
////                        Log.d(TAG, "encoding INFO_OUTPUT_BUFFERS_CHANGED")
////                        encoderOutputBuffers = mediaDecoder?.outputBuffers
////                    } else if (encodingOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
////                        val oformat = mediaDecoder?.getOutputFormat()
////                        Log.d(TAG, "encoding Output format has changed to $oformat")
////                    } else if (encodingOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
////                        Log.d(TAG, "encoding dequeueOutputBuffer timed out!")
////                    }
////                }
////            }
////        }
////    }
////
////    private fun decodeInput( extractor: MediaExtractor, decoder: MediaCodec, codecInputBuffers: Array<ByteBuffer> ): Boolean {
////        var sawInputEOS = false
////        val inputBufIndex = decoder.dequeueInputBuffer(0)
////        if (inputBufIndex >= 0) {
////            val dstBuf = codecInputBuffers[inputBufIndex]
//////            input1count++
////
////            var sampleSize = extractor.readSampleData(dstBuf, 0)
////            var presentationTimeUs: Long = 0
////            if (sampleSize < 0) {
////                sawInputEOS = true
////                sampleSize = 0
//////                Log.d(LOG_TAG, "done decoding input: #$input1count")
////            } else {
////                presentationTimeUs = extractor.sampleTime
////            }
////
////            decoder.queueInputBuffer(
////                inputBufIndex,
////                0,
////                sampleSize,
////                presentationTimeUs,
////                if (sawInputEOS) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
////            )
////            if (!sawInputEOS) {
////                extractor.advance()
////            }
////        }
////        return sawInputEOS
////    }
//    }
//}
