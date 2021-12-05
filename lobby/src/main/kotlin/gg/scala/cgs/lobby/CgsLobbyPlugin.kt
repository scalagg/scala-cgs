package gg.scala.cgs.lobby

import gg.scala.cgs.lobby.command.LeaderboardPlacementCommand
import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEngine
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry
import gg.scala.cgs.lobby.locator.CgsInstanceLocator
import gg.scala.commons.ExtendedScalaPlugin
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsLobbyPlugin : ExtendedScalaPlugin()
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<CgsLobbyPlugin>()
    }

    override fun enable()
    {
        INSTANCE = this

        CgsInstanceLocator.initialLoad {
            invokeTrackedTask("lobby loading") {
                CgsGameLobby.INSTANCE.initialResourceLoad()
            }

            registerDefaultCommands()
        }
    }

    private fun registerDefaultCommands()
    {
        val manager = CubedCommandManager(this)

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
    }
}
