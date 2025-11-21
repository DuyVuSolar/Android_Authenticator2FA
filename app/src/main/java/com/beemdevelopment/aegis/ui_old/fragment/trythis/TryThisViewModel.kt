package com.beemdevelopment.aegis.ui_old.fragment.trythis

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.viewModelScope
import com.beemdevelopment.aegis.base.BaseViewModel
import com.beemdevelopment.aegis.data.store.DataStoreHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class TryThisViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val repo : DataStoreHelper,
) : BaseViewModel(){

    private val tryThisEventChannel = Channel<Event>()
    val homeEvent = tryThisEventChannel.receiveAsFlow()
    var isLoading : ObservableBoolean = ObservableBoolean(true)
    var isLoadCompleted : ObservableBoolean = ObservableBoolean(false)


    fun onClickBack() = viewModelScope.launch {
        tryThisEventChannel.send(Event.OnClickBack)
    }

    fun OnNavigateUp() = viewModelScope.launch {
        tryThisEventChannel.send(Event.OnNavigateUp)
    }

    fun onClickTryThis() = viewModelScope.launch {
        tryThisEventChannel.send(Event.OnClickTryThis)
    }

    fun NavigateOnClickTryThis() = viewModelScope.launch {
        tryThisEventChannel.send(Event.NavigateOnClickTryThis)
    }

    fun onClickPlayPause() = viewModelScope.launch {
        tryThisEventChannel.send(Event.OnClickPlayPause)
    }

    sealed class Event {
        object OnNavigateUp : Event()
        object OnClickBack : Event()
        object OnClickTryThis : Event()
        object NavigateOnClickTryThis : Event()
        object OnClickPlayPause : Event()
        object OnLoadVideoCompleted : Event()
        object VideoEnd : Event()
    }

}