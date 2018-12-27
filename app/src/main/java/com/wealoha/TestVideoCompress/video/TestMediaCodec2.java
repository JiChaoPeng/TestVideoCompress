package com.wealoha.TestVideoCompress.video;

import android.media.*;
import android.os.Environment;
import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jichaopeng
 * 2018/12/26
 * @author jichaopeng
 */
public class TestMediaCodec2 {
    private static final String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/123.mp4";
    private static final String mOutPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/123_out5.mp4";
    private MediaExtractor extractor;
    private MediaCodec decoder;
    private MediaCodec encoder;
    String mime;

    private static final String MIME_TYPE = "video/avc";

    public void extractMediaFile() {

        // work plan
        // locate media file
        // extract media file using Media Extractor
        // retrieve decoded frames

        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(mFilePath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // file not found
            e.printStackTrace();
        }

        // add decoded frames
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                extractor.selectTrack(i);
                try {
                    decoder = MediaCodec.createDecoderByType(mime);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                decoder.configure(format, null, null, 0);
                break;
            }
        }

        if (decoder == null) {
            Log.e("DecodeActivity", "Can't find video info!");
            return;
        }

        // - start decoder -
        decoder.start();
        extractor.selectTrack(0);

        // - decoded frames can obtain in here -

    }

    private void createsEncoder() {

        // creates media encoder to set formats
        try {
            encoder = MediaCodec.createDecoderByType(MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // init media format/* 640 *//* 480 */
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                320, 240);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 400000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        encoder.configure(mediaFormat, null, null,
                MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.start();
        ByteBuffer[] inputBuffers = encoder.getInputBuffers();

        // - encoded data format is avaiable in here

    }

    private void createMuxer() {

        // creates media muxer - media muxer will be used to write the final
        // strem in to a desired file :)

        try {
            MediaMuxer muxer = new MediaMuxer(mOutPath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            int videoTrackIndex = muxer.addTrack(encoder.getOutputFormat());

            //muxer.writeSampleData(videoTrackIndex, inputBuffers, bufferInfo);
            muxer.start();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
