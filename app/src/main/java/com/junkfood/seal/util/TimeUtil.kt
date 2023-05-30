package com.junkfood.seal.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtil {
    //Zulu time to local time parser
    @SuppressLint("SimpleDateFormat")
    fun parseDateStringToLocalTime(dateString: String): String? {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return try {
            val parsedDate = inputFormat.parse(dateString)
            val localTimeString = outputFormat.format(parsedDate!!)
            localTimeString
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}