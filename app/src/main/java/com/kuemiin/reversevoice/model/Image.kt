/*
 * SPDX-FileCopyrightText: 2023 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */
package com.kuemiin.reversevoice.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Image(
    val id: Long = 0,
    val label: String,
    val uri: Uri,
    var relativePath: String,
    var count: Long = 0,
    var size: Long = 0,
    var isPinned: Boolean = false,
    var index : Int = 1,
    var uuid : String = UUID.randomUUID().toString(),
    var bitmap: Bitmap? = null
) : Parcelable, Cloneable {

    public override fun clone(): Image {
        val clone: Image = try {
            super.clone() as Image
        } catch (e: CloneNotSupportedException) {
            Image(
                id = -1,
                label = "Image",
                uri = Uri.EMPTY,
                relativePath = "",
            )
        }
        clone.bitmap = null
        return clone
    }

    operator fun compareTo(o: Any): Int {
        val compare: Image = o as Image
        return if (compare.id == this.id
            && compare.label == label
            && compare.relativePath == this.relativePath
            && compare.index == this.index
        ) {
            0
        } else 1
    }

    companion object {

        val EmptyImage = Image(
            id = -1,
            label = "Image",
            uri = Uri.EMPTY,
            relativePath = "",
        )
    }
}