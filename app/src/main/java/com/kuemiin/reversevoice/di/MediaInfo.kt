package com.kuemiin.reversevoice.di

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class MediaInfo(val getFieldName: String)
