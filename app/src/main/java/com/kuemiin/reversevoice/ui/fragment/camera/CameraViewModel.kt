package com.kuemiin.reversevoice.ui.fragment.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Handler
import android.util.Size
import android.view.Surface
import android.view.View
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableFloat
import androidx.databinding.ObservableInt
import androidx.databinding.ObservableLong
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ezandroid.lib.ezfilter.extra.sticker.Bitmap2025QuizFilter.EFFECT_SING_A_SONG
import com.kuemiin.reversevoice.BaseApplication.Companion.cameraFacing
import com.kuemiin.reversevoice.base.BaseViewModel
import com.kuemiin.reversevoice.data.store.DataStoreHelper
import com.kuemiin.reversevoice.model.EffectModel
import com.kuemiin.reversevoice.model.EncoderModel
import com.kuemiin.reversevoice.model.QuizModel
import com.kuemiin.reversevoice.pose.FaceLandmarkerHelper
import com.kuemiin.reversevoice.ui.fragment.camera.canvas.CanvasFileFilter
import com.kuemiin.reversevoice.utils.Constant
import com.kuemiin.reversevoice.utils.Constant.MAX_TIME_RECORD_10P
import com.kuemiin.reversevoice.utils.Constant.MAX_TIME_RECORD_15s
import com.kuemiin.reversevoice.utils.Constant.timeRecord
import com.kuemiin.reversevoice.utils.FileUtils
import com.kuemiin.reversevoice.utils.PermissionUtils
import com.kuemiin.reversevoice.utils.gone
import com.kuemiin.reversevoice.utils.loge
import com.kuemiin.reversevoice.utils.visible
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
@ExperimentalCoroutinesApi
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val repo : DataStoreHelper,
) : BaseViewModel() {

    companion object{
        const val TYPE_TIMER_ZERO = 0
    }

    private val cameraEventChannel = Channel<Event>()
    val cameraEvent = cameraEventChannel.receiveAsFlow()

    var firstNoInternet : Boolean? = null
    var enableStartRecord : ObservableBoolean = ObservableBoolean(false)
    var poseLandmarkerHelper: FaceLandmarkerHelper? = null

    var isRecording: ObservableBoolean = ObservableBoolean(false)
    var isShowListFilter: ObservableBoolean = ObservableBoolean(false)
    var isSavedRecent: ObservableBoolean = ObservableBoolean(false)
    var isStateNoInternet: ObservableBoolean = ObservableBoolean(false)
    var isNoInternet: ObservableBoolean = ObservableBoolean(false)
    var isLoading: ObservableBoolean = ObservableBoolean(false)
    var isInitingFilter: ObservableBoolean = ObservableBoolean(false)
    var isCountingTimeStart: ObservableBoolean = ObservableBoolean(false)
    var isFrontCamera: ObservableBoolean = ObservableBoolean(false)
    var timeRecoding: ObservableFloat = ObservableFloat(0f)
    var isAllowPermission: ObservableBoolean = ObservableBoolean(PermissionUtils.hasPermissionCamera)
    var typeTimer: ObservableInt = ObservableInt(0)

    var timeRemainToStart = 0

    var startTime = 0L
    var timeLimit = ObservableInt(MAX_TIME_RECORD_15s)

    var mCurrentCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK // The front camera is usually 1
    var mCameraManager: CameraManager? = null
    var mSurfaceTexture: SurfaceTexture? = null
    private var cameraSelector: CameraSelector? = null
    private var imageAnalyzer: ImageAnalysis? = null
    var backgroundExecutor: ExecutorService? = null
    var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var cameraX: Camera? = null
    var typeFilter = EFFECT_SING_A_SONG
    var mCanvasFilter = CanvasFileFilter()
    var listQuiz = arrayListOf<QuizModel>()
        set(value) {
            field = value
            setCanvasQuiz()
        }

    var cameraOpened = false

    private fun setCanvasQuiz(){
        isInitingFilter.set(true)
        mCanvasFilter.setQuizAndCanvas(context, listQuiz, typeFilter){
            isInitingFilter.set(false)
            if(cameraOpened){//voi truong hop camera mo truoc khi handle bitmnap
                setResolution()
                handleInitFilter()
            }
        }
    }

    fun handleInitFilter() = viewModelScope.launch{
        if(cameraOpened && !isInitingFilter.get()){
            cameraEventChannel.send(Event.HandleInitFilter)
        }
    }

    fun getListQuizQuestion(): ArrayList<QuizModel> {
        val listEffect = arrayListOf<EffectModel>().apply {
            addAll(Constant.listMath)
        }
        val quizs = listEffect.first { it.name == typeFilter }.quiz
        return quizs
    }

    fun setResolution(){
        val displaySize = Point()
        activity!!.windowManager.defaultDisplay.getSize(displaySize)
        val characteristics = mCameraManager!!.getCameraCharacteristics(mCurrentCameraId.toString())
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        // Find out if we need to swap dimension to get the preview size relative to sensor
        val targetRatio = displaySize.y.toFloat() / displaySize.x.toFloat()
        val listSize = map?.getOutputSizes(SurfaceTexture::class.java)?.filter { it.height in 700.. 2300 && it.width in 700..  2300}
        mPreviewSize = listSize?.minByOrNull {
            (it.width.toFloat() / it.height.toFloat() - targetRatio).absoluteValue
        }
    }

    private var mPreviewSize : Size? = null
    fun getSizeFromResolution(): Size {
        return mPreviewSize ?: Size(1920, 1080)
    }


    fun handleTimerToStartRecord(callbackTime : (Int) -> Unit) = viewModelScope.launch{
        isCountingTimeStart.set(true)
        timeRemainToStart = typeTimer.get()
        for( i in 0..typeTimer.get()){
            if(isCountingTimeStart.get()){
                callbackTime.invoke(timeRemainToStart)
                timeRemainToStart -= 1
                delay(1050)
            }
        }
    }

    fun onClickBack() = viewModelScope.launch {
        cameraEventChannel.send(Event.OnClickBack)
    }

    fun onClickChangeWordCamera() = viewModelScope.launch {
        cameraEventChannel.send(Event.OnClickChangeWordCamera)
    }

    fun NavigateUp() = viewModelScope.launch {
        cameraEventChannel.send(Event.NavigateUp)
    }

    fun onClickOpenSetting() = viewModelScope.launch {
        cameraEventChannel.send(Event.OnClickOpenPermission)
    }

    fun onClickChangeTimeLimit(time : Int) = viewModelScope.launch {
        timeLimit.set(time)
        timeRecord = time
        cameraEventChannel.send(Event.OnClickChangeTimeLimit)
    }

    private var isAllowClick = true
    private fun blockClickTop() = viewModelScope.launch{
        if(isAllowClick){
            isAllowClick = false
            delay(1500)
            isAllowClick = true
        }
    }

    fun onClickRecord(isShowToastFullMemory: Boolean = false) = viewModelScope.launch {
        if(isLoading.get() || isInitingFilter.get() || !enableStartRecord.get()) return@launch
        if(isAllowClick){
            blockClickTop()
            cameraEventChannel.send(Event.OnClickRecord(isShowToastFullMemory))
        }
    }

    fun createExecutor(
        activity: FragmentActivity,
        callbacks: (FaceLandmarkerHelper.ResultBundle) -> Unit
    ) {
        backgroundExecutor = Executors.newSingleThreadExecutor()
//        setUpCamera(activity)
        backgroundExecutor?.execute {
            poseLandmarkerHelper = FaceLandmarkerHelper(
                activity,
                object : FaceLandmarkerHelper.LandmarkerListener {
                    override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
                        callbacks.invoke(resultBundle)
                    }

                    override fun onError(error: String, errorCode: Int) {
                    }
                }
            )
        }
    }

    private fun setUpCamera(context: FragmentActivity) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(context)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }


    // Declare and bind preview, capture and analysis use cases
    @Suppress("DEPRECATION")
    @SuppressLint("RestrictedApi")
    private fun bindCameraUseCases(activity: FragmentActivity) {
        // In this mode, the executor always caches the latest image into an image buffer (similar to a queue with a depth of one
        // while the application analyzes the previous image. If CameraX receives a new image before the application finishes processing,
        // the new image is saved to the same buffer, overwriting the previous image. Note that ImageAnalysis.Builder.setImageQueueDepth() has no effect in this scenario, and the buffer contents are always overwritten.
        // You can enable this non-blocking mode by calling setBackpressureStrategy() with STRATEGY_KEEP_ONLY_LATEST
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()


        val characteristics = mCameraManager!!.getCameraCharacteristics(mCurrentCameraId.toString())
        val configs: StreamConfigurationMap? = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val resolution = Size(480, 640)
//        val resolution = Size(720, 540)
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //
            .setSurfaceOccupancyPriority(1)
            .setImageQueueDepth(6)
            .setTargetResolution(resolution)
            .setMaxResolution(resolution)
            .setDefaultResolution(resolution)
            .setHighResolutionDisabled(true)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
//            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888) //TODO Buffer not large enough for pixels
            .build()


        val previewSizes = configs?.getOutputSizes(SurfaceTexture::class.java)
        previewSizes?.forEach {
//            Log.i("DUYVD123", "Preview available output size: $it")
        }
        imageAnalyzer?.setAnalyzer(backgroundExecutor!!) { imageProxy ->
            detectPose(imageProxy)
        }
        cameraProvider.unbindAll()
        setUpSurfaceTexture(activity, activity, mSurfaceTexture)
    }

    private fun setUpSurfaceTexture(context: Context, lifecycle: LifecycleOwner, mSurfaceTexture: SurfaceTexture?) {
        // Must unbind the use-cases before rebinding them
        try {
            val surfaceProvider = Preview.SurfaceProvider { request: SurfaceRequest ->
                if (mSurfaceTexture != null) {
                    val surface = Surface(mSurfaceTexture)
                    request.provideSurface(
                        surface,
                        ContextCompat.getMainExecutor(context)
                    ) { result: SurfaceRequest.Result ->

                    }
                }
            }

            preview = Preview.Builder().build()
            preview?.setSurfaceProvider(surfaceProvider)
            cameraX = cameraProvider?.bindToLifecycle(lifecycle, cameraSelector!!, imageAnalyzer, preview)
            preview?.setSurfaceProvider(surfaceProvider)
//            cameraX?.cameraControl?.enableTorch(isAllowOnFlash())
        } catch (e: Exception) {
        }
    }

//    fun isAllowOnFlash() : Boolean{
//        isTurnOnFlash.set(turnOnFlash)
//        return isTurnOnFlash.get() && isCameraBack()
//    }

    private fun isCameraBack(): Boolean {
        return cameraFacing == CameraSelector.LENS_FACING_BACK
    }

    private fun detectPose(imageProxy: ImageProxy) {
        if(!isLoading.get()){
            poseLandmarkerHelper?.detectLiveStream(
                imageProxy,
                cameraFacing == CameraSelector.LENS_FACING_FRONT
            )
        }
    }


    private var handler = Handler()
    private var runnableTime = object : Runnable {
        override fun run() {
            timeRecoding.set((System.currentTimeMillis() - startTime) / 1f)
            loge("setProgressTimer runnableTime ---- ${timeRecoding.get()})")

            if(timeRecoding.get() >= timeLimit.get() * 1000){
                timeRecoding.set(timeLimit.get().toFloat() * 1000)
                loge("runnableTime - ${timeLimit.get()}")
                handler.removeCallbacks(this)
                viewModelScope.launch {
                    delay(200)
                    onClickRecord(false)
                }
            }else if(FileUtils.checkAvailableStoreToRecordDownload()){
                handler.postDelayed(this, 200)
            }else{
                handler.removeCallbacks(this)
                onClickRecord(true)
            }
        }
    }

    fun setStartTimeRecording() {
        startTime = System.currentTimeMillis()
        timeRecoding.set(0f)
        handler.removeCallbacks(runnableTime)
        handler.postDelayed(runnableTime, 200)
    }

    //endregion
    fun setStopTimeRecording() {
        handler.removeCallbacks(runnableTime)
    }

    fun showPreview(file : File) = viewModelScope.launch {
        if(!isRecording.get()){
            cameraEventChannel.send(Event.OnShowPreview(file))
        }
    }
    fun NavigateOnShowPreview(encoderModel: EncoderModel) = viewModelScope.launch {
        if(!isRecording.get()){
            cameraEventChannel.send(Event.NavigateOnShowPreview(encoderModel))
        }
    }

    @SuppressLint("StaticFieldLeak")
    private var imvNoInternet: View? = null
    @SuppressLint("StaticFieldLeak")
    private var imvRestoredInternet: View? = null
    fun setViewInternetState(vNoInternet: View, vRestoredInternet: View) {
        imvNoInternet = vNoInternet
        imvRestoredInternet = vRestoredInternet
    }

    fun handleStateViewNetwork() {
        if (isStateNoInternet.get()) {
            if (isNoInternet.get()) {
                hideAllViewStateNetwork()
            } else if(!isRecording.get()){
                imvNoInternet?.visible()
                imvRestoredInternet?.gone()
            }
        } else if(!isRecording.get()){
            imvNoInternet?.gone()
            imvRestoredInternet?.visible()
            imvRestoredInternet?.gone(2000)
        }
    }

    fun hideAllViewStateNetwork() {
        imvNoInternet?.gone(1)
        imvRestoredInternet?.gone(1)
    }


    sealed class Event {
        class OnClickRecord(val isShowToastFullMemory : Boolean) : Event()
        class OnShowPreview(val file : File) : Event()
        class NavigateOnShowPreview(val encoderModel: EncoderModel) : Event()
        object OnClickChangeWordCamera : Event()
        object OnClickBack : Event()
        object NavigateUp : Event()
        object OnClickOpenPermission : Event()
        object OnClickChangeTimeLimit : Event()
        object HandleInitFilter : Event()
        object OnClickCancelWatermark : Event()
    }

}