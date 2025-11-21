@file:Suppress("DEPRECATION")

package com.beemdevelopment.aegis.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.beemdevelopment.aegis.ui_old.dialog.permission.GrantPermissionDialog

fun Context.goToSettingsApplication() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setData(Uri.fromParts("package", packageName, null))
    startActivity(intent)
}


object PermissionUtils {
//    private var confirmDialogPer: DialogPermission? = null
    var isRequestingPermission = false
    var onAllow: (() -> Unit)? = null
    var onDenied: (() -> Unit)? = null
    var REQUEST_PERMISSION = 5
    var isGotoSetting = false
    private var mDialogPermission : GrantPermissionDialog? = null
    private val permissionRecord = arrayListOf(Manifest.permission.RECORD_AUDIO)
    private val permissionCamera = arrayListOf(Manifest.permission.CAMERA)
    private var KEY_SAVE_HAS_PERMISSION = "KEY_SAVE_HAS_PERMISSION"
    var hasPermissionCamera = true

    fun isHasPermissionCamera(activity: Context): Boolean {
        return activity.getSharedPreferences("SharedPreferences_Name", Context.MODE_PRIVATE)
            .getBoolean(KEY_SAVE_HAS_PERMISSION, false)
    }

    fun setIsPermissionCamera(activity: Activity, value : Boolean) {
        val sharePreference = activity.getSharedPreferences("SharedPreferences_Name", Context.MODE_PRIVATE)
        sharePreference.edit().putBoolean(KEY_SAVE_HAS_PERMISSION, value).apply()
    }


    fun requestPermissionRecord(activity: Activity?, showDialog : Boolean = false, onGrant: () -> Unit, onDeny: (setting: Boolean) -> Unit = {}) {
        Dexter.withActivity(activity)
            .withPermissions(permissionRecord)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (activity?.isFinishing == false) {
                        if (report?.areAllPermissionsGranted() == true) {
                            dismissDialog()
                            onGrant()
                        } else if (report?.deniedPermissionResponses?.isEmpty() == false || report?.isAnyPermissionPermanentlyDenied == true) {
                            if(showDialog) showDialogPermission(activity, onDeny)
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
//                        if (!isGotoSetting)
                    token?.continuePermissionRequest()
                }

            })
            .withErrorListener {
                loge(it)
                isGotoSetting = false
            }
            .onSameThread()
            .check()
    }

    fun requestPermissionCamera(activity: Activity?, onGrant: () -> Unit, onDeny: () -> Unit = {}) {
        Dexter.withActivity(activity)
            .withPermissions(permissionCamera)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (activity?.isFinishing == false) {
                        if (report?.areAllPermissionsGranted() == true) {
                            dismissDialog()
                            onGrant()
                        } else if (report?.deniedPermissionResponses?.isEmpty() == false || report?.isAnyPermissionPermanentlyDenied == true) {
                            onDeny.invoke()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
//                        if (!isGotoSetting)
                    token?.continuePermissionRequest()
                }

            })
            .withErrorListener {
                loge(it)
                isGotoSetting = false
            }
            .onSameThread()
            .check()
    }

    fun checkPermissionFull(activity: FragmentActivity?, onGrant: () -> Unit, onDeny: (setting: Boolean) -> Unit = {}, isNeedGotToSetting : Boolean = true) {
        Dexter.withActivity(activity)
            .withPermissions(arrayListOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (activity?.isFinishing == false) {
                        if (report?.areAllPermissionsGranted() == true) {
                            dismissDialog()
                            onGrant()
                        } else if (report?.deniedPermissionResponses?.isEmpty() == false || report?.isAnyPermissionPermanentlyDenied == true) {
                            onDeny(false)
                            if(isNeedGotToSetting){
                                isGotoSetting = true
                                activity.goToSettingsApplication()
                            }
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
//                        if (!isGotoSetting)
                    token?.continuePermissionRequest()
                }

            })
            .withErrorListener {
                loge(it)
                isGotoSetting = false
            }
            .onSameThread()
            .check()
    }

    fun requestPermissionNotify(activity: Activity?,onGrant: () -> Unit, onDeny: () -> Unit = {}) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withActivity(activity)
                .withPermissions(arrayListOf(Manifest.permission.POST_NOTIFICATIONS))
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (activity?.isFinishing == false) {
                            if (report?.areAllPermissionsGranted() == true) {
                                dismissDialog()
                                onGrant.invoke()
                            } else if (report?.deniedPermissionResponses?.isEmpty() == false || report?.isAnyPermissionPermanentlyDenied == true) {
                                onDeny.invoke()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
    //                        if (!isGotoSetting)
                        token?.continuePermissionRequest()
                    }

                })
                .withErrorListener {
                    loge(it)
                    isGotoSetting = false
                }
                .onSameThread()
                .check()
        }
    }

    fun checkHasPerNotify(activity: Activity) : Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayListOf(Manifest.permission.POST_NOTIFICATIONS).forEach {
                if (activity.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) {
                    return false
                }
            }
        }
        return true
    }
    fun checkHasPerRecord(activity: Activity) : Boolean{
        arrayListOf(Manifest.permission.RECORD_AUDIO).forEach {
            if (activity.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    fun checkHasPerCameraAndRecord(activity: Activity) : Boolean{
        arrayListOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).forEach {
            if (activity.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) {
                hasPermissionCamera = false
                return false
            }
        }
        hasPermissionCamera = true
        return true
    }

    fun checkHasPerCamera(activity: Activity) : Boolean{
        arrayListOf(Manifest.permission.CAMERA).forEach {
            if (activity.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    fun dismissDialog() {
        try {
            mDialogPermission?.dismiss()
        } catch (_: WindowManager.BadTokenException) {
        } catch (_: java.lang.Exception) {
        }
    }

//    fun showDialog() {
//        try {
//            if (confirmDialogPer?.isShowing == false) confirmDialogPer?.show()
//        } catch (e: WindowManager.BadTokenException) {
//        } catch (e: Exception) {
//        }
//    }

    fun showDialogPermission(activity: Activity, onDeny: (setting: Boolean) -> Unit) {
        try {
            if(mDialogPermission == null) mDialogPermission = GrantPermissionDialog(activity)
            mDialogPermission?.apply {
                setUpData {
                    isGotoSetting = true
                    onDeny(true)
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = uri
                    activity.startActivityForResult(intent, REQUEST_PERMISSION)
                }
                show()
            }

            mDialogPermission?.show()
        } catch (e: WindowManager.BadTokenException) {
            loge("$e")
        } catch (e: Exception) {
            loge("$e")
        }
    }
}