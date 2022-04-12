package dev.niggelgame.kreidebot

@kotlinx.serialization.Serializable
data class NameDistribution(
    val userId: Long,
    val number: Int,
    val name: String,
    val guildId: Long,
)
