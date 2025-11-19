package com.kuemiin.reversevoice.ui.activity.choose_language

import android.annotation.SuppressLint
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kuemiin.reversevoice.base.BaseViewModel
import com.kuemiin.reversevoice.data.repository.LanguageRepository
import com.kuemiin.reversevoice.data.store.DataStoreHelper
import com.kuemiin.reversevoice.data.store.DataStoreHelper.Companion.KEY_HAS_CHOOSE_LANGUAGE
import com.kuemiin.reversevoice.model.Language
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.kuemiin.reversevoice.BaseApplication
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class ChooseLanguageViewModel @Inject constructor(
    val store: DataStoreHelper,
    val languageRepo: LanguageRepository
) : BaseViewModel() {

    // region Const and Fields
    private val languageEventChannel = Channel<ChooseLanguageEvent>()
    val languageEvent = languageEventChannel.receiveAsFlow()
    val listLanguage = languageRepo.getListLanguageClones()
    var isShowBackBtn: ObservableBoolean = ObservableBoolean(false)
    var hasChooseLanguage: ObservableBoolean = ObservableBoolean(false)
    var showIntro: ObservableBoolean = ObservableBoolean(false)

    // endregion
    fun checkShowIntro() = viewModelScope.launch {
        showIntro.set(store.hasShowIntro.first())
    }
    fun setShowChooseLanguage() = viewModelScope.launch {
        store.setValueBoolean(true, KEY_HAS_CHOOSE_LANGUAGE)
    }

    fun checkChooseLanguage() = viewModelScope.launch {
        val language = BaseApplication.getAppInstance().getLanguage()
        listLanguage.forEach {
            it.isSelected = it.localeCode == language
        }
        hasChooseLanguage.set(true)
    }

    fun onClickBack() = viewModelScope.launch {
        languageEventChannel.send(ChooseLanguageEvent.OnClickBack)
    }

    fun onClickSave() = viewModelScope.launch {
        setShowChooseLanguage()
        languageEventChannel.send(ChooseLanguageEvent.OnClickSave)
    }

    fun onClickItemLanguage(item : Language) = viewModelScope.launch {
        hasChooseLanguage.set(true)
        listLanguage.forEach {
            it.isSelected = item.title == it.title
        }
        languageEventChannel.send(ChooseLanguageEvent.OnClickItemLanguage(item))
        setChecked()
    }
    private val _isChecked = MutableLiveData(false)
    val isChecked: LiveData<Boolean> get() = _isChecked

    fun setChecked() {
        _isChecked.value = true
    }
    //region onclick

    // endregion

    sealed class ChooseLanguageEvent {
        object OnClickBack : ChooseLanguageEvent()
        object OnClickSave : ChooseLanguageEvent()
        class OnClickItemLanguage(val item : Language) : ChooseLanguageEvent()
    }
}