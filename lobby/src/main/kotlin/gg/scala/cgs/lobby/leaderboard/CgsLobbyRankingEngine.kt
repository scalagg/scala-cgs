package gg.scala.cgs.lobby.leaderboard

import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.commons.annotations.runnables.Repeating
import gg.scala.flavor.service.Service
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.promise.ThreadContext
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 12/4/2021
 */
@Service
@Repeating(20 * 60 * 5L, context = ThreadContext.ASYNC)
object CgsLobbyRankingEngine : Runnable
{
    @JvmStatic
    val ID_TO_FORMAT = mutableMapOf<String, List<String>>()

    var entries = mutableListOf<CgsLobbyRankingEntry>()
    var entriesConfigured = false

    fun findEntry(id: String): CgsLobbyRankingEntry?
    {
        return entries.firstOrNull {
            it.getId().equals(id, true)
        }
    }

    override fun run()
    {
        if (!this.entriesConfigured)
        {
            kotlin.runCatching {
                this.entries.addAll(
                    CgsGameLobby.INSTANCE.getRankingEntries()
                )

                this.entriesConfigured = true
            }
        }

        CgsPlayerHandler.handle
            .loadAll(DataStoreStorageType.MONGO)
            .thenAccept {
                for (entry in CgsGameLobby.INSTANCE.getRankingEntries())
                {
                    var topTen = it.entries
                        .sortedByDescending { mapping -> entry.getValue(mapping.value) }

                    if (topTen.size > 10)
                    {
                        topTen = topTen.subList(0, 9)
                    }

                    val formatted = mutableListOf<String>()

                    topTen.forEachIndexed { index, data ->
                        formatted.add(
                            "${CC.PRI}${index + 1}. ${CC.RESET}${
                                CubedCacheUtil.fetchName(data.key)
                            } ${CC.GRAY}- ${CC.GREEN}${data.value}"
                        )
                    }

                    ID_TO_FORMAT[entry.getId()] = formatted
                }
            }
    }
}
