package gg.scala.cgs.lobby.command.commands

import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingHologram
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
@AutoRegister
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
        hologram.initialLoad()

        player.sendMessage("${CC.GREEN}The leaderboard has been placed.")
    }
}
