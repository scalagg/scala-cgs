package gg.scala.cgs.lobby.command.commands

import gg.scala.cgs.common.snapshot.CgsGameSnapshotEngine
import gg.scala.cgs.lobby.command.menu.RecentGamesMenu
import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.cgs.lobby.updater.CgsGameInfoUpdater
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bungee.BungeeUtil
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 2/22/2022
 */
object RecentGamesCommand : ScalaCommand()
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

    @CommandAlias("joingame|play")
    fun onJoinGame(player: Player, gameMode: String)
    {
        val gameModeType = CgsGameLobby.INSTANCE
            .getGameInfo().gameModes
            .firstOrNull {
                it.getId() == gameMode
            }
            ?: throw ConditionFailedException("${CC.YELLOW}$gameMode${CC.RED} is not a valid gamemode.")

        val server = CgsGameInfoUpdater.findAvailableServer(
            gameModeType, CgsGameLobby.INSTANCE.getGameInfo().fancyNameRender
        ) ?: throw ConditionFailedException("We couldn't find a suitable server for you to play on.")

        VelocityRedirectSystem.redirect(
            player, server.internalServerId
        )
    }
}
