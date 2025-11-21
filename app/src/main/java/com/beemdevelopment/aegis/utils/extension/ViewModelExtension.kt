package com.beemdevelopment.aegis.utils.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> Fragment.observer(liveData: LiveData<T>?, onDataChange: (T?) -> Unit) {
    liveData?.observe(viewLifecycleOwner, Observer(onDataChange))
}

fun <T> AppCompatActivity.observer(liveData: LiveData<T>?, onDataChange: (T?) -> Unit) {
    liveData?.observe(this, Observer(onDataChange))
}