package com.kuemiin.reversevoice.pose.render

import android.content.Context
import android.text.TextUtils
import cn.ezandroid.lib.ezfilter.core.util.NumberUtil
import cn.ezandroid.lib.ezfilter.core.util.Path
import cn.ezandroid.lib.ezfilter.extra.sticker.model.Component
import java.io.File
import java.io.IOException
import java.util.Locale

/**
 * Sticker Kit Converter
 *
 * @author like
 * @date 2018-01-15
 */
object ComponentConvert {
    /**
     * Convert the read raw sticker component model into a more usable format
     *
     *
     * 1，Convert the read raw sticker component model into a more usable format
     * 2，Get and set the number of valid files in the material folder
     * 3，Get and set the resource file path list resources
     *
     * @param context
     * @param component
     * @param dir
     * @throws IOException
     */
    fun convert(context: Context, component: Component, dir: String) {
        // modify the resource file path to an "absolute" path for easy reading later
        component.src = dir + component.src
        var names: Array<String>? = null
        if (Path.ASSETS.belongsTo(component.src)) {
            try {
                names = context.assets.list(Path.ASSETS.crop(component.src))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (Path.FILE.belongsTo(component.src)) {
            names = File(Path.FILE.crop(component.src)).list()
        } else {
            names = File(component.src).list()
        }

        // avoid .DS_Store & Thumbs.db
        val paths: MutableList<String> = ArrayList()
        if (names != null) {
            for (name in names) {
                if (name.lowercase(Locale.getDefault()).contains(".DS_Store".lowercase(Locale.getDefault())) || name.lowercase(Locale.getDefault()).contains("Thumbs.db".lowercase(Locale.getDefault()))) {
                    continue
                }
                paths.add(component.src + "/" + name)
            }
        }

        component.length = paths.size

        // Sort by image file name
        paths.sortWith { o1: String, o2: String ->
            // Các số sau dấu gạch dưới _ và trước dấu thập phân.
            val sp1 = o1.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val sp2 = o2.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var num1 = sp1[sp1.size - 1]
            if (num1.contains(".")) {
                num1 = num1.substring(0, num1.indexOf("."))
            }
            if (!TextUtils.isDigitsOnly(num1)) {
                num1 = num1.substring(num1.length - 2)
            }
            var num2 = sp2[sp2.size - 1]
            if (num2.contains(".")) {
                num2 = num2.substring(0, num2.indexOf("."))
            }
            if (!TextUtils.isDigitsOnly(num2)) {
                num2 = num2.substring(num2.length - 2)
            }
            NumberUtil.parseInt(num1) - NumberUtil.parseInt(
                num2
            )
        }
        component.resources = paths
    }
}
