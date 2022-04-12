package dev.niggelgame.kreidebot

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalGuild
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasRole
import com.kotlindiscord.kord.extensions.utils.setNickname
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.json.JsonErrorCode
import dev.kord.rest.request.RestRequestException
import dev.niggelgame.kreidebot.utils.extractData
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

        val member = guild.getMember(event.interaction.user.id)

        if (!member.hasRole(guild.getRole(Config.JOIN_ROLE_ID))) {
            respond {
                content =
                    "Only users with the role ${guild.getRole(Config.JOIN_ROLE_ID).mention} should use this command!"
            }
            return@action
        }

        var newNickname = KreideDatabase.updateNameForUser(
            guildId = guild.id.value.toLong(),
            userId = event.interaction.user.id.value.toLong(),
            name = arguments.value
        )

        if (newNickname == null) {
            // first try parsing the current name and extracting name and number
            val nicknameData = extractData(member.displayName)

            newNickname = KreideDatabase.createNameForUser(
                guildId = guild.id.value.toLong(),
                userId = event.interaction.user.id.value.toLong(),
                name = arguments.value,
                forcedNumber = nicknameData?.number
            )
        }
        try {
            member.setNickname(newNickname)
        } catch (e: RestRequestException) {
            if (e.error?.code == JsonErrorCode.PermissionLack) {
                respond {
                    content = "I don't have the permission to set the nickname!"
                }
                return@action
            } else if (e.error?.code == JsonErrorCode.InvalidFormBody) {
                respond {
                    content = "The nickname is too long!"
                }
                return@action
            }
            throw e
        }

        respond {
            embeds.add(embed {
                title = "Nickname set!"
                description = "Your nickname has been set to `$newNickname`. \nYou can set a new nickname using `/nickname <nickname>`"
            })
        }
    }
}


data class NicknameData(
    val name: String,
    val number: Int
)