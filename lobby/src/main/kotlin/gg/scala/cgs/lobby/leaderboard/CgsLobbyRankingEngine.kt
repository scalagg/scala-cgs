package gg.scala.cgs.lobby.leaderboard

import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.lemon.util.CubedCacheUtil
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
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

    private var entries by Delegates.notNull<List<CgsLobbyRankingEntry<*>>>()

    fun initialLoad()
    {
        entries = engine.getRankingEntries().toList()

        Schedulers.async().runRepeating(Runnable {
            for (entry in entries)
            {
                entry.computeTopTen().thenAccept {
                    val formatted = mutableListOf<String>()

                    it.entries.forEachIndexed { index, data ->
                        formatted.add("${CC.PRI}${index + 1}. ${CC.RESET}${
                            CubedCacheUtil.fetchName(data.key)
                        } ${CC.GRAY}- ${CC.GREEN}${data.value}")
                    }

                    ID_TO_FORMAT[entry.getId()] = formatted
                }
            }
        }, 0L, 60L)
    }
}
