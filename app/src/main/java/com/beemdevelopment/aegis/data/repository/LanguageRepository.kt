package com.beemdevelopment.aegis.data.repository

import com.beemdevelopment.aegis.BaseApplication
import com.beemdevelopment.aegis.model.Language
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageRepository @Inject constructor() {

    private val languages = listOf(
        Language(title = "English", alpha2 = "us", localeCode = "en", variant = "US"),
        Language(title = "Afrikaans", alpha2 = "za", localeCode = "af"),
        Language(title = "አማርኛ", alpha2 = "et", localeCode = "am"),         // Amharic
        Language(title = "Български", alpha2 = "bg", localeCode = "bg"),      // Bulgarian
        Language(title = "中文", alpha2 = "cn", localeCode = "zh"),            // Chinese
        Language(title = "Hrvatski", alpha2 = "hr", localeCode = "hr"),        // Croatian
        Language(title = "Čeština", alpha2 = "cz", localeCode = "cs"),         // Czech
        Language(title = "Dansk", alpha2 = "dk", localeCode = "da"),           // Danish
        Language(title = "Nederlands", alpha2 = "nl", localeCode = "nl"),      // Dutch
        Language(title = "Eesti", alpha2 = "ee", localeCode = "et"),           // Estonian
        Language(title = "Filipino", alpha2 = "ph", localeCode = "fil"),       // Filipino
        Language(title = "Suomi", alpha2 = "fi", localeCode = "fi"),           // Finnish
        Language(title = "Français", alpha2 = "fr", localeCode = "fr"),        // French
        Language(title = "Deutsch", alpha2 = "de", localeCode = "de"),         // German
        Language(title = "Ελληνικά", alpha2 = "gr", localeCode = "el"),        // Greek
        Language(title = "Magyar", alpha2 = "hu", localeCode = "hu"),          // Hungarian
        Language(title = "Italiano", alpha2 = "it", localeCode = "it"),        // Italian
        Language(title = "日本語", alpha2 = "jp", localeCode = "ja"),            // Japanese
        Language(title = "한국어", alpha2 = "kr", localeCode = "ko"),            // Korean
        Language(title = "Polski", alpha2 = "pl", localeCode = "pl"),          // Polish
        Language(title = "Português", alpha2 = "pt", localeCode = "pt"),       // Portuguese
        Language(title = "Română", alpha2 = "ro", localeCode = "ro"),          // Romanian
        Language(title = "Русский", alpha2 = "ru", localeCode = "ru"),         // Russian
        Language(title = "Српски", alpha2 = "rs", localeCode = "sr"),          // Serbian
        Language(title = "Slovenčina", alpha2 = "sk", localeCode = "sk"),      // Slovak
        Language(title = "Slovenščina", alpha2 = "si", localeCode = "sl"),       // Slovenian
        Language(title = "Español", alpha2 = "es", localeCode = "es"),         // Spanish
        Language(title = "Svenska", alpha2 = "se", localeCode = "sv"),         // Swedish
        Language(title = "ภาษาไทย", alpha2 = "th", localeCode = "th"),          // Thai
        Language(title = "Türkçe", alpha2 = "tr", localeCode = "tr"),          // Turkish
        Language(title = "Українська", alpha2 = "ua", localeCode = "uk"),       // Ukrainian
        Language(title = "Tiếng Việt", alpha2 = "vn", localeCode = "vi"),      // Vietnamese
    )

    fun getListLanguageClones(): ArrayList<Language> {
        val res = arrayListOf<Language>()
        val locale = BaseApplication.getAppInstance().getLanguage()
        languages.map {
            val clone = it.copy()
//            clone.isSelected = clone.localeCode == locale
            res.add(clone)
        }
        return res
    }
//  Afrikaans,  Amharic, Bulgarian, Chinese, Croatian, Czech, Danish, Dutch, Estonian, Filipino, Finnish, French, German, Greek, Hungarian, Italian, Japanese, Korean, Polish, Portuguese, Romanian, Russian, Serbian, Slovak, Slovenian, Spanish, Swedish, Thai, Turkish, Ukrainian, Vietnamese
}