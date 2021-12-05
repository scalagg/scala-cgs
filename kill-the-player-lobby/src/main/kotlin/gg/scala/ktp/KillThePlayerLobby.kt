package gg.scala.ktp

import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.cgs.common.instance.CgsServerInstance
import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry
import gg.scala.cgs.lobby.updater.CgsGameInfoUpdater
import gg.scala.ktp.game.KillThePlayerCgsInfo
import gg.scala.ktp.game.KillThePlayerCgsStatistics
import gg.scala.ktp.ranking.DeathsRankingEntry
import gg.scala.ktp.ranking.KillsRankingEntry
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
object KillThePlayerLobby : CgsGameLobby<KillThePlayerCgsStatistics>()
{
    override fun getScoreboardAdapter() = KillThePlayerScoreboard
    override fun getGameInfo() = KillThePlayerCgsInfo

    override fun getRankingEntries() = listOf(
        DeathsRankingEntry, KillsRankingEntry
    )

    override fun getGameModeButtons() = mutableMapOf(
        14 to ItemBuilder(Material.BED)
            .name("${CC.GREEN}Solo")
            .addToLore(
                "${CC.GRAY}Play a Solo game of KTP!",
                "",
                "${CC.WHITE}${
                    CgsGameInfoUpdater.gameModeCounts["solo"]
                } playing...",
                "",
                "${CC.YELLOW}Click to join!"
            )
            .toButton()
    )

    override fun getFormattedButton(info: CgsServerInstance): Button
    {
        return ItemBuilder(Material.NETHER_STAR)
            .name("${CC.GREEN}${
                info.internalServerId
            }")
            .addToLore("${CC.GRAY}click to spec lol")
            .toButton()
    }
}
