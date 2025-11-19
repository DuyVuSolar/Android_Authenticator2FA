package com.lhd.visualizer_record;

import com.lhd.visualizer_record.audiorecordingsdk.AudioChunk;

import java.io.File;

public interface OnAudioRecordListener {
    File onAudioStartRecord();

    void onAudioRecordStopped();

    void onAudioRecordRunning(AudioChunk audioChunk);

    void onAudioRecordResumed();

    void onAudioRecordPaused();
}
