package com.lhd.visualizer_record;

import androidx.lifecycle.MutableLiveData;

import com.lhd.visualizer_record.audiorecordingsdk.AudioChunk;
import com.lhd.visualizer_record.view.VisualizerView;

import java.io.File;

public class VisualizerRecorder implements OnAudioRecordListener {

    //region properties

    public static final int STATE_RECORD_IDLE = 0;
    public static final int STATE_RECORD_RUNNING = 1;
    public static final int STATE_PAUSE = 2;
    private MutableLiveData<Integer> liveStateRecord = new MutableLiveData<>(STATE_RECORD_IDLE);
    private AudioRecording audioRecording = null;
    private VisualizerView visualizerView;
    private String outputPath = "";
    private Listener listener;
    private long rawLength = 0;
    private long currentMillisDuration = 0;
    private long maxDurationRecord = -1;
    private int sampleRate = 0;
    private int channel = 0;

    //endregion

    public VisualizerRecorder() {
        liveStateRecord.setValue(STATE_RECORD_IDLE);
    }

    public void setSampleRate(int sampleRate){
        this.sampleRate = sampleRate;
    }

    public void setChannelCount(int channel){
        this.channel = channel;
    }
    //region action record

    public void startRecord(String outputPath) {
        if (audioRecording != null && audioRecording.isRecording()) {
            return;
        }
        setDefaultRecordInfo();
        if (audioRecording == null)
            audioRecording = new AudioRecording();
        audioRecording.setRecordSampleRate(sampleRate);
        audioRecording.setRecordChannels(channel);

        this.outputPath = outputPath;
        audioRecording.setOnAudioRecordListener(this);
        audioRecording.startRecording();
    }

    public void pauseRecord() {
        if (audioRecording == null)
            return;
        liveStateRecord.setValue(STATE_PAUSE);
        audioRecording.pauseRecord();
    }

    public boolean resumeRecord() {
        if (audioRecording == null)
            return false;
        if (maxDurationRecord > 0 && currentMillisDuration >= maxDurationRecord) {
            return false;
        }
        liveStateRecord.setValue(STATE_RECORD_RUNNING);
        audioRecording.resumeRecord();
        return true;
    }

    public void stopRecord() {
        if (audioRecording == null)
            return;
        setDefaultRecordInfo();
        if (liveStateRecord.getValue() != null && liveStateRecord.getValue() != STATE_RECORD_IDLE) {
            liveStateRecord.setValue(STATE_RECORD_IDLE);
        }
        audioRecording.stopRecording();
    }

    public void releaseRecord() {
        if (audioRecording == null)
            return;
        setDefaultRecordInfo();
        if (liveStateRecord.getValue() != null && liveStateRecord.getValue() != STATE_RECORD_IDLE) {
            liveStateRecord.setValue(STATE_RECORD_IDLE);
        }
        audioRecording.stopRecording();
        audioRecording = null;
    }

    //endregion

    //region Listener

    @Override
    public File onAudioStartRecord() {
        liveStateRecord.setValue(STATE_RECORD_RUNNING);
        File outputFile = new File(outputPath);
        File parentFile = outputFile.getParentFile();
        if (parentFile != null)
            parentFile.mkdirs();
        return outputFile;
    }

    @Override
    public void onAudioRecordStopped() {
        liveStateRecord.setValue(STATE_RECORD_IDLE);
    }

    @Override
    public void onAudioRecordRunning(AudioChunk audioChunk) {
        rawLength += audioChunk.readCount();
        currentMillisDuration = (long) (getCurrentAudioDurationFromRawLength(rawLength) * 1000L);
        if (maxDurationRecord > 0) {
            if (currentMillisDuration >= maxDurationRecord) {
                currentMillisDuration = maxDurationRecord;
                if (listener != null) {
                    listener.onRecordLimited();
                }
                pauseRecord();
            }
        }
        if (listener != null) {
            listener.onRecordDurationChange(currentMillisDuration);
        }
        if (visualizerView != null)
            visualizerView.addAmplitude((float) audioChunk.maxAmplitude());
    }

    @Override
    public void onAudioRecordResumed() {
        liveStateRecord.setValue(STATE_RECORD_RUNNING);
    }

    @Override
    public void onAudioRecordPaused() {
        liveStateRecord.setValue(STATE_PAUSE);
    }

    //endregion

    //region get set
    private void setDefaultRecordInfo() {
        currentMillisDuration = 0;
        rawLength = 0;
        if (visualizerView != null) {
            visualizerView.clear();
        }
    }

    private float getCurrentAudioDurationFromRawLength(long rawLength) {
        int channel = 1;
        int bitsPerSample = 16;
        return (float) (rawLength) / ((float) (AudioRecording.recordSampleRate * channel * bitsPerSample) / 8);
    }

    public MutableLiveData<Integer> getLiveStateRecord() {
        return liveStateRecord;
    }

    public void attachToVisualizerView(VisualizerView visualizerView) {
        this.visualizerView = visualizerView;
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public long getCurrentDuration() {
        return currentMillisDuration;
    }

    public long getMaxDurationRecord() {
        return maxDurationRecord;
    }

    public void setMaxDurationRecord(long maxDurationRecord) {
        this.maxDurationRecord = maxDurationRecord;
    }

    public boolean isRecording() {
        return liveStateRecord.getValue() != null && liveStateRecord.getValue() == STATE_RECORD_RUNNING;
    }

    //endregion

    //region Listener
    public interface Listener {
        void onRecordDurationChange(long durationInMillis);

        default public void onRecordLimited() {
        }
    }

    //endregion
}
