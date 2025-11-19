package com.kuemiin.reversevoice.model

data class Language(
    val title: String,
    val alpha2: String,
    val localeCode: String,
    val variant: String = "",
    var isSelected : Boolean = false
) : BaseModel()
