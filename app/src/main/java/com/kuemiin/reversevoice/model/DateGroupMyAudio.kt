package com.kuemiin.reversevoice.model

data class DateGroupMyAudio(var date: String = "", var timeStamp: Long = 0, var list: ArrayList<AppAudio> = ArrayList()) {

    fun addItem(appAudio: AppAudio) {
        list.add(appAudio)
    }

    override fun toString(): String {
        return "DateGroupMyAudio(date='$date', list=$list)"
    }


}