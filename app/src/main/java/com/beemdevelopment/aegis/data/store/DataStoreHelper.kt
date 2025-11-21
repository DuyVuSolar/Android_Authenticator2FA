package com.beemdevelopment.aegis.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DataStoreHelper.PREFERENCE_NAME)

@Singleton
class DataStoreHelper @Inject constructor(@ApplicationContext private val context: Context) {


    companion object {
         const val PREFERENCE_NAME = "Store"

        val KEY_HAS_SHOW_INTRO = booleanPreferencesKey("HAS_SHOW_INTRO")
        val KEY_HAS_SHOW_GUIDE = booleanPreferencesKey("KEY_HAS_SHOW_GUIDE")
        val KEY_HAS_CHOOSE_LANGUAGE = booleanPreferencesKey("KEY_HAS_CHOOSE_LANGUAGE")
        val KEY_PLAYBACK_SPEED = floatPreferencesKey("KEY_PLAYBACK_SPEED")
        val KEY_PITCH_SHIFT = floatPreferencesKey("KEY_PITCH_SHIFT")
        val KEY_LOOP_PLAYBACK = booleanPreferencesKey("KEY_LOOP_PLAY_BACK")
    }

    val hasShowIntro: Flow<Boolean> = context.dataStore.data.map { it[KEY_HAS_SHOW_INTRO] ?: false }
    val hasShowGuide: Flow<Boolean> = context.dataStore.data.map { it[KEY_HAS_SHOW_GUIDE] ?: false }
    val hasChooseLanguage: Flow<Boolean> = context.dataStore.data.map { it[KEY_HAS_CHOOSE_LANGUAGE] ?: false }
    val loopPlayback: Flow<Boolean> = context.dataStore.data.map { it[KEY_LOOP_PLAYBACK] ?: true }
    val playBackSpeed: Flow<Float> = context.dataStore.data.map { it[KEY_PLAYBACK_SPEED] ?: 1f }
    val pitchShift: Flow<Float> = context.dataStore.data.map { it[KEY_PITCH_SHIFT] ?: 1f }


    suspend fun setTimeInt(result: Int, key : Preferences.Key<Int>) = context.dataStore.edit {
        it[key] = result
    }

    suspend fun setValueString(result: String, key : Preferences.Key<String>) = context.dataStore.edit {
        it[key] = result
    }

    suspend fun setValueLong(result: Long, key : Preferences.Key<Long>) = context.dataStore.edit {
        it[key] = result
    }

    suspend fun setValueInt(result: Int, key : Preferences.Key<Int>) = context.dataStore.edit {
        it[key] = result
    }

    suspend fun setValueFloat(result: Float, key : Preferences.Key<Float>) = context.dataStore.edit {
        it[key] = result
    }

    suspend fun setValueBoolean(result: Boolean, key : Preferences.Key<Boolean>) = context.dataStore.edit {
        it[key] = result
    }


    fun <T> getValue(transform: (preferences: Preferences) -> T): Flow<T> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {
            transform.invoke(it)
        }

    suspend fun setValue(transform: (preference: MutablePreferences) -> Unit) = try {
        context.dataStore.edit {
            transform.invoke(it)
        }
    } catch (exception: Exception) {
        Timber.e("DataStore: Fail to set value - $exception")
    }
}