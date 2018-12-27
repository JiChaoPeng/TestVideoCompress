package com.wealoha.TestVideoCompress.video

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaExtractor
import android.util.Log
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer


/**
 * Created by jichaopeng
 * 2018/12/25
 */
class CompressVideo {
    @Throws(IOException::class)
    fun getCompressedFile(videoPath: String): File {
        val extractor = MediaExtractor()
        extractor.setDataSource(videoPath)
        val trackCount = extractor.trackCount

        val filePath = videoPath.substring(0, videoPath.lastIndexOf(File.separator))
        val splitByDot = videoPath.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var ext = ""
        if (splitByDot.size > 1)
            ext = splitByDot[splitByDot.size - 1]
        var fileName = videoPath.substring(
            videoPath.lastIndexOf(File.separator) + 1,
            videoPath.length
        )

        if (ext.isNotEmpty())
            fileName = fileName.replace(".$ext", "_out.$ext")
        else
            fileName += "_out"

        val outFile = File(filePath, fileName)
        if (!outFile.exists())
            outFile.createNewFile()
        val muxer = MediaMuxer(
            outFile.absolutePath,
            MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
        )
        val indexMap = HashMap<Int, Int>(trackCount)
        for (i in 0 until trackCount) {
            extractor.selectTrack(i)
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith("video/")) {
                val currWidth = format.getInteger(MediaFormat.KEY_WIDTH)
                val currHeight = format.getInteger(MediaFormat.KEY_HEIGHT)
                format.setInteger(MediaFormat.KEY_WIDTH, if (currWidth > currHeight) 960 else 540)
                format.setInteger(MediaFormat.KEY_HEIGHT, if (currWidth > currHeight) 540 else 960)
                //API19 MediaFormat.KEY_MAX_WIDTH and KEY_MAX_HEIGHT
                format.setInteger("max-width", format.getInteger(MediaFormat.KEY_WIDTH))
                format.setInteger("max-height", format.getInteger(MediaFormat.KEY_HEIGHT))
            }
            val dstIndex = muxer.addTrack(format)
            indexMap[i] = dstIndex
        }

        var sawEOS = false
        val bufferSize = 256 * 1024
        val offset = 0
        val dstBuf = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()
        muxer.start()
        while (!sawEOS) {
            bufferInfo.offset = offset
            try {
                bufferInfo.size = extractor.readSampleData(dstBuf, offset)
                Log.d("CompressVideo", "$dstBuf  $offset")
            } catch (e: Exception) {
                Log.d("CompressVideo", "${e.cause}  ${e.message}")
            }
            if (bufferInfo.size < 0) {
                sawEOS = true
                bufferInfo.size = 0
            } else {
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags
                val trackIndex = extractor.sampleTrackIndex
                indexMap[trackIndex]?.let {
                    muxer.writeSampleData(
                        it, dstBuf,
                        bufferInfo
                    )
                }
                extractor.advance()
            }
        }

        muxer.stop()
        muxer.release()
        return outFile
    }
}