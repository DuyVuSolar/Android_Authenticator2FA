package com.beemdevelopment.aegis.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class EffectModel(
    var name : String = "",
    var thumb : Int = 0,
    var total : Int = 0,
    var quiz : ArrayList<QuizModel> = arrayListOf(),
    var isSelected: Boolean = false,
    var id : String = UUID.randomUUID().toString(),
    var date: Long = System.currentTimeMillis(),
) : BaseModel(), Parcelable

@Parcelize
data class QuizModel(
    var question : String = "",
    var leftAnswer : String = "",
    var rightAnswer : String = "",
    var answerCorrect : String = "",
    var isCorrect : Boolean = false,

) : BaseModel(), Parcelable

fun buildQuiz(question : String, leftAnswer : String, rightAnswer : String, answerCorrect : String): QuizModel {
    return QuizModel(question, leftAnswer,rightAnswer, answerCorrect)
}


fun createEffectEmpty() : EffectModel{
    return EffectModel()
}
