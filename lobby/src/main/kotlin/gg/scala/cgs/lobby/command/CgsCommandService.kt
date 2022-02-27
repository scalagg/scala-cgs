package gg.scala.cgs.lobby.command

import gg.scala.cgs.lobby.CgsLobbyPlugin
import gg.scala.cgs.lobby.command.commands.LeaderboardPlacementCommand
import gg.scala.cgs.lobby.command.commands.RecentGamesCommand
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEngine
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.parties.service.PartyCommandService
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 1/22/2022
 */
@Service
object CgsCommandService
{
    @Configure
    fun configure()
    {
        val manager = CubedCommandManager(
            plugin = CgsLobbyPlugin.INSTANCE,
            primary = ChatColor.valueOf(Lemon.instance.lemonWebData.primary),
            secondary = ChatColor.valueOf(Lemon.instance.lemonWebData.secondary)
        )

        manager.commandContexts.registerContext(
            CgsLobbyRankingEntry::class.java
        ) {
            val firstArg = it.popFirstArg()

            return@registerContext CgsLobbyRankingEngine.findEntry(firstArg)
                ?: throw ConditionFailedException("There is no leaderboard with the name $firstArg")
        }

        manager.commandCompletions.registerCompletion(
            "leaderboards"
        ) {
            CgsLobbyRankingEngine.entries
                .map { it.getId() }
        }

        manager.registerCommand(LeaderboardPlacementCommand)
        manager.registerCommand(RecentGamesCommand)
    }
}
