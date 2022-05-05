package dev.niggelgame.kreidebot

import com.kotlindiscord.kord.extensions.annotations.DoNotChain
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.setNickname
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.rest.request.RestRequestException
import dev.schlaubi.mikbot.plugin.api.util.embed
import kotlinx.coroutines.runBlocking

class KreideModule : Extension() {
    override val name = "Kreide"

    override suspend fun setup() {
        nicknameCommand()
        numberCommand()
        ensureJoinCommand()


        event<MemberJoinEvent> {
            action {
                // Don't do anything if bot joins server
                if (event.member.isBot) return@action

                event.member.makeJoin()
            }
        }
    }
}

@OptIn(DoNotChain::class)
suspend fun Member.makeJoin() {
    // Add role
    // if (event.guild.botHasPermissions(Permission.ManageRoles)) {
    try {
        addRole(Config.JOIN_ROLE_ID, "Join role")
    } catch (e: RestRequestException) {
        getDmChannel()
            .createMessage("I don't have enough rights to give you your role. I'm sorry.")
    }
    /* } else {
        event.member.getDmChannel()
            .createMessage("I don't have enough rights to give you your role. I'm sorry.")
    }*/

    // Set nickname
    val userId = id.value.toLong()
    val newNickname =
        KreideDatabase.getNameForUser(userId, guildId = guildId.value.toLong()) ?: runBlocking {
            KreideDatabase.createNameForUser(userId, guildId.value.toLong(), displayName, forcedNumber = null)
        }

    setNickname(newNickname)

    val welcomeMessage = embed {
        title = "Welcome $displayName to the cartel server."
        description = "Your name was changed according to our guidelines.\n" +
                "Please change the name in parentheses to your real name using `/nickname <name>`."
    }

    // Send welcome message
    if (Config.WELCOME_CHANNEL_ID != null) {
        val welcomeChannel = guild.getChannel(Config.WELCOME_CHANNEL_ID!!)

        (welcomeChannel as? TextChannel)?.createMessage {
            embeds.add(welcomeMessage)
        }
    }

    // Send welcome message to the user via PM
    getDmChannel().createMessage {
        embeds.add(welcomeMessage)
    }
}