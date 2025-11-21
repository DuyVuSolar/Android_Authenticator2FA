/*
 * SPDX-FileCopyrightText: 2023 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */
package com.beemdevelopment.aegis.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    val id: Long = 0,
    var label: String,
    val uri: Uri,
    var relativePath: String,
    var isSelected: Boolean = false,
    var isLast: Boolean = false,
    var total: Int = 0,
) : Parcelable {


    companion object {
        const val PDF = "PDF"
        const val Word = "Word"
        const val Excel = "Excel"
        const val PPT = "PPT"
        const val TXT = "TXT"
        val AllAlbum = Album(
            id = -1,
            label = "",
            uri = Uri.EMPTY,
            relativePath = "",
            isSelected = true
        )
        val PDFAlbum = Album(
            id = -1,
            label = PDF,
            uri = Uri.EMPTY,
            relativePath = "",
            isSelected = false
        )
        val WordAlbum = Album(
            id = -1,
            label = Word,
            uri = Uri.EMPTY,
            relativePath = "",
            isSelected = false
        )
        val ExcelAlbum = Album(
            id = -1,
            label = Excel,
            uri = Uri.EMPTY,
            relativePath = "",
            isSelected = false
        )
        val PPTAlbum = Album(
            id = -1,
            label = PPT,
            uri = Uri.EMPTY,
            relativePath = "",
            isSelected = false
        )
        val TXTAlbum = Album(
            id = -1,
            label = TXT,
            uri = Uri.EMPTY,
            relativePath = "",
            isSelected = false
        )
        val listAlbumDefault = arrayListOf(PDFAlbum, WordAlbum, ExcelAlbum, PPTAlbum, TXTAlbum)

    }
}