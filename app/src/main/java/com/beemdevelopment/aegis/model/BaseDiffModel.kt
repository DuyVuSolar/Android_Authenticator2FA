package com.beemdevelopment.aegis.model

import android.os.Parcelable
import kotlin.random.Random

@Suppress("PLUGIN_IS_NOT_ENABLED")
@kotlinx.parcelize.Parcelize
open class BaseDiffModel(
    var uuid : Int = Random.nextInt(10000),
    var isSelected : Boolean = false
): Cloneable, Parcelable {

    public override fun clone(): BaseDiffModel {
        val clone: BaseDiffModel = try {
            super.clone() as BaseDiffModel
        } catch (e: CloneNotSupportedException) {
            BaseDiffModel()
        }
        return clone
    }

    open fun compareTo(o: Any): Boolean = false
}