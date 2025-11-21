package com.beemdevelopment.aegis.model


data class IntroSplash(
    val title: Int,
    val res: Int,
    val index: Int,
    val showNative : Boolean,
    val showNativeFull : Boolean = false,
    val isPerNotification : Boolean = false,
    val isPerMicrophone : Boolean = false,
) : BaseModel()
