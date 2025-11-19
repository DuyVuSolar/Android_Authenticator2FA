package com.kuemiin.reversevoice.model

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore
import com.kuemiin.reversevoice.di.MediaInfo
import com.kuemiin.reversevoice.utils.FileUtils
import com.kuemiin.reversevoice.utils.FileUtils.REVERSE_AUDIO
import com.kuemiin.reversevoice.utils.binding.longDurationToString
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

data class AppAudio(
        @MediaInfo(MediaStore.Audio.AudioColumns._ID)
        var id: Long = -1,
        @MediaInfo(MediaStore.Audio.AudioColumns.DATA)
        var path: String = "",
        @MediaInfo(MediaStore.Audio.AudioColumns.DURATION)
        var duration: Long = 0,
        @MediaInfo(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
        var date: Long = 0,

        var isPlayingNormal : Boolean = false,
        var isPlayingReverse : Boolean = false
) : BaseModel(), Cloneable {

    public override fun clone(): AppAudio {
        val clone: AppAudio = try {
            super.clone() as AppAudio
        } catch (e: CloneNotSupportedException) {
            AppAudio()
        }
        return clone
    }

    var fileUri: Uri? = null

    @MediaInfo(MediaStore.Audio.Media.RELATIVE_PATH)
    var relativePath: String = ""

    @MediaInfo(MediaStore.Audio.AudioColumns.ARTIST)
    var artist: String = ""

    var displayName = getNameWithoutExtension()

    var selected = false


    constructor(audio: AppAudio) : this(audio.id, audio.path, audio.duration, audio.date) {
        this.fileUri = audio.fileUri
    }

    fun getDisplayNameVisible() = if (displayName.isEmpty()) getNameWithoutExtension() else displayName

    fun getNameWithoutExtension(): String {
        return File(path).nameWithoutExtension
    }

    fun getNameWithExtension(): String {
        return File(path).name
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateCreateFormat(): String {
        val today = Date(date)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        return dateFormat.format(today)
    }
    fun getStringDuration(): String {
        return FileUtils.convertsTime(duration, minHourFormat = false, isCanBeZero = false)
    }

    fun getStringDurationWithHour(): String {
        return FileUtils.convertsTime(duration, minHourFormat = true, isCanBeZero = false)
    }

    fun getUri(): Uri {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    fun getDetailInformation(): String {
        return "${duration.longDurationToString()}     |    ${File(path).length() / 1024} kB"
    }

    override fun toString(): String {
        return "AppAudio(id=$id, path='$path', duration=$duration, date=$date, fileUri=$fileUri, relativePath='$relativePath', artist='$artist')"
    }

    fun getPathReverse(): String {
        val parent = FileUtils.getFolderTempReverse()
        val fileReverse = File(parent, File(path).nameWithoutExtension + REVERSE_AUDIO + "." + File(path).extension)
        return fileReverse.absolutePath
    }

}