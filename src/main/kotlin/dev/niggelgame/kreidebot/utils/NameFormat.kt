package dev.niggelgame.kreidebot.utils

import dev.niggelgame.kreidebot.Config


fun formatName(name: String, number: Int) : String {
    return Config.NICKNAME_TEMPLATE
        .replace("{{num}}", number.toString())
        .replace("{{name}}", name)
}