package com.kuemiin.reversevoice.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.kuemiin.reversevoice.utils.extension.isBuildLargerThan
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.utils.LocaleHelper
import com.kuemiin.reversevoice.utils.PermissionUtils
import com.kuemiin.reversevoice.utils.extension.toast

abstract class BaseActivity<BD : ViewDataBinding> : AppCompatActivity() {

    // region Const and Fields
    protected lateinit var binding: BD
    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    // endregion

    override fun attachBaseContext(newBase: Context) {
        val newContext = LocaleHelper.onAttach(newBase)
        val newOverrideConfiguration = Configuration(newContext.resources?.configuration).apply { fontScale = 1.0f }
        applyOverrideConfiguration(newOverrideConfiguration)
        super.attachBaseContext(newContext)
    }

    // region override function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.lifecycleOwner = this
        initViews()
        initEvents()
        changeFullScreen()
    }

    fun showMessage(message: String?) {
        message?.let { toast(it) }
    }
    // endregion

    // region abstract function
    abstract fun getLayoutId(): Int
    // endregion

    // region protected function
    protected open fun initViews() {}

    protected open fun initEvents() {}

    open fun enableDarkMode(): Boolean = false
    open fun isPreviewFull(): Boolean = true
    open fun isFullScreen(): Boolean = true

    private fun changeFullScreen() {
//        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        if (isFullScreen()) {
            window?.apply {
                if (isBuildLargerThan(Build.VERSION_CODES.R)) {
                    setDecorFitsSystemWindows(false)
                    insetsController?.hide(WindowInsets.Type.statusBars())
                    insetsController?.hide(WindowInsets.Type.navigationBars())
                    insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_FULLSCREEN)
                }
                this.statusBarColor = this.statusBarColor
            }
        } else if (isPreviewFull()) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_STABLE,
//                WindowManager.LayoutParams.FLAG_LAYOUT_STABLE)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )//bug : action editext show outside

            // Apply the updated layout parameters to the window
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or window.decorView.systemUiVisibility)

            val colorStatus =
                if (enableDarkMode()) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and colorStatus
            setSystemBarAppearance(
                isAppearanceLightStatusBars = true,
                isAppearanceLightNavigationBars = true
            )

        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val colorStatus =
                if (enableDarkMode()) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and colorStatus
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.statusBarColor = ContextCompat.getColor(this, colorStatusBar())
            setSystemBarAppearance(
                isAppearanceLightStatusBars = false,
                isAppearanceLightNavigationBars = false
            )

            if (isBuildLargerThan(Build.VERSION_CODES.R)) {
                window?.decorView?.windowInsetsController?.setSystemBarsAppearance(
                    APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                window?.decorView?.systemUiVisibility =
                    (View.SYSTEM_UI_FLAG_LAYOUT_STABLE and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) and (android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Get the WindowInsetsController
        windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // 2. Configure the behavior for showing system bars when hidden
        // BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE means they appear on swipe from the edge
        // and then automatically hide again.
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        hideSystemNavigation()
    }

    private fun changePreviewFullScreen() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or window.decorView.systemUiVisibility)

        val colorStatus =
            if (enableDarkMode()) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and colorStatus
        setSystemBarAppearance(
            isAppearanceLightStatusBars = true,
            isAppearanceLightNavigationBars = true
        )
    }

    private fun Activity.setSystemBarAppearance(
        isAppearanceLightStatusBars: Boolean,
        isAppearanceLightNavigationBars: Boolean
    ) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        val windowInsetController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetController.isAppearanceLightStatusBars = isAppearanceLightStatusBars
        windowInsetController.isAppearanceLightNavigationBars = isAppearanceLightNavigationBars
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemNavigation()
        }
    }

    private fun hideSystemNavigation() {
        // Hide only the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

        // If you want to hide BOTH the status bar and navigation bar for a fully immersive experience:
        // windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            hideSystemNavigation()
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            hideSystemNavigation()
        }
        return super.dispatchTouchEvent(ev)
    }



    @SuppressLint("ServiceCast")
    fun showKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    @SuppressLint("ServiceCast")
    fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            currentFocus?.windowToken ?: view.windowToken,
            0
        );
    }

    //region permission

    val REQUEST_CODE_STORAGE_R = 2296
    open fun requestPermissionAndroidR() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, REQUEST_CODE_STORAGE_R)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, REQUEST_CODE_STORAGE_R)
            }
        }
    }

    private val REQUEST_PERMISSION = 1
    fun doPermission(
        permission: Array<String>,
        callbackGrant: () -> Unit,
        callbackDenied: (() -> Unit)? = null
    ) {
        if (PermissionUtils.isRequestingPermission) return
        PermissionUtils.isRequestingPermission = true
        doRequestPermission(permission, {
            callbackGrant.invoke()
        }, {
            callbackDenied?.invoke()
        })
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun doRequestPermission(
        permissions: Array<String>,
        onAllow: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        PermissionUtils.onAllow = onAllow
        PermissionUtils.onDenied = onDenied
        if (checkPermission(permissions)) {
            onAllow()
            PermissionUtils.isRequestingPermission = false
        } else {
            requestPermissions(permissions, REQUEST_PERMISSION)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun checkPermission(permissions: Array<String>): Boolean {
        permissions.forEach {
            if (checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPermission(permissions)) {
            if (permissions.isNotEmpty()) PermissionUtils.onAllow?.invoke()
            PermissionUtils.isRequestingPermission = false
        } else {
//            PermissionUtils.showDialogPermission(this){ PermissionUtils.isRequestingPermission = false }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PermissionUtils.isRequestingPermission = false
    }
    // endregion

//    private var permissionsErrorDialog: MaterialDialog? = null
//    fun showErrorDialog(
//    @StringRes titleRes: Int = R.string.permission_activity_error_title,
//    @StringRes messageRes: Int = R.string.permission_activity_error_unknown
//    ) {
//        permissionsErrorDialog?.dismiss()
//
//        permissionsErrorDialog = MaterialDialog(this).show {
//            lifecycleOwner(this@BaseActivity)
//            icon(R.drawable.ic_notification_small_anim3_24dp)
//            title(titleRes)
//            message(messageRes)
//            positiveButton(android.R.string.ok)
//            cancelable(false)
//            cancelOnTouchOutside(false)
//        }
//    }
}