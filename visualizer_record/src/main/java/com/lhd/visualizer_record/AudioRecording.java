package com.lhd.visualizer_record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.lhd.visualizer_record.audiorecordingsdk.AudioRecordConfig;
import com.lhd.visualizer_record.audiorecordingsdk.OmRecorder;
import com.lhd.visualizer_record.audiorecordingsdk.PullTransport;
import com.lhd.visualizer_record.audiorecordingsdk.PullableSource;
import com.lhd.visualizer_record.audiorecordingsdk.Recorder;

import java.io.File;
import java.io.IOException;

/**
 * Created by matejv on 22.11.2016.
 */

public class AudioRecording {
    public static int recordSampleRate = 44100;
    private int recordChannels = AudioFormat.CHANNEL_IN_STEREO;
    private int recordEncodeType = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferElements2Rec = 4096; // want to play 2048 (2K) since 2 bytes we use only 1024
    private int bytesPerElement = 2; // 2 bytes in 16bit format
    private boolean isRecording = false;

        private MediaRecorder mediaRecorder = null;
    private Recorder recorder = null;
    private Thread recordingThread = null;
    private OnAudioRecordListener onAudioRecordListener;

    public AudioRecording() {
        int bufferSize = AudioRecord.getMinBufferSize(recordSampleRate,
                recordChannels, recordEncodeType);
    }

    public int getRecordSampleRate() {
        return recordSampleRate;
    }

    public void setRecordSampleRate(int recordSampleRate) {
        this.recordSampleRate = recordSampleRate;
    }

    public int getRecordChannels() {
        return recordChannels;
    }

    public void setRecordChannels(int recordChannels) {
        this.recordChannels = recordChannels;
    }

    public int getRecordEncodeType() {
        return recordEncodeType;
    }

    public void setRecordEncodeType(int recordEncodeType) {
        this.recordEncodeType = recordEncodeType;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public int getBufferElements2Rec() {
        return bufferElements2Rec;
    }

    public void setBufferElements2Rec(int bufferElements2Rec) {
        this.bufferElements2Rec = bufferElements2Rec;
    }

    public int getBytesPerElement() {
        return bytesPerElement;
    }

    public void setBytesPerElement(int bytesPerElement) {
        this.bytesPerElement = bytesPerElement;
    }

    public void pauseRecord() {
        if (recorder != null) {
            recorder.pauseRecording();
            isRecording = false;
            if (onAudioRecordListener != null)
                onAudioRecordListener.onAudioRecordPaused();
        }
    }

    public void resumeRecord() {
        if (recorder != null) {
            recorder.resumeRecording();
            isRecording = true;
            if (onAudioRecordListener != null)
                onAudioRecordListener.onAudioRecordResumed();
        }
    }

    public void startRecording() {
        File file = null;
        if (onAudioRecordListener != null)
            file = onAudioRecordListener.onAudioStartRecord();
        if (file == null) {
            stopRecording();
            return;
        }
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), audioChunk -> {
                    if (onAudioRecordListener != null)
                        onAudioRecordListener.onAudioRecordRunning(audioChunk);
                }), file);
        isRecording = true;
        recorder.startRecording();
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, recordEncodeType,
                        recordChannels, recordSampleRate
                )
        );
    }

    public void stopRecording() {
        // stops the recording activity
        if (recorder != null) {
            isRecording = false;

            try {
                recorder.stopRecording();

                recorder = null;
            } catch (IOException e) {
            }
            try {
                recordingThread.stop();
            } catch (Exception ignored) {
            }
            recordingThread = null;
        }
        if (onAudioRecordListener != null)
            onAudioRecordListener.onAudioRecordStopped();

    }

    public void stopRecordingWithoutCB() {
        // stops the recording activity
        if (recorder != null) {
            isRecording = false;

            try {
                recorder.stopRecording();

                recorder = null;
            } catch (IOException e) {
            }
            try {
                recordingThread.stop();
            } catch (Exception ignored) {
            }
            recordingThread = null;
        }

    }

    public void setOnAudioRecordListener(OnAudioRecordListener onAudioRecordListener) {
        this.onAudioRecordListener = onAudioRecordListener;
    }
}