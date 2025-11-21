package com.beemdevelopment.aegis.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class EncoderModel(
    var pathVideo : String = "",
    var widthBackground : Int = 0,
    var heightBackground : Int = 0,

    var typeFilter : String = "",
    var rotation : Int = 0,
    var isSelected: Boolean = false,
    var id : String = UUID.randomUUID().toString(),
    var date: Long = System.currentTimeMillis(),
) : BaseModel(), Parcelable {
    companion object {
//        fun from(user: EncoderModel): EncoderModel {
//            return EncoderModel(
//                id = user.id,
//                name = user.name,
//                source = user.source,
//                thumb = user.thumb,
//                isSelected = user.isSelected,
//            )
//        }
    }
}
