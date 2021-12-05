package gg.scala.cgs.lobby.leaderboard

import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
interface CgsLobbyRankingEntry<T : Any>
{
    fun getId(): String
    fun getDisplay(): String

    fun computeTopTen(): CompletableFuture<Map<UUID, T>>
}
