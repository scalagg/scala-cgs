package gg.scala.cgs.common.information

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
data class CgsGamePersistenceInfo(
    val mongoDataLocation: String,
    val redisDataLocation: String,
    val shouldStoreGameToMongo: Boolean
)
