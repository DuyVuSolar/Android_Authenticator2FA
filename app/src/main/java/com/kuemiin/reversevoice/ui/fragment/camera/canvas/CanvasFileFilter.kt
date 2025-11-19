package com.kuemiin.reversevoice.ui.fragment.camera.canvas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.kuemiin.reversevoice.BaseApplication
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.model.QuizModel
import com.kuemiin.reversevoice.utils.FileUtils.FOLDER_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil


@Suppress("DEPRECATION")
@SuppressLint("DefaultLocale")
class CanvasFileFilter {
//    region canvas prepare filter
    private var mContext : Context? = BaseApplication.instance
    private var mTypeFilter : String = ""
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    private var textQuestion : String = ""

    private var arrayTextQuestion = arrayListOf<String>()

    private var questionPaint = Paint().apply { isAntiAlias = true }
    private var outlinePaint = Paint().apply { isAntiAlias = true }
    private var emptyPaint = Paint().apply { isAntiAlias = true }
    private var whitePaint = Paint().apply { isAntiAlias = true }
    private var greenPaint = Paint().apply { isAntiAlias = true }
    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var frameBitmapCongrat : Bitmap? = null//this frame will contain mBitmapQuestion

    private var frameBitmapQuestion : Bitmap? = null//this frame will contain mBitmapQuestion
    private var mBitmapQuestion : Bitmap? = null
    private val bitmapWidthQuestion = 501
    private val bitmapHeightQuestion = 234

    private var frameBitmapMirco : Bitmap? = null//this frame will contain mBitmapQuestion
    private var frameBitmapDegree360 : Bitmap? = null//this frame will contain mBitmapQuestion
    private var mBitmapTimeToShow : Bitmap? = null
    private val bitmapWidthTimeToShow = 300
    private val bitmapHeightTimeToShow = 300

    private var frameBitmapTomJerryVoice : Bitmap? = null
    private val bitmapHeightTomJerryVoice = 546
    private val bitmapWidthTomJerryVoice = 366

    private var frameBitmapTiktokVoice : Bitmap? = null
    private var frameBitmapTiktokVoiceMan : Bitmap? = null
    private var frameBitmapTiktokVoiceWoMan : Bitmap? = null
    private val bitmapHeightTiktokVoice = 354
    private val bitmapWidthTiktokVoice = 288

    private val quality = 70
    private val compressConfig = Bitmap.CompressFormat.WEBP
    private val endFile = ".webp"
    private var currentQuiz = 0
    private var quizModels = arrayListOf<QuizModel>()
    private var mCanvasDone : () -> Unit = {}


    private var bitmapTimerSize = 350
    private var frameBitmapTimer: Bitmap? = null
    private val DURATION_PER_NUMBER_MS = 1000L // Each number (3, 2, 1) is visible for 1 second

    fun setQuizAndCanvas(context : Context, quizs : ArrayList<QuizModel>, typeFilter : String, callback : () -> Unit){
        mContext = context
        mTypeFilter = "typeFilter"
        mCanvasDone = callback
        quizModels.clear()
        quizModels.addAll(quizs)
        setText()
    }

    private fun setText(){
        scope.launch {
            withContext(Dispatchers.Default){
                if(frameBitmapQuestion == null) {
                    frameBitmapQuestion = Bitmap.createBitmap(bitmapWidthQuestion, bitmapHeightQuestion, Bitmap.Config.ARGB_8888)
                }

                if(frameBitmapDegree360 == null) {
                    frameBitmapDegree360 = Bitmap.createBitmap(bitmapWidthTimeToShow, bitmapHeightTimeToShow, Bitmap.Config.ARGB_8888)
                }

                if(frameBitmapMirco == null){
                    val source = BitmapFactory.decodeResource(mContext?.resources, R.drawable.ic_micro_green)
                    frameBitmapMirco = Bitmap.createScaledBitmap(source, bitmapWidthTimeToShow / 2, bitmapHeightTimeToShow / 2, true)
                }

                if(mBitmapQuestion == null){
                    val source = BitmapFactory.decodeResource(mContext?.resources, R.drawable.bg_question)
                    mBitmapQuestion = Bitmap.createScaledBitmap(source, bitmapWidthQuestion, bitmapHeightQuestion, true)
                }
                if(frameBitmapCongrat == null){
                    val inputStream = mContext?.assets?.open("congrat/frame_001.png")
                    frameBitmapCongrat = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStream), 1200, 1200, true)
                }

                if(frameBitmapTiktokVoice == null){
                    val inputStream = mContext?.assets?.open("voice/tiktok_voice.webp")
                    val inputStreamMan = mContext?.assets?.open("voice/tiktok_voice_face.webp")
                    val inputStreamWoman = mContext?.assets?.open("voice/tiktok_voice_face_woman.webp")
                    frameBitmapTiktokVoice = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStream), bitmapWidthTiktokVoice, bitmapHeightTiktokVoice, true)
                    frameBitmapTiktokVoiceMan = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStreamMan), bitmapWidthTiktokVoice, bitmapHeightTiktokVoice, true)
                    frameBitmapTiktokVoiceWoMan = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStreamWoman), bitmapWidthTiktokVoice, bitmapHeightTiktokVoice, true)
                }

                frameBitmapTimer = Bitmap.createBitmap(bitmapTimerSize, bitmapTimerSize, Bitmap.Config.ARGB_8888)

                questionPaint.color = Color.parseColor("#EA00FF")
                questionPaint.style = Paint.Style.FILL_AND_STROKE
                questionPaint.textAlign = Paint.Align.CENTER
                try {
                    questionPaint.typeface = Typeface.createFromAsset(
                        mContext?.resources?.assets,
                        "fonts/crushed_regular.ttf"
                    )
                } catch (e: Exception) {
                    questionPaint.typeface = Typeface.DEFAULT_BOLD
                }


                outlinePaint.color = Color.BLACK
                outlinePaint.style = Paint.Style.STROKE
                outlinePaint.strokeWidth = 4f
                outlinePaint.typeface = questionPaint.typeface
                outlinePaint.textAlign = Paint.Align.CENTER

                textPaint.apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                    textAlign = Paint.Align.CENTER
                    try {
                        typeface = Typeface.createFromAsset(mContext?.resources?.assets, "fonts/crushed_regular.ttf")
                    } catch (e: Exception) {
                        typeface = Typeface.DEFAULT_BOLD
                    }
                }

                whitePaint.apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }

                greenPaint.apply {
                    color = Color.parseColor("#5FBE85")
                    style = Paint.Style.FILL
                }

                quizModels.forEach { quiz ->
                    currentQuiz = quizModels.indexOf(quiz)
                    textQuestion = quiz.question

                    arrayTextQuestion.clear()
                    for (line in textQuestion.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        arrayTextQuestion.add(line)
                    }

                    val indexQuiz = quizModels.indexOf(quiz)
                    createQuestionBitmapAndSaveFile(indexQuiz)//create question with type filter
                }

                for (i in 30 downTo 1 step 1){
                    createAndSaveBitmapCountDown(i.toString())
                }
                for (i in 1..140){
                    createCongratulationBitmapAndSaveFile(i)
                }
                for (i in 0..181){
                    createTimeToShowBitmapAndSaveFile(i * 2)
                }
                createTiktokVoiceBitmapAndSaveFile(0, frameBitmapTiktokVoice)
                createTiktokVoiceBitmapAndSaveFile(1, frameBitmapTiktokVoiceMan)
                createTiktokVoiceBitmapAndSaveFile(2, frameBitmapTiktokVoiceWoMan)

                mCanvasDone.invoke()
            }
        }
    }

    private fun createQuestionBitmapAndSaveFile(indexQuiz : Int) {
        frameBitmapQuestion ?: return
        val folderSingWords = File(getFolderFilter(), "/${folderSingWord}")
        if(!folderSingWords.exists()) folderSingWords.mkdirs()
        val file = File(folderSingWords, "SingWords_${indexQuiz}${endFile}")
        if(file.exists() && file.length() > 0) return

        frameBitmapQuestion?.eraseColor(Color.TRANSPARENT)
        val canvasQuestion = Canvas(frameBitmapQuestion!!)
        canvasQuestion.drawBitmap(mBitmapQuestion!!, 0f, 0f, emptyPaint)
        questionPaint.textSize = if(textQuestion.contains("\n")) mBitmapQuestion!!.height * 0.18f else mBitmapQuestion!!.height * 0.45f
        outlinePaint.textSize = questionPaint.textSize

        var yText = if(textQuestion.contains("\n")) (mBitmapQuestion!!.height / 2) else (mBitmapQuestion!!.height * 4/5)
        for (line in arrayTextQuestion) {
            canvasQuestion.drawText(line, mBitmapQuestion!!.width / 2f * 0.96f , yText.toFloat(), outlinePaint)
            canvasQuestion.drawText(line, mBitmapQuestion!!.width / 2f * 0.96f, yText.toFloat(), questionPaint)
            yText = (yText + (questionPaint.descent() - questionPaint.ascent())).toInt()
        }

        // Save to file
        FileOutputStream(file).use { out ->
            frameBitmapQuestion?.compress(compressConfig, quality, out)
        }
    }

    private fun createCongratulationBitmapAndSaveFile(indexQuiz : Int) {
        val inputStream = mContext?.assets?.open("congrat/frame_${String.format("%03d", indexQuiz)}.png")
        val bitmapCongrat = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStream), 600, 600, true)

        val folderSingWords = File(getFolderFilter(), "/${folderCongrat}")
        if(!folderSingWords.exists()) folderSingWords.mkdirs()
        val file = File(folderSingWords, "Congrat_${indexQuiz}${endFile}")
        if(file.exists() && file.length() > 0) return

        // Save to file
        FileOutputStream(file).use { out ->
            bitmapCongrat?.compress(compressConfig, quality, out)
        }
    }

    private fun createTimeToShowBitmapAndSaveFile(indexQuiz : Int) {
        frameBitmapDegree360 ?: return
        val folderTimeToShow = File(getFolderFilter(), "/${folderTime}")
        if(!folderTimeToShow.exists()) folderTimeToShow.mkdirs()
        val file = File(folderTimeToShow, "TimeToShow_${indexQuiz}${endFile}")
        if(file.exists() && file.length() > 0) return

        frameBitmapDegree360?.eraseColor(Color.TRANSPARENT)
        val canvasArc = Canvas(frameBitmapDegree360!!)
        canvasArc.drawCircle(frameBitmapDegree360!!.width / 2f, frameBitmapDegree360!!.width / 2f, frameBitmapDegree360!!.width / 2f, whitePaint)
        val rectF = RectF(0f, 0f, frameBitmapDegree360!!.width.toFloat(), frameBitmapDegree360!!.width.toFloat())
        canvasArc.drawArc(rectF, -90f, indexQuiz.toFloat(), true, greenPaint)

        canvasArc.drawBitmap(frameBitmapMirco!!, frameBitmapDegree360!!.width / 4f, frameBitmapDegree360!!.width / 4f, emptyPaint)


        // Save to file
        FileOutputStream(file).use { out ->
            frameBitmapDegree360?.compress(compressConfig, quality, out)
        }
    }

    private fun createTiktokVoiceBitmapAndSaveFile(indexQuiz : Int, bitmapCanvas : Bitmap?) {
        bitmapCanvas ?: return
        val folderSingWords = File(getFolderFilter(), "/${folderVoice}")
        if(!folderSingWords.exists()) folderSingWords.mkdirs()
        val file = File(folderSingWords, "TiktokVoice_${indexQuiz}${endFile}")
        if(file.exists() && file.length() > 0) return

        // Save to file
        FileOutputStream(file).use { out ->
            bitmapCanvas?.compress(compressConfig, quality, out)
        }
    }

    private fun createAndSaveBitmapCountDown(text: String) {
        frameBitmapTimer ?: return
        val canvas = Canvas(frameBitmapTimer!!)
        val folderTimer = File(getFolderFilter(), "/${folderTimer}")
        if (!folderTimer.exists()) folderTimer.mkdirs()

        val file = File(folderTimer, "Timer_${text}${endFile}")
        if(file.exists() && file.length() > 0) return

        val countDownFolder = File(folderTimer, FOLDER_NAME)
        if (!countDownFolder.exists()) countDownFolder.mkdirs()

        val opacityValue = text.toInt() % 10f / 10
        val scaleFactor = text.toInt() % 10f / 10

        if(scaleFactor == 0f){
            textPaint.textSize = bitmapTimerSize * 0.8f
            textPaint.alpha = 255
        }else{
            textPaint.textSize = bitmapTimerSize * 0.8f * scaleFactor
            textPaint.alpha = (255f * opacityValue).toInt()
        }
        frameBitmapTimer?.eraseColor(Color.TRANSPARENT)
        textPaint.textSize = bitmapTimerSize * 0.8f
        val x = canvas.width / 2f
        val y = (canvas.height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(ceil(text.toFloat() / 10f).toInt().toString(), x, y, textPaint)

        try {
            FileOutputStream(file).use { out ->
                frameBitmapTimer?.compress(Bitmap.CompressFormat.PNG, quality, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFolderFilter(): File {
        val folder = File(mContext?.filesDir, getFilterFormatFile(mTypeFilter))
        if(!folder.exists()) folder.mkdirs()
//        val totalSizeBytes = getFolderSize(folder)
//        val totalSizeMB = totalSizeBytes / (1024f * 1024f)
        return folder
    }


    private fun getFolderSize(folder: File): Long {
        var totalSize: Long = 0
        folder.listFiles()?.forEach { file ->
            totalSize += if (file.isFile) {
                file.length() // Size of the file
            } else {
                getFolderSize(file) // Recursively calculate size for subdirectories
            }
        }
        return totalSize
    }
    //endregion

    companion object{
        const val maxRotate = 24f

        const val folderSingWord = "Question"
        const val folderTimer = "Timer"
        const val folderVoice = "Voice" // 1 : quay lưng, 2 : man, 3 : woman
        const val folderCongrat = "Congrat"
        const val folderTime = "Time"//show 360 do
         fun getFilterFormatFile(type : String) : String{
             return "/${type.replace(" ", "_")}"
         }
    }

}