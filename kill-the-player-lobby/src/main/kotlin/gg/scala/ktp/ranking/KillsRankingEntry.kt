package gg.scala.ktp.ranking

import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
object KillsRankingEntry : CgsLobbyRankingEntry<Int>
{
    override fun getId() = "kills"
    override fun getDisplay() = "Top 10 Kills"

    override fun getValue(cgsGamePlayer: CgsGamePlayer): Int
    {
        return cgsGamePlayer.gameSpecificStatistics["KillThePlayerCgsStatistics"]?.kills?.value ?: 0
    }
}
