package com.beemdevelopment.aegis.di

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class MediaInfo(val getFieldName: String)
