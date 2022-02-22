package gg.scala.cgs.lobby.command.commands

import gg.scala.cgs.common.snapshot.CgsGameSnapshotEngine
import gg.scala.cgs.lobby.command.menu.RecentGamesMenu
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 2/22/2022
 */
object RecentGamesCommand : BaseCommand()
{
    @CommandAlias("recentgames|rg|mygames")
    fun onRecentGames(
        player: Player,
        @Optional target: UUID?
    ): CompletableFuture<Void>
    {
        player.sendMessage("${CC.GREEN}Loading recent games...")

        return CgsGameSnapshotEngine.findRecentGamesOf(
            target ?: player.uniqueId
        ) {
            RecentGamesMenu(it).openMenu(player)
        }
    }
}
