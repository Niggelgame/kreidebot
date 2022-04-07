package dev.niggelgame.kreidebot

import dev.niggelgame.kreidebot.utils.formatName
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import org.koin.core.component.KoinComponent
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

object KreideDatabase : KoinComponent {
    private val nameDistributor = database.getCollection<NameDistribution>("name_distributor")

    suspend fun getNameForUser(userId: Long, guildId: Long): String? {
        val nameDistribution =
            nameDistributor.findOne(and(NameDistribution::userId eq userId, NameDistribution::guildId eq guildId))
        if (nameDistribution != null) {
            return formatName(nameDistribution.name, nameDistribution.number)
        }
        return null
    }

    suspend fun createNameForUser(userId: Long, guildId: Long, name: String): String {
        val lastNumber =
            nameDistributor.find(NameDistribution::guildId eq guildId).descendingSort(NameDistribution::number).limit(1)
                .first()?.number
                ?: Config.NAME_COUNT_START_VALUE

        nameDistributor.insertOne(NameDistribution(userId, lastNumber + 1, name, guildId))

        return formatName(name, lastNumber + 1)
    }

    private suspend fun updateNameForUser(userId: Long, guildId: Long, name: String) : String {
        nameDistributor.updateOne(
            and(NameDistribution::userId eq userId, NameDistribution::guildId eq guildId),
            setValue(NameDistribution::name, name)
        )

        return getNameForUser(userId, guildId)!!
    }

    suspend fun updateNameForUserOrCreate(userId: Long, guildId: Long, name: String) : String {
        val nameDistribution =
            nameDistributor.findOne(and(NameDistribution::userId eq userId, NameDistribution::guildId eq guildId))
        return if (nameDistribution != null) {
            updateNameForUser(userId, guildId, name)
        } else {
            createNameForUser(userId, guildId, name)
        }
    }
}