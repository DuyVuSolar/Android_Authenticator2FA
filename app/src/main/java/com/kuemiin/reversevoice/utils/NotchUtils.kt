package com.kuemiin.reversevoice.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.os.Build
import android.view.View
import com.kuemiin.base.extension.STATUS_BAR_HEIGHT
import com.kuemiin.base.extension.buildVersion
import com.kuemiin.reversevoice.utils.extension.isBuildLargerThan
import java.lang.reflect.InvocationTargetException


object NotchUtils {
    private const val NOTCH_HUA_WEI = "com.huawei.android.util.HwNotchSizeUtil"
    private const val NOTCH_OPPO = "com.oppo.feature.screen.heteromorphism"
    private const val NOTCH_VIVO = "android.util.FtFeature"
    private const val NOTCH_XIAO_MI = "ro.miui.notch"
    private const val NOTCH_LENOVO = "config_screen_has_notch"
    private const val NOTCH_MEIZU = "flyme.config.FlymeFeature"
    private const val SYSTEM_PROPERTIES = "android.os.SystemProperties"

    private fun Activity?.hasNotchScreen(): Boolean {
        return if (this != null) {
            if (isBuildLargerThan(buildVersion.P)) {
                hasNotchAtAndroidP()
            } else {
                hasNotchAtXiaoMi() ||
                        hasNotchAtHuaWei() ||
                        hasNotchAtOPPO() ||
                        hasNotchAtVIVO() ||
                        hasNotchAtLenovo() ||
                        hasNotchAtMeiZu()
            }
        } else {
            false
        }
    }

    fun View?.hasNotchScreen(): Boolean {
        return if (this != null) {
            if (isBuildLargerThan(buildVersion.P)) {
                hasNotchAtAndroidP()
            } else {
                context.hasNotchAtXiaoMi() ||
                        context.hasNotchAtHuaWei() ||
                        context.hasNotchAtOPPO() ||
                        context.hasNotchAtVIVO()
            }
        } else {
            false
        }
    }

    private fun View?.hasNotchAtAndroidP(): Boolean {
        if (isBuildLargerThan(buildVersion.P)) {
            return this?.rootWindowInsets?.displayCutout != null
        }
        return false
    }

    private fun Activity?.hasNotchAtAndroidP(): Boolean {
        if (isBuildLargerThan(buildVersion.P)) {
            return this?.window?.decorView?.rootWindowInsets?.displayCutout != null
        }
        return false
    }

    @SuppressLint("PrivateApi")
    private fun Context?.hasNotchAtXiaoMi(): Boolean {
        var result = 0
        if (Build.MANUFACTURER.lowercase().contains("xiaomi")) {
            try {
                val classLoader = this?.classLoader
                val aClass = classLoader?.loadClass(com.kuemiin.reversevoice.utils.NotchUtils.SYSTEM_PROPERTIES)
                val method = aClass?.getMethod(
                    "getInt", String::class.java,
                    Int::class.javaPrimitiveType
                )
                result = method?.invoke(aClass,
                    com.kuemiin.reversevoice.utils.NotchUtils.NOTCH_XIAO_MI, 0) as Int
            } catch (ignored: NoSuchMethodException) {

            } catch (ignored: IllegalAccessException) {

            } catch (ignored: InvocationTargetException) {

            } catch (ignored: ClassNotFoundException) {

            }
        }
        return result == 1
    }

    fun Context?.hasNotchAtHuaWei(): Boolean {
        var result = false
        if (Build.MANUFACTURER.lowercase().contains("huawei")) {
            try {
                val classLoader = this?.classLoader
                val aClass = classLoader?.loadClass(com.kuemiin.reversevoice.utils.NotchUtils.NOTCH_HUA_WEI)
                val method = aClass?.getMethod("hasNotchInScreen")
                result = method?.invoke(aClass) as Boolean
            } catch (ignored: ClassNotFoundException) {

            } catch (ignored: NoSuchMethodException) {

            } catch (ignored: Exception) {

            }
        }
        return result
    }

    @SuppressLint("PrivateApi")
    private fun Context?.hasNotchAtVIVO(): Boolean {
        var result = false
        if (Build.MANUFACTURER.lowercase().contains("vivo")) {
            try {
                val classLoader = this?.classLoader
                val aClass = classLoader?.loadClass(com.kuemiin.reversevoice.utils.NotchUtils.NOTCH_VIVO)
                val method = aClass?.getMethod("isFeatureSupport", Int::class.javaPrimitiveType)
                result = method?.invoke(aClass, 0x00000020) as Boolean
            } catch (ignored: ClassNotFoundException) {

            } catch (ignored: NoSuchMethodException) {

            } catch (ignored: Exception) {

            }
        }
        return result
    }

    private fun Context?.hasNotchAtOPPO(): Boolean {
        if (Build.MANUFACTURER.lowercase().contains("oppo")) {
            return try {
                this?.packageManager?.hasSystemFeature(com.kuemiin.reversevoice.utils.NotchUtils.NOTCH_OPPO) ?: false
            } catch (ignored: Exception) {
                false
            }
        }
        return false
    }

    @SuppressLint("DiscouragedApi")
    private fun Context?.hasNotchAtLenovo(): Boolean {
        if (Build.MANUFACTURER.lowercase().contains("lenovo")) {
            val resourceId = this?.resources?.getIdentifier(com.kuemiin.reversevoice.utils.NotchUtils.NOTCH_LENOVO, "bool", "android") ?: 0
            if (resourceId > 0) {
                return this?.resources?.getBoolean(resourceId) ?: false
            }
        }
        return false
    }

    private fun Context?.hasNotchAtMeiZu(): Boolean {
        if (Build.MANUFACTURER.lowercase().contains("meizu")) {
            return try {
                val clazz = Class.forName(com.kuemiin.reversevoice.utils.NotchUtils.NOTCH_MEIZU)
                val field = clazz.getDeclaredField("IS_FRINGE_DEVICE")
                field.get(null) as Boolean
            } catch (e: Exception) {
                false
            }
        }
        return false
    }


    private fun Context?.getActivity(): Activity? {
        if (this == null) return null
        else if (this is ContextWrapper) {
            return if (this is Activity) {
                this
            } else {
                this.baseContext.getActivity()
            }
        }
        return null
    }

    @SuppressLint("DiscouragedApi")
    fun Context.getInternalDimensionSize(key: String): Int {
        try {
            val resourceId =
                Resources.getSystem().getIdentifier(key, "dimen", "android")
            if (resourceId > 0) {
                val sizeOne: Int = resources.getDimensionPixelSize(resourceId)
                val sizeTwo: Int = Resources.getSystem().getDimensionPixelSize(resourceId)
                return if (sizeTwo >= sizeOne && !(isBuildLargerThan(buildVersion.Q) &&
                            key != STATUS_BAR_HEIGHT)
                ) {
                    sizeTwo
                } else {
                    val densityOne: Float = resources.displayMetrics.density
                    val densityTwo = Resources.getSystem().displayMetrics.density
                    val f = sizeOne * densityTwo / densityOne
                    (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
                }
            }
        } catch (ignored: Resources.NotFoundException) {
            return 0
        }
        return 0
    }

    @SuppressLint("DiscouragedApi")
    private fun Context?.getXiaoMiNotchHeight(): Int {
        val resourceId = this?.resources?.getIdentifier("notch_height", "dimen", "android") ?: 0
        return if (resourceId > 0) {
            this?.resources?.getDimensionPixelSize(resourceId) ?: 0
        } else {
            0
        }
    }

    private fun Context?.getHuaWeiNotchSize(): IntArray {
        val ret = intArrayOf(0, 0)
        return try {
            val cl = this?.classLoader
            val aClass = cl?.loadClass("com.huawei.android.util.HwNotchSizeUtil")
            val get = aClass?.getMethod("getNotchSize")
            get?.invoke(aClass) as IntArray
        } catch (ignored: ClassNotFoundException) {
            ret
        } catch (ignored: NoSuchMethodException) {
            ret
        } catch (ignored: Exception) {
            ret
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun Context?.getLenovoNotchHeight(): Int {
        val resourceId: Int = this?.resources?.getIdentifier("notch_h", "dimen", "android") ?: 0
        return if (resourceId > 0) {
            this?.resources?.getDimensionPixelSize(resourceId) ?: 0
        } else {
            0
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun Context?.getMeizuNotchHeight(): Int {
        var notchHeight = 0
        val resourceId: Int =
            this?.resources?.getIdentifier("fringe_height", "dimen", "android") ?: 0
        if (resourceId > 0) {
            notchHeight = this?.resources?.getDimensionPixelSize(resourceId) ?: 0
        }
        return notchHeight
    }
}
