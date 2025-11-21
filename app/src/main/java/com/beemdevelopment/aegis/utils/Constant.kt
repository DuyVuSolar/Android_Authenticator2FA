package com.beemdevelopment.aegis.utils

import android.content.Context
import android.content.res.Resources
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.model.EffectModel
import com.beemdevelopment.aegis.model.QuizModel
import com.beemdevelopment.aegis.model.buildQuiz
import com.beemdevelopment.aegis.utils.LocaleHelper.dLocale
import com.beemdevelopment.aegis.utils.extension.getApplication

object Constant {
    const val STATUS_BAR_HEIGHT = "status_bar_height"
    const val NAVIGATION_BAR_HEIGHT = "navigation_bar_height"

    const val MAX_TIME_RECORD_15s = 15
    const val MAX_TIME_RECORD_60s = 60
    const val MAX_TIME_RECORD_10P = 600

    var timeRecord = MAX_TIME_RECORD_15s

    //key extra
    const val KEY_EXTRA_ID = "KEY_EXTRA_ID"
    const val KEY_EXTRA_DATA = "KEY_EXTRA_DATA"

    const val KEY_EXTRA_BOOLEAN = "KEY_EXTRA_BOOLEAN"
    const val MY_FOLDER_EXTERNAL = "PDFViewer"

    const val REQUEST_CODE_OPEN_SETTINGS_SYSTEM = 3001

    const val MAIN_TYPE_HOME = 0
    const val MAIN_TYPE_RECORDINGS = 1
    const val MAIN_TYPE_SETTING = 2

    const val TODAY = "Today"
    const val YESTERDAY = "Yesterday"


    fun getConfigurationContext(): Context {
        val configuration: android.content.res.Configuration = getApplication().resources.configuration
        configuration.setLocale(dLocale)
        configuration.setLayoutDirection(dLocale)
        // Create a new context with this configuration
        val resources: Resources = getApplication().resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return getApplication().createConfigurationContext(configuration)
    }

    private var singWithWords = arrayListOf(
        buildQuiz("Love", "","", ""),
        buildQuiz("Baby", "","", ""),
        buildQuiz("Red", "","", ""),
        buildQuiz("Me", "","", ""),
        buildQuiz("Heart", "","", ""),
        buildQuiz("Party", "","", ""),
        buildQuiz("Time", "","", ""),
        buildQuiz("Rain", "","", ""),
        buildQuiz("Yesterday", "","", ""),
        buildQuiz("Tomorrow", "","", ""),
        buildQuiz("Sorry", "","", ""),
        buildQuiz("Blue", "","", ""),
        buildQuiz("Purple", "","", ""),
        buildQuiz("Money", "","", ""),
        buildQuiz("Life", "","", ""),
        buildQuiz("World", "","", ""),
        buildQuiz("Friend", "","", ""),
        buildQuiz("Day", "","", ""),
        buildQuiz("Night", "","", ""),
        buildQuiz("Dream", "","", ""),
        buildQuiz("Cry", "","", ""),
        buildQuiz("Forever", "","", ""),
        buildQuiz("Smile", "","", ""),
        buildQuiz("Happy", "","", ""),
        buildQuiz("Kiss", "","", ""),
        buildQuiz("Dance", "","", ""),
        buildQuiz("Girl", "","", ""),
        buildQuiz("Sun", "","", ""),
        buildQuiz("Boy", "","", ""),
    )

    private var tiktokVoices = arrayListOf(
        buildQuiz("1+1", "","", ""),
        buildQuiz("2+1", "","", ""),
        buildQuiz("3+1", "","", ""),
        buildQuiz("a", "","", ""),
        buildQuiz("b", "","", ""),
        buildQuiz("c", "","", ""),
        buildQuiz("d", "","", ""),
        buildQuiz("e", "","", ""),
        buildQuiz("f", "","", ""),
        buildQuiz("g", "","", ""),
        buildQuiz("x", "","", ""),
    )

    fun getListWordsClone() : ArrayList<QuizModel>{
        val res = arrayListOf<QuizModel>()
        singWithWords.forEach {
            res.add(it.copy())
        }
        res.shuffled()
        return res
    }
    fun getListTikTokVoiceClone() : ArrayList<QuizModel>{
        val res = arrayListOf<QuizModel>()
        tiktokVoices.forEach {
            res.add(it.copy())
        }
        res.shuffled()
        return res
    }

}
