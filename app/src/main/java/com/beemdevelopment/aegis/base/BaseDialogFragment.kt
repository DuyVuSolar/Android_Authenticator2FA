package com.beemdevelopment.aegis.base

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.beemdevelopment.aegis.R
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable


abstract class BaseDialogFragment<BD : ViewDataBinding> : DialogFragment() {

    open fun isCanceledOnTouchOutside(): Boolean = false
    lateinit var binding: BD

    private var mCallbackDismiss: () -> Unit = {}

    fun setCallbackDismiss(callback: () -> Unit){
        mCallbackDismiss = callback
    }

    fun captureView(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
//        canvas.drawColor(Color.parseColor("#80000000"))
        return bitmap
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = object : Dialog(requireContext(), getThemeCustom()) {
            override fun onBackPressed() {
                this@BaseDialogFragment.onBackPressed()
            }
        }

        dialog.setContentView(getLayoutId())
        dialog.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            if(isShowKeyboard()){
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            }else{
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            }
        }

        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside())

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
//        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        dialog.window?.statusBarColor = getColorStatusBar()
//        dialog.window?.navigationBarColor = Color.TRANSPARENT
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(width, height)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)


        //full screen
        dialog.window?.let {
            if (SDK_INT >= Build.VERSION_CODES.O) {
                dialog.window?.decorView?.systemUiVisibility =
                    ((View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).inv() or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR//check sdk
                            or dialog.window!!.decorView.systemUiVisibility)
            } else {
                dialog.window?.decorView?.systemUiVisibility =
                    ((View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).inv() or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or dialog.window!!.decorView.systemUiVisibility)
            }
        }


        //preview full
//        dialog.window?.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)


//            window.decorView.systemUiVisibility = ( View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    or window.decorView.systemUiVisibility)
//
//        dialog.window?.decorView?.systemUiVisibility = ( View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                or dialog.window?.decorView?.systemUiVisibility!!)
//
//        val colorStatus = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        dialog.window?.decorView!!.systemUiVisibility = dialog.window?.decorView!!.systemUiVisibility and colorStatus
//        dialog.window?.apply {
//            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).inv()
//        }
        dialog.window?.setWindowAnimations(setAnimation())
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp()
        setAnimation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), getLayoutId(), null, false)
        return binding.root
    }

    override fun dismiss() {
        super.dismiss()
        mCallbackDismiss.invoke()
    }

    override fun dismissAllowingStateLoss() {
        super.dismissAllowingStateLoss()
        mCallbackDismiss.invoke()
    }

    override fun dismissNow() {
        super.dismissNow()
        mCallbackDismiss.invoke()
    }

    fun isInitBinding() = this::binding.isInitialized

    open fun setAnimation() : Int = R.style.BottomSheetDialogAnimation

    abstract fun getLayoutId(): Int

    abstract fun setUp()

    abstract fun onBackPressed()

    open fun getInputHelper(): View? = null

    open fun getThemeCustom(): Int = theme

    open fun getColorStatusBar(): Int = R.color.transparent

    open fun isTextWhite() = true

    open fun isShowKeyboard() = false

    protected fun showInputHelper() {
        getInputHelper()?.visibility = View.VISIBLE
        getInputHelper()?.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
    }

    protected fun hideInputHelper() {
        getInputHelper()?.visibility = View.GONE
    }

    protected fun showKeyboard(view: View?) {
        view?.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        val v = view?.findFocus() ?: activity?.currentFocus
        if (v != null) {
            imm?.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        }
    }
    protected fun hideKeyboard(view: View?) {
        view?.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        val v = view?.findFocus() ?: activity?.currentFocus
        if (v != null) {
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}