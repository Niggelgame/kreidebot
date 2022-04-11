package dev.niggelgame.kreidebot.utils

import dev.niggelgame.kreidebot.Config
import dev.niggelgame.kreidebot.NicknameData


fun formatName(name: String, number: Int) : String {
    return Config.NICKNAME_TEMPLATE
        .replace("{{num}}", number.toString())
        .replace("{{name}}", name)
}

fun extractData(name: String): NicknameData? {
    val regex = Regex("Junkie (\\d+) \\((.*?)\\)")

    regex.find(name)?.let {
        val (number, realName) = it.destructured
        val num = number.toIntOrNull() ?: return null
        return NicknameData(realName, num)
    }
    return null
}