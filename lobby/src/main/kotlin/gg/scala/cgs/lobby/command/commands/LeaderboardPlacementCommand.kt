package gg.scala.cgs.lobby.command.commands

import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingHologram
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
object LeaderboardPlacementCommand : ScalaCommand()
{
    @CommandAlias("place-leaderboard")
    @CommandPermission("op")
    @CommandCompletion("@leaderboards")
    fun onPlaceLeaderboard(player: Player, entry: CgsLobbyRankingEntry)
    {
        val hologram = CgsLobbyRankingHologram(
            player.eyeLocation, entry.getId()
        )
        hologram.configure()

        player.sendMessage("${CC.GREEN}The leaderboard has been placed.")
    }
}
