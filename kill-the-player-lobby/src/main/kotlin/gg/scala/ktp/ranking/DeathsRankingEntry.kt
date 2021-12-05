package gg.scala.ktp.ranking

import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
object DeathsRankingEntry : CgsLobbyRankingEntry<Int>
{
    override fun getId() = "deaths"
    override fun getDisplay() = "Top 10 Deaths"

    override fun getValue(cgsGamePlayer: CgsGamePlayer): Int
    {
        return cgsGamePlayer.gameSpecificStatistics["KillThePlayerCgsStatistics"]?.deaths?.value ?: 0
    }
}
