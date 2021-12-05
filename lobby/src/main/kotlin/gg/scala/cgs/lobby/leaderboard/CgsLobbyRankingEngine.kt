package gg.scala.cgs.lobby.leaderboard

import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.lemon.util.CubedCacheUtil
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import java.util.*
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 12/4/2021
 */
object CgsLobbyRankingEngine
{
    @JvmStatic
    val ID_TO_FORMAT = mutableMapOf<String, List<String>>()

    private val engine by lazy {
        CgsGameLobby.INSTANCE
    }

    var entries by Delegates.notNull<List<CgsLobbyRankingEntry<*>>>()

    fun findEntry(id: String): CgsLobbyRankingEntry<*>?
    {
        return entries.firstOrNull {
            it.getId().equals(id, true)
        }
    }

    fun initialLoad()
    {
        entries = engine.getRankingEntries().toList()

        Schedulers.async().runRepeating(Runnable {
            CgsPlayerHandler.handle.fetchAllEntries().thenAccept {
                for (entry in entries)
                {
                    val topTen = it.entries
                        .sortedByDescending { mapping -> entry.getValue(mapping.value) }
                        .subList(0, 9)

                    val formatted = mutableListOf<String>()

                    topTen.forEachIndexed { index, data ->
                        formatted.add("${CC.PRI}${index + 1}. ${CC.RESET}${
                            CubedCacheUtil.fetchName(
                                UUID.fromString(data.key)
                            )
                        } ${CC.GRAY}- ${CC.GREEN}${data.value}")
                    }

                    ID_TO_FORMAT[entry.getId()] = formatted
                }
            }
        }, 0L, 60L)
    }
}
