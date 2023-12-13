package com.junkfood.seal.util

import kotlinx.serialization.Serializable

@Serializable
data class SponsorData(
    val data: Data
)

@Serializable
data class Data(
    val viewer: Viewer
)

@Serializable
data class Viewer(
    val sponsorshipsAsMaintainer: SponsorshipsAsMaintainer
)

@Serializable
data class SponsorshipsAsMaintainer(
    val nodes: List<SponsorShip>
)


@Serializable
data class SponsorEntity(
    val login: String,
    val name: String? = null,
    val websiteUrl: String? = null,
    val socialAccounts: SocialAccounts? = null
)

@Serializable
data class Tier(
    val monthlyPriceInDollars: Int
)

@Serializable
data class SponsorShip(
    val sponsorEntity: SponsorEntity, val tier: Tier? = null
)

@Serializable
data class SocialAccounts(
    val nodes: List<SocialAccount>? = null
)

@Serializable
data class SocialAccount(val displayName: String?, val url: String?)