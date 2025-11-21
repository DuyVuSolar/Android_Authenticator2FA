package com.beemdevelopment.aegis.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import android.preference.PreferenceManager
import java.util.Locale
import androidx.core.content.edit

object LocaleHelper {

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private val defaultLanguage = "en"
    var dLocale: Locale? = null

    fun onAttach(context: Context): Context {
        return setLocale(context)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(context)
    }

    private fun setLocale(context: Context): Context {
        return updateResources(context)
    }

    private fun getPersistedData(context: Context): String {
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }

    private fun persist(context: Context, language: String) {
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit {
            putString(SELECTED_LANGUAGE, language)
        }
    }

    private fun updateResources(context: Context): Context {
        Locale.setDefault(dLocale)
        val configuration: Configuration = context.resources.configuration
        configuration.setLocale(dLocale)
        configuration.setLayoutDirection(dLocale)
        val localeList = LocaleList(dLocale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
        val resources: Resources = context.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context.createConfigurationContext(configuration)
    }
}