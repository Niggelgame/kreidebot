package dev.niggelgame.kreidebot

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.niggelgame.kreidebot.utils.formatName
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

object KreideDatabase : KordExKoinComponent {
    private val nameDistributor = database.getCollection<NameDistribution>("name_distributor")

    private fun ensureNameLength(name: String, number: Int) : String {
        val formattedName = formatName(name, number)
        if (formattedName.length > 32) {
            return name.substring(0, name.length - (formattedName.length - 32))
        }
        return name
    }

    suspend fun getNameForUser(userId: Long, guildId: Long): String? {
        val nameDistribution =
            nameDistributor.findOne(and(NameDistribution::userId eq userId, NameDistribution::guildId eq guildId))
        if (nameDistribution != null) {
            return formatName(nameDistribution.name, nameDistribution.number)
        }
        return null
    }

    /**
     * Insert a user with name into the database
     *
     * To make sure, name fits discord constraints, we check if the name is longer than 32 chars and shorten it if it is
     *
     * @param userId The discord user id
     * @param guildId The discord guild id
     * @param name The name of the user
     * @param forcedNumber The number of the user forced to set. If null, will take the next available number
     */
    suspend fun createNameForUser(userId: Long, guildId: Long, name: String, forcedNumber: Int?): String {
        val lastNumber = forcedNumber
            ?: (nameDistributor.find(NameDistribution::guildId eq guildId).descendingSort(NameDistribution::number)
                .limit(1)
                .first()?.number?.let { it -> it + 1 }
                ?: Config.NAME_COUNT_START_VALUE)

        val shortenedName = ensureNameLength(name, lastNumber)

        nameDistributor.insertOne(NameDistribution(userId, lastNumber, shortenedName, guildId))

        return formatName(shortenedName, lastNumber)
    }

    suspend fun updateNameForUser(userId: Long, guildId: Long, name: String): String? {
        nameDistributor.updateOne(
            and(NameDistribution::userId eq userId, NameDistribution::guildId eq guildId),
            setValue(NameDistribution::name, name)
        )

        return getNameForUser(userId, guildId)
    }

    suspend fun updateNumberForUser(userId: Long, guildId: Long, number: Int): String? {
        nameDistributor.updateOne(
            and(NameDistribution::userId eq userId, NameDistribution::guildId eq guildId),
            setValue(NameDistribution::number, number)
        )

        return getNameForUser(userId, guildId)
    }

    /*suspend fun updateNameForUserOrCreate(userId: Long, guildId: Long, name: String) : String {
        val nameDistribution =
            nameDistributor.findOne(and(NameDistribution::userId eq userId, NameDistribution::guildId eq guildId))
        return if (nameDistribution != null) {
            updateNameForUser(userId, guildId, name)
        } else {
            createNameForUser(userId, guildId, name)
        }
    }*/
}