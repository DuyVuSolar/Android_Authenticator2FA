package com.beemdevelopment.aegis.base

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.beemdevelopment.aegis.utils.extension.isBuildLargerThan
import java.lang.Exception
import androidx.core.graphics.drawable.toDrawable

abstract class BaseDialogAlert<BD : ViewDataBinding>(context: Context) : AlertDialog(context) {

    lateinit var binding: BD
    private var callBackCancel : () -> Unit = {}
    fun setUpCallbackCancel(callback : () -> Unit) {
        callBackCancel = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            if (window != null) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            }


            setFullScreen()

            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.inflate(layoutInflater, getLayout(), null, false)
            setContentView(binding.root)
            binding.root.post {
                initView()
                setAnimation()
            }
            setCancelable(isCanceledOnTouchOut())
            this.setCanceledOnTouchOutside(isCanceledOnTouchOut())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    private fun setFullScreen() {
        // This method should only be called if isFullScreen() is true
        if(isPreviewFull()){
            window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            window?.decorView?.systemUiVisibility = ( View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or window!!.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv())
        }


        if (isFullScreen()) {
            window?.apply {
                if (isBuildLargerThan(Build.VERSION_CODES.R)) {
                    setDecorFitsSystemWindows(false) // Content draws behind system bars
                    insetsController?.hide(WindowInsets.Type.statusBars())
                    insetsController?.hide(WindowInsets.Type.navigationBars())
                    insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) // Hides status bar
                    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // For swipe-to-show
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hides navigation bar
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // Content behind nav bar
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Content behind status bar
                            or View.SYSTEM_UI_FLAG_FULLSCREEN) // Hides status bar
                }
                // this.statusBarColor = this.statusBarColor // This line seems redundant or a placeholder
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setFullScreen()
    }

    override fun show() {
        super.show()
        setFullScreen()
    }

    override fun cancel() {
        super.cancel()
        setFullScreen()
        callBackCancel.invoke()
    }

    internal fun isInitialized() : Boolean = this::binding.isInitialized

    open fun isPreviewFull(): Boolean = true

    open fun isFullScreen(): Boolean = true

    abstract fun setAnimation()

    abstract fun initView()

    abstract fun getLayout(): Int

    abstract fun isCanceledOnTouchOut(): Boolean

}