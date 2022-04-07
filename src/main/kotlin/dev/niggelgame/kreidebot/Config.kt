package dev.niggelgame.kreidebot

import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig("") {
    // Starting number to append to new user
    val NAME_COUNT_START_VALUE: Int by getEnv(0) {
        it.toInt()
    }

    // Role to give when a user joins the server
    val JOIN_ROLE_ID: Snowflake by getEnv { Snowflake(it) }

    // Welcome Channel ID
    val WELCOME_CHANNEL_ID: Snowflake? by getEnv { Snowflake(it) }.optional()

    // Nickname Template
    val NICKNAME_TEMPLATE: String by getEnv("Junkie {{num}} ({{name}})")
}