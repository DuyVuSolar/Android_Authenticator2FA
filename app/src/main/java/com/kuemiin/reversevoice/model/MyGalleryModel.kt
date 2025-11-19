package com.kuemiin.reversevoice.model

import android.media.MediaMetadataRetriever
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kuemiin.reversevoice.utils.binding.longDurationToString
import kotlinx.parcelize.Parcelize
import java.io.File

@Entity(tableName = "my_design")
@Parcelize
data class MyGalleryModel(
    var currentVideo : String = "",
    var typeFilter : String = "",
    var currentPathBackground : String = "",

    var pathRingTone : String = "",

    var iconRejectRes : Int = 0,
    var iconAcceptRes : Int = 0,

    var isSelected: Boolean = false,
    var isSelecting: Boolean = false,
    var duration: Long = 0,
    var date: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var isApplied: Boolean = false,
    var isDefault: Boolean = false,
) : BaseModel(), Parcelable, Cloneable {

    var avatarRes : Int = 0
    public override fun clone(): MyGalleryModel {
        val clone: MyGalleryModel = try {
            super.clone() as MyGalleryModel
        } catch (e: CloneNotSupportedException) {
            MyGalleryModel()
        }
        return clone
    }

    operator fun compareTo(o: Any): Int {
        val compare: MyGalleryModel = o as MyGalleryModel
        return if (compare.id == this.id
            && compare.isSelecting == this.isSelecting
            && compare.isSelected == this.isSelected
            && compare.duration == this.duration
            && compare.currentVideo == this.currentVideo
            && compare.typeFilter == this.typeFilter
            && compare.currentPathBackground == this.currentPathBackground
            && compare.date == this.date) {
            0
        } else 1
    }

    fun getAudioDuration(path: String): Long {
        val mmr = MediaMetadataRetriever()
        var duration = 0L
        try {
            mmr.setDataSource(path)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = java.lang.Long.parseLong(durationStr)
        } catch (e: Exception) {

        }
        mmr.release()
        return duration
    }


    fun getDetailInformation(): String {
        return "${duration.longDurationToString()}"
    }
}


fun createGalleryEmpty() : MyGalleryModel{
    return MyGalleryModel()
}

