package dev.niggelgame.kreidebot

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalGuild
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.TextChannel
import dev.niggelgame.kreidebot.KreideDatabase.updateNameForUserOrCreate
import dev.schlaubi.mikbot.plugin.api.util.embed


class NicknameArgument : Arguments() {
    val value by string {
        name = "nickname"
        description = "The nickname you want to set"
    }
    val guild by optionalGuild {
        name = "server"
        description = "The server you want to set the nickname in"
    }
}

suspend fun KreideModule.nicknameCommand() = ephemeralSlashCommand(::NicknameArgument) {
    name = "nickname"
    description = "Set your nickname"

    action {
        val guild = if (arguments.guild != null) {
            arguments.guild!!
        } else if (event.interaction.getChannel() as? TextChannel != null) {
            (event.interaction.getChannel() as TextChannel).getGuild()
        } else {
            respond {
                content = "You need to specify a guild if you are using me via Private Messages!"
            }
            return@action
        }

        val newNickname = updateNameForUserOrCreate(
            guildId = guild.id.value.toLong(),
            userId = event.interaction.user.id.value.toLong(),
            name = arguments.value
        )

        guild.getMember(event.interaction.user.id).edit {
            nickname = newNickname
        }

        respond {

            embeds.add(embed {
                title = "Nickname set!"
                description = "Your nickname has been set to `$newNickname`"
            })
        }
    }
}