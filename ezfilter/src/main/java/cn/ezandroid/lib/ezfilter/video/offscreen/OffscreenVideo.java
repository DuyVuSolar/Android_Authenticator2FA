package cn.ezandroid.lib.ezfilter.video.offscreen;

import android.graphics.SurfaceTexture;
import android.media.AudioFormat;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;

import java.io.IOException;
import java.util.List;

import cn.ezandroid.lib.ezfilter.core.FBORender;
import cn.ezandroid.lib.ezfilter.core.GLRender;
import cn.ezandroid.lib.ezfilter.core.RenderPipeline;
import cn.ezandroid.lib.ezfilter.media.transcode.AudioTrackTranscoder;
import cn.ezandroid.lib.ezfilter.media.transcode.IVideoRender;
import cn.ezandroid.lib.ezfilter.media.transcode.QueuedMuxer;
import cn.ezandroid.lib.ezfilter.media.transcode.VideoFBORender;
import cn.ezandroid.lib.ezfilter.media.transcode.VideoTrackTranscoder;
import cn.ezandroid.lib.ezfilter.media.util.MediaUtil;

/**
 * Off-screen rendering video
 *
 * @author like
 * @date 2017-09-24
 */
public class OffscreenVideo {

    private RenderPipeline mPipeline;

    private MediaExtractor mExtractor;
    private MediaUtil.Track mTrack;
    private String mVideoPath;
    private VideoFBORender mOffscreenRender;

    private IVideoRenderListener mVideoRenderListener;

    private int mWidth;
    private int mHeight;

    public interface IVideoRenderListener {

        /**
         * Current frame drawing callback
         *
         * @param time Current frame time (in nanoseconds)
         */
        void onFrameDraw(long time);
    }

    public OffscreenVideo(String videoPath) {
        mVideoPath = videoPath;

        initRenderSize();
    }

    private void initRenderSize() {
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(mVideoPath);
            // Read audio and video tracks
            mTrack = MediaUtil.getFirstTrack(mExtractor);
            if (null == mTrack || null == mTrack.videoTrackFormat) {
                return;
            }
            int w = mTrack.videoTrackFormat.getInteger(MediaFormat.KEY_WIDTH);
            int h = mTrack.videoTrackFormat.getInteger(MediaFormat.KEY_HEIGHT);

            // Correct the angle, you may need to swap width and height
            int degrees = 0;
            if (mTrack.videoTrackFormat.containsKey(MediaUtil.KEY_ROTATION)) {
                degrees = mTrack.videoTrackFormat.getInteger(MediaUtil.KEY_ROTATION);
            } else {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    retriever.setDataSource(mVideoPath);
                    String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                    degrees = Integer.parseInt(rotation);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } finally {
                    retriever.release();
                }
            }
            if (degrees == 90 || degrees == 270) {
                // Swap width and height
                w = w ^ h;
                h = w ^ h;
                w = w ^ h;
            }

            mWidth = w;
            mHeight = h;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPipeline() {
        if (mPipeline != null) {
            return;
        }
        mOffscreenRender = new VideoFBORender();
        mOffscreenRender.setRenderSize(mWidth, mHeight);
        mPipeline = new RenderPipeline();
        mPipeline.onSurfaceCreated(null, null);
        mPipeline.setStartPointRender(mOffscreenRender);
        mPipeline.addEndPointRender(new GLRender());
    }

    public void setVideoRenderListener(IVideoRenderListener videoRenderListener) {
        mVideoRenderListener = videoRenderListener;
    }

    public void addFilterRender(FBORender filterRender) {
        initPipeline();
        mPipeline.addFilterRender(filterRender);
    }

    public void removeFilterRender(FBORender filterRender) {
        initPipeline();
        mPipeline.removeFilterRender(filterRender);
    }

    public List<FBORender> getFilterRenders() {
        initPipeline();
        return mPipeline.getFilterRenders();
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    private int getInteger(String name, int defaultValue) {
        try {
            return mTrack.audioTrackFormat.getInteger(name);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private VideoTrackTranscoder initVideoTrack(MediaFormat videoFormat, QueuedMuxer queuedMuxer) {
        return new VideoTrackTranscoder(mExtractor, mTrack.videoTrackIndex,
                videoFormat, queuedMuxer, new IVideoRender() {
            @Override
            public void drawFrame(long time) {
                if (mVideoRenderListener != null) {
                    mVideoRenderListener.onFrameDraw(time);
                }
                mOffscreenRender.drawFrame(time);
            }

            @Override
            public SurfaceTexture getSurfaceTexture() {
                return mOffscreenRender.getSurfaceTexture();
            }
        });
    }

    private AudioTrackTranscoder initAudioTrack(MediaFormat audioFormat, QueuedMuxer queuedMuxer) {
        return new AudioTrackTranscoder(mExtractor, mTrack.audioTrackIndex,
                audioFormat, queuedMuxer);
    }

    public void save(String output) throws IOException {
        save(output, mWidth, mHeight);
    }

    public void save(String output, int width, int height) throws IOException {
        if (null == mTrack || null == mTrack.videoTrackFormat) {
            return;
        }
        initPipeline();
        mPipeline.onSurfaceChanged(null, width, height);
        mPipeline.startRender();

        // Định dạng video đảm bảo tốc độ bit đầu vào và đầu ra không thay đổi để tránh video bị mờ sau khi xử lý
        MediaFormat videoFormat = MediaUtil.createVideoFormat(width, height,
                MediaUtil.getMetadata(mVideoPath).bitrate, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        // Initialize the transcoder
        MediaMuxer muxer = new MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        QueuedMuxer queuedMuxer = new QueuedMuxer(muxer);

        if (null != mTrack.audioTrackFormat) {
            int sampleRate = getInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
            int channelMask = getInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_STEREO);
            int channelCount = getInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);

            // Audio Format
            MediaFormat audioFormat = MediaUtil.createAudioFormat(sampleRate, channelMask, channelCount);

            // Both audio and video tracks are required
            queuedMuxer.setTrackCount(QueuedMuxer.TRACK_VIDEO & QueuedMuxer.TRACK_AUDIO);
            VideoTrackTranscoder videoTrack = initVideoTrack(videoFormat, queuedMuxer);
            AudioTrackTranscoder audioTrack = initAudioTrack(audioFormat, queuedMuxer);

            videoTrack.setup();
            audioTrack.setup();

            while (!videoTrack.isFinished() || !audioTrack.isFinished()) {
                boolean stepped = videoTrack.stepPipeline() || audioTrack.stepPipeline();
                if (!stepped) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // nothing to do
                    }
                }
            }

            mPipeline.onSurfaceDestroyed(); // Need to be called before videoTrack.release();
            videoTrack.release();
            audioTrack.release();
        } else {
            // Only video track required
            queuedMuxer.setTrackCount(QueuedMuxer.TRACK_VIDEO);
            VideoTrackTranscoder videoTrack = initVideoTrack(videoFormat, queuedMuxer);

            videoTrack.setup();

            while (!videoTrack.isFinished()) {
                boolean stepped = videoTrack.stepPipeline();
                if (!stepped) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // nothing to do
                    }
                }
            }

            mPipeline.onSurfaceDestroyed(); // Need to be called before videoTrack.release();
            videoTrack.release();
        }

        muxer.stop();
        muxer.release();

        mExtractor.release();
    }
}
