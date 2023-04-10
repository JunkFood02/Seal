package com.junkfood.seal.util

import kotlinx.serialization.Serializable

@Serializable
data class SponsorData(
    val data: Data
)

@Serializable
data class User(
    val sponsorshipsAsMaintainer: SponsorshipsAsMaintainer
)

@Serializable
data class SponsorshipsAsMaintainer(
    val nodes: List<Node>
)

@Serializable
data class Data(
    val user: User
)

@Serializable
data class SponsorEntity(
    val login: String, val name: String
)

@Serializable
data class Tier(
    val monthlyPriceInDollars: Int
)

@Serializable
data class Node(
    val sponsorEntity: SponsorEntity, val tier: Tier
)