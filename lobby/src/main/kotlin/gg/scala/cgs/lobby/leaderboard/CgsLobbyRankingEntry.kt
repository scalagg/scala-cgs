package gg.scala.cgs.lobby.leaderboard

import gg.scala.cgs.common.player.CgsGamePlayer

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
interface CgsLobbyRankingEntry
{
    fun getId(): String
    fun getDisplay(): String

    fun getValue(cgsGamePlayer: CgsGamePlayer): Int
}
