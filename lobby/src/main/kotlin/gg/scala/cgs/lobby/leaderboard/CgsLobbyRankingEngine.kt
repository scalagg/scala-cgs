package gg.scala.cgs.lobby.leaderboard

import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.commons.annotations.runnables.Repeating
import gg.scala.flavor.service.Service
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.promise.ThreadContext
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.math.Numbers
import java.util.concurrent.ConcurrentHashMap

/**
 * @author GrowlyX
 * @since 12/4/2021
 */
@Service
object CgsLobbyRankingEngine : Runnable
{
    @JvmStatic
    val ID_TO_FORMAT = ConcurrentHashMap<String, List<String>>()

    fun findEntry(id: String) =
        CgsGameLobby.INSTANCE
            .getRankingEntries()
            .firstOrNull {
                it.getId().equals(id, true)
            }

    override fun run()
    {
        val statisticsLayer = CgsPlayerHandler.statsLayer

        for (entry in CgsGameLobby.INSTANCE.getRankingEntries())
        {
            val formatted = mutableListOf<String>()

            statisticsLayer.mongo()
                .aggregate(
                    entry.buildAggregation()
                )
                .forEachIndexed { index, document ->
                    val result = document.parseIntoLeaderboardResult()
                    formatted += "${CC.PRI}#${index + 1} ${CC.GRAY}- ${CC.RESET}${result.uniqueId.username()} ${CC.GRAY}- ${CC.PRI}${
                        Numbers.format(
                            result.value
                        )
                    }"
                }

            ID_TO_FORMAT[entry.getId()] = formatted
        }
    }
}
