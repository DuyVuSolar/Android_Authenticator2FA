package com.kuemiin.reversevoice.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.kuemiin.reversevoice.utils.extension.isBuildLargerThan
import timber.log.Timber
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kuemiin.base.extension.NAVIGATION_BAR_HEIGHT
import com.kuemiin.reversevoice.utils.LocaleHelper
import com.kuemiin.reversevoice.utils.MaxUtils.isGoneBanner
import com.kuemiin.reversevoice.utils.NotchUtils.getInternalDimensionSize
import com.kuemiin.reversevoice.utils.NotchUtils.hasNotchAtHuaWei
import com.kuemiin.reversevoice.utils.extension.toPixel
import com.kuemiin.reversevoice.utils.isAndroid13
import com.kuemiin.reversevoice.utils.keyboard.KeyboardHeightProvider

@Suppress("DEPRECATION")
abstract class BaseFragment<BD : ViewDataBinding> : Fragment(){

    protected lateinit var binding: BD

    fun captureView(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
//        canvas.drawColor(Color.parseColor("#80000000"))
        return bitmap
    }

    fun captureActivityWindowWithPixelCopy(activity: Activity, callback: (Bitmap?) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // PixelCopy added in API 26, but recommended from O for window capture
            val window: Window = activity.window
            val bitmap = Bitmap.createBitmap(window.decorView.width, window.decorView.height, Bitmap.Config.ARGB_8888)
            val locationOfWindowInScreen = IntArray(2)
            window.decorView.getLocationInWindow(locationOfWindowInScreen)

            try {
                PixelCopy.request(
                    window,
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        } else {
                            Log.e("PixelCopy", "Failed to copy window: $copyResult")
                            callback(null)
                        }
                    },
                    Handler(Looper.getMainLooper()) // Callback on the main thread
                )
            } catch (e: IllegalArgumentException) {
                // This can happen if the window is not valid, e.g., not attached.
                callback(null)
            }
        } else {
            Log.w("PixelCopy", "PixelCopy for window capture requires API 26+.")
            // Fallback to older method if needed for < API 26
            callback(getScreenBitmap(activity)) // Using the View.draw() method as fallback
        }
    }

    fun getScreenBitmap(activity: Activity): Bitmap {
        val rootView: View = activity.window.decorView.rootView
        val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        rootView.draw(canvas)
        return bitmap
    }

    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    open fun marginViewBottomDefault() = 20.toPixel.toInt() - if(isGoneBanner) 0 else 80.toPixel.toInt()
    open fun viewNeedToMarginBottom() : View? = null

    private fun getKeyboardListener() = object : KeyboardHeightProvider.KeyboardListener {
        override fun onHeightChanged(height: Int) {
            viewNeedToMarginBottom()?.marginBottomNormal((height + 20.toPixel).toInt())
        }
    }

    fun View.marginBottomNormal(space: Int) {
        if (this.layoutParams is ConstraintLayout.LayoutParams) {
            val params = this.layoutParams as ConstraintLayout.LayoutParams
            val navigationBarHeight = this.context.getInternalDimensionSize(
                NAVIGATION_BAR_HEIGHT
            )
            val actionBarSize = navigationBarHeight + if (space > 0) space + marginViewBottomDefault() else 0
            params.setMargins(20.toPixel.toInt(), 0, 20.toPixel.toInt(), actionBarSize)
            this.layoutParams = params
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val layoutInflater: LayoutInflater = if (styleFragment() != null) {
            val themeWrapper = ContextThemeWrapper(activity, styleFragment()!!)
            LayoutInflater.from(themeWrapper)
        } else {
            inflater
        }
        binding = DataBindingUtil.inflate(layoutInflater, getLayoutId(), container, false)
        binding.lifecycleOwner = this
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        keyboardHeightProvider?.onResume()
        changeFullScreen()
    }

    override fun onPause() {
        super.onPause()
        keyboardHeightProvider?.onPause()
    }

    fun isInitBinding() : Boolean = this@BaseFragment::binding.isInitialized

    fun changeFullScreen() {
        if (isFullScreen()) {
            requireActivity().window?.apply {
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
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isHandleBackPress()) initEventBackStack()


        if (isAndroid13() && !requireContext().hasNotchAtHuaWei()) {
            ViewCompat.setOnApplyWindowInsetsListener(requireActivity().window.decorView) { v: View?, insets: WindowInsetsCompat ->
                val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
                val keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

                try {
                    val navigationBarHeight = this.requireContext().getInternalDimensionSize(
                        NAVIGATION_BAR_HEIGHT
                    )
                    if (keyboardHeight > 100 && isKeyboardVisible) { // 0.15 ratio is perhaps enough to determine keypad height.
                        viewNeedToMarginBottom()?.marginBottomNormal(keyboardHeight - navigationBarHeight + 20f.toPixel.toInt())
                    } else { //Keyboard is hidden
                        //
                        viewNeedToMarginBottom()?.marginBottomNormal(- navigationBarHeight + 20f.toPixel.toInt())
                    }
                } catch (e: Exception) {
                }
                insets
            }
        }else{
            keyboardHeightProvider = KeyboardHeightProvider(requireActivity())
            keyboardHeightProvider?.addKeyboardListener(getKeyboardListener())
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
        } catch (e: Exception) {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp()
    }

    private fun initEventBackStack() {
        val callback: OnBackPressedCallback =
                object : OnBackPressedCallback(true /* enabled by default */) {
                    override fun handleOnBackPressed() {
                        onBackPressed()
                    }
                }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    open fun styleFragment(): Int? = null

    open fun isFullScreen(): Boolean = true

    open fun isDarkTheme(): Boolean = false

    open fun isHandleBackPress() = true

    open fun notifyAdapterSelectList() {}

    open fun afterNavigateTo() {}

    abstract fun getLayoutId(): Int

    abstract fun setUp()

    open fun onBackPressed() {
        if(!navigateUpValue()){
            handleNavigateUpError()
        }
    }

    open fun handleNavigateUpError(){

    }

    open fun onActivityReturned(result: ActivityResult) {}


    //region pick media
    private val resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        onActivityReturned(result)
    }

    private var callbackGetAudio: (Uri) -> Unit = {}
    private var mGetContentAudio = registerForActivityResult(GetContent()) {
        it?.let { it1 -> callbackGetAudio.invoke(it1) }
    }
    private var callbackGetImage: (Uri) -> Unit = {}
    private var mGetContentImage = registerForActivityResult(GetContent()) {
        it?.let { it1 -> callbackGetImage.invoke(it1) }
    }

    fun selectAudio(callback: (Uri) -> Unit){
        callbackGetAudio = callback
        mGetContentAudio.launch("audio/*")
    }

    fun selectImage(callback: (Uri) -> Unit){
        callbackGetImage = callback
        mGetContentImage.launch("image/*")
    }

    protected fun onStartActivityForResult(intent: Intent, option: ActivityOptionsCompat? = null) {
        resultLauncher.launch(intent, option)
    }
    //endregion

    fun popBackStack(tag: String) {
        val backTag = tag.ifEmpty { javaClass.simpleName }
        activity?.supportFragmentManager?.popBackStack(
                backTag,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private val REQUEST_PERMISSION = 1
    private var onAllow: (() -> Unit)? = null
    private var onDenied: (() -> Unit)? = null

    @SuppressLint("ObsoleteSdkInt")
    fun checkPermission(permissions: Array<String>): Boolean {
        permissions.forEach {
            if (activity?.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    private fun doPermission(permission: Array<String>, callbackGrant: () -> Unit) {
        doRequestPermission(permission, {
            callbackGrant.invoke()
        }, {
            Log.d("TAG", "denied() called")
        })
    }

    @SuppressLint("ObsoleteSdkInt")
    protected fun doRequestPermission(
            permissions: Array<String>,
            onAllow: () -> Unit = {},
            onDenied: () -> Unit = {}
    ) {
        this.onAllow = onAllow
        this.onDenied = onDenied
        if (checkPermission(permissions)) {
            onAllow()
        } else {
            requestPermissions(permissions, REQUEST_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPermission(permissions)) {
            if (permissions.isNotEmpty()) onAllow?.invoke()
        } else {
//            PermissionUtils.showDialogPermission(activity, this) {
//
//            }
        }
    }

    fun navigateUp() {
        try {
            findNavController().navigateUp()
        } catch (e: IllegalArgumentException) {
            Timber.e("IllegalArgumentException Multiple navigation attempts handled.")
        } catch (e: Exception) { //
            Timber.e("Exception Multiple navigation attempts handled.")
        }
    }

    fun navigateUpValue() : Boolean {
        try {
            return findNavController().navigateUp()
        } catch (e: IllegalArgumentException) {
            Timber.e("IllegalArgumentException Multiple navigation attempts handled.")
            return false
        } catch (e: Exception) { //
            Timber.e("Exception Multiple navigation attempts handled.")
            return false
        }
    }

    fun navigateTo(actionId: Int, args: Bundle? = null) {
        try {
            with(findNavController()) {
                currentDestination?.getAction(actionId)?.let {
                    if (currentDestination?.id != currentDestination?.getAction(actionId)?.destinationId) {
                        if(args != null){
                            navigate(actionId, args)
                            afterNavigateTo()
                        }else{
                            navigate(actionId)
                            afterNavigateTo()
                        }
                    }
                }
            }
        } catch (e: IllegalArgumentException) {
            Timber.e("Multiple navigation attempts handled.")
        } catch (e: Exception) {
            Timber.e("Exception Multiple navigation attempts handled.")
        }
    }

    fun navigateToDes(actionId: Int, args: Bundle? = null) {
        try {
            with(findNavController()) {
                if(args != null){
                    navigate(actionId, args)
                }else{
                    navigate(actionId)
                }
            }
        } catch (e: IllegalArgumentException) {
            Timber.e("Multiple navigation attempts handled.")
        } catch (e: Exception) {
            Timber.e("Exception Multiple navigation attempts handled.")
        }
    }


    @SuppressLint("ServiceCast")
     fun showKeyboard(view : View){
        val inputMethodManager = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    @SuppressLint("ServiceCast")
    fun hideKeyboard(){
        val inputMethodManager = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val v = view?.findFocus() ?: requireActivity().currentFocus
        if (v != null) {
            inputMethodManager.hideSoftInputFromWindow(v.windowToken ?: view?.windowToken, 0);
        }
    }

    @SuppressLint("ServiceCast")
    fun hideKeyboard(view: View?){
        view?.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        val v = view?.findFocus() ?: activity?.currentFocus
        if (v != null) {
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    //endregion
}