package gg.scala.cgs.lobby.customizer

import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEngine
import gg.scala.cgs.lobby.leaderboard.CgsLobbyRankingEntry
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager

/**
 * @author GrowlyX
 * @since 4/17/2022
 */
object CgsCommandCustomizer
{
    @CommandManagerCustomizer
    fun customize(
        manager: CubedCommandManager
    )
    {
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
    }
}
