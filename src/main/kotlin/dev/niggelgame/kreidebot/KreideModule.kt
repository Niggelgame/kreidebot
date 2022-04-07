package dev.niggelgame.kreidebot

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.rest.request.RestRequestException
import dev.schlaubi.mikbot.plugin.api.util.embed
import kotlinx.coroutines.runBlocking

class KreideModule : Extension() {
    override val name = "Kreide"

    override suspend fun setup() {
        slashCommandCheck {
            anyGuild()
        }

        nicknameCommand()

        event<MemberJoinEvent> {
            action {
                // Don't do anything if bot joins server
                if (event.member.isBot) return@action

                // Add role

                // if (event.guild.botHasPermissions(Permission.ManageRoles)) {
                try {
                    event.member.addRole(Config.JOIN_ROLE_ID!!, "Join role")
                } catch (e: RestRequestException) {
                    event.member.getDmChannel()
                        .createMessage("I don't have enough rights to give you your role. I'm sorry.")
                }
                /* } else {
                    event.member.getDmChannel()
                        .createMessage("I don't have enough rights to give you your role. I'm sorry.")
                }*/


                // Set nickname
                val userId = event.member.id.value.toLong()
                val newNickname =
                    KreideDatabase.getNameForUser(userId, guildId = event.guildId.value.toLong()) ?: runBlocking {
                        KreideDatabase.createNameForUser(userId, event.guildId.value.toLong(), event.member.displayName, forcedNumber = null)
                    }

                event.member.edit {
                    nickname = newNickname
                }

                val welcomeMessage = embed {
                    title = "Welcome ${event.member.displayName} to the cartel server."
                    description = "Your name was changed according to our guidelines.\n" +
                            "Please keep the layout the same. You may change the name in the parenthesis"
                }

                // Send welcome message
                if (Config.WELCOME_CHANNEL_ID != null) {
                    val welcomeChannel = event.guild.getChannel(Config.WELCOME_CHANNEL_ID!!)

                    (welcomeChannel as? TextChannel)?.createMessage {
                        embeds.add(welcomeMessage)
                    }
                }

                // Send welcome message to the user via PM
                event.member.getDmChannel().createMessage {
                    embeds.add(welcomeMessage)
                }
            }
        }
    }
}

