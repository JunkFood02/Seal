package com.junkfood.seal.util

import android.util.Base64
import android.util.Log
import androidx.annotation.CheckResult
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object SponsorUtil {
    private const val TAG = "SponsorUtil"
    private const val MAGIC_STRING_0 = "Z2hwX2F0cFlT"
    private const val MAGIC_STRING_1 = "ZWtJQzFXb0JoQnBlYmlFbWI2TEZF"
    private const val MAGIC_STRING_2 = "NXJDNzNvZndQVw"

    // pls don't abuse
    private val magicString =
        Base64.decode(MAGIC_STRING_0 + MAGIC_STRING_1 + MAGIC_STRING_2, Base64.DEFAULT)
            .toString(Charsets.UTF_8)

    private val body =
        """
{ "query": "query { viewer { sponsorshipsAsMaintainer(first: 100) { nodes { sponsorEntity { ... on User { login name websiteUrl socialAccounts(first: 4) { nodes { displayName url } } } ... on Organization { login name websiteUrl } } tier { monthlyPriceInDollars } } } } }" }
"""
            .toRequestBody("application/json".toMediaType())

    private val request =
        Request.Builder()
            .url("https://api.github.com/graphql")
            .post(body)
            .addHeader("Authorization", "bearer $magicString")
            .build()

    private val client = OkHttpClient()
    private val jsonFormat = Json { ignoreUnknownKeys = true }
    private var sponsorData: SponsorData? = null

    @CheckResult
    fun getSponsors(): Result<SponsorData> =
        client
            .runCatching {
                sponsorData
                    ?: jsonFormat
                        .decodeFromString<SponsorData>(
                            newCall(request).execute().body.string().also { Log.d(TAG, it) }
                        )
                        .apply { sponsorData = this }
            }
            .onFailure { it.printStackTrace() }
}
