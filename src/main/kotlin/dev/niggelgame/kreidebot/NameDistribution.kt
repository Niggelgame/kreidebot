package dev.niggelgame.kreidebot

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class NameDistribution(
    @SerialName("_id")
    val userId: Long,
    val number: Int,
    val name: String,
    val guildId: Long,
)
