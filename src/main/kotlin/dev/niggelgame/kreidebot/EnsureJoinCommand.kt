package dev.niggelgame.kreidebot

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.boolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.kotlindiscord.kord.extensions.utils.hasRole
import dev.kord.common.entity.Permission

class EnsureJoinArguments : Arguments() {
    val user by member {
        name = "user"
        description = "The user you want to set the nickname for"
    }

    val force by boolean {
        name = "force"
        description = "Force the user to regain the role"
    }
}

/**
 * The command to set a users Junkie number
 */
suspend fun KreideModule.ensureJoinCommand() = ephemeralSlashCommand(::EnsureJoinArguments) {
    name = "ensurejoin"
    description = "Re-gives the join role and sets nickname"


    action {
        val guild = arguments.user.guild

        // Check if the command executing user has the required permissions (ManageNicknames)
        if(!event.interaction.user.asMember(guild.id).hasPermission(Permission.ManageNicknames)) {
            respond { content = "You don't have the permission to set nicknames" }
            return@action
        }

        // Check if the changable user has the required role (JOIN_ROLE_ID)
        if(arguments.user.hasRole(guild.getRole(Config.JOIN_ROLE_ID)) && !arguments.force) {
            respond { content = "User seems to be already added to joined." }
            return@action
        }

        arguments.user.makeJoin()

        respond {}
    }
}