package com.junkfood.seal.util

import android.util.Base64
import androidx.annotation.CheckResult
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object SponsorUtil {
    private const val TAG = "SponsorUtil"
    private const val MAGIC_STRING = "Z2hwX3hmUDYzWDhGZ2l5Q3c5aUVoV1NHd05udWZrNlFwYjFFZVZEbA"

    // pls don't abuse
    private val magicString = Base64.decode(MAGIC_STRING, Base64.DEFAULT).toString(Charsets.UTF_8)

    private val body = """
{ "query": "query { user(login: \"JunkFood02\") { sponsorshipsAsMaintainer(first: 100) { nodes { sponsorEntity { ... on User { name login } ... on Organization { name login } } tier { monthlyPriceInDollars } } } } }" }
""".toRequestBody("application/json".toMediaType())

    private val request = Request.Builder()
        .url("https://api.github.com/graphql")
        .post(body)
        .addHeader("Authorization", "bearer $magicString")
        .build()

    private val client = OkHttpClient()
    private val jsonFormat = Json { ignoreUnknownKeys = true }
    private var sponsorData: SponsorData? = null

    @CheckResult
    fun getSponsors(): Result<SponsorData> = client.runCatching {
        sponsorData
            ?: jsonFormat.decodeFromString<SponsorData>(newCall(request).execute().body.string())
                .apply { sponsorData = this }
    }
}