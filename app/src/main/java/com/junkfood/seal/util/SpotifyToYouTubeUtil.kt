package com.junkfood.seal.util

import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SpotifyToYouTubeUtil {

    private val client = OkHttpClient()
    private val requestForLatestRelease =
        Request.Builder().url("https://spowlo-js-api.onrender.com/api/v1/spottoyt")
            .build()
    private val jsonFormat = Json { ignoreUnknownKeys = true }

    //create a function that have the url parameter that will be passed to the api
    suspend fun getYouTubeUrl(url: String): Response {

        //create a request with the url parameter
        val request = Request.Builder().url("https://spowlo-js-api.onrender.com/api/v1/spottoyt?song=$url")
            .build()

        return suspendCoroutine { continuation ->
            client.newCall(request).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    val responseData = response.body!!.string()
                    val latestRelease = jsonFormat.decodeFromString<Response>(responseData)
                    response.body!!.close()
                    continuation.resume(latestRelease)
                }

            })
        }
    }

    @Serializable
    data class Response(
        @SerialName("result") val url: String
    )

}