package com.beemdevelopment.aegis.ui_old.fragment.camera

import android.content.Context
import com.beemdevelopment.aegis.base.BaseViewModel
import com.beemdevelopment.aegis.data.store.DataStoreHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val repo : DataStoreHelper,
) : BaseViewModel() {


}