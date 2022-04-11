package dev.niggelgame.kreidebot

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.kotlindiscord.kord.extensions.utils.hasRole
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.edit
import dev.niggelgame.kreidebot.utils.extractData

class NumberArgument : Arguments() {
    val value by int {
        name = "nickname"
        description = "The nickname you want to set"
    }

    val user by member {
        name = "user"
        description = "The user you want to set the nickname for"
    }
}

/**
 * The command to set a users Junkie number
 */
suspend fun KreideModule.numberCommand() = ephemeralSlashCommand(::NumberArgument) {
    action {
        val guild = arguments.user.guild

        // Check if the command executing user has the required permissions (ManageNicknames)
        if(!event.interaction.user.asMember(guild.id).hasPermission(Permission.ManageNicknames)) {
            respond { content = "You don't have the permission to set nicknames" }
            return@action
        }

        // Check if the changable user has the required role (JOIN_ROLE_ID)
        if(!arguments.user.hasRole(guild.getRole(Config.JOIN_ROLE_ID))) {
            respond { content = "You can't set the nickname of a user that does not have the role ${guild.getRole(Config.JOIN_ROLE_ID).mention}." }
            return@action
        }

        var newName = KreideDatabase.updateNumberForUser(arguments.user.id.value.toLong(), guild.id.value.toLong(), arguments.value)

        if(newName == null) {
            // first try parsing the current name and extracting name and number
            val nicknameData = extractData(arguments.user.displayName)

            newName = KreideDatabase.createNameForUser(
                guildId = guild.id.value.toLong(),
                userId = event.interaction.user.id.value.toLong(),
                name = nicknameData?.name ?: arguments.user.displayName,
                forcedNumber = arguments.value
            )
        }

        arguments.user.edit {
            nickname = newName
        }
        respond { content = "The nickname of ${arguments.user.mention} has been set to $newName" }
    }
}