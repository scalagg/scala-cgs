package gg.scala.cgs.common.runnable.state

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.runnable.StateRunnable
import gg.scala.cgs.common.teams.CgsGameTeamEngine
import gg.scala.lemon.Lemon
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.md_5.bungee.api.chat.ClickEvent
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.Bukkit
import org.bukkit.Sound

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object StartedStateRunnable : StateRunnable(
    CgsGameState.STARTED
)
{
    private val engine = CgsGameEngine.INSTANCE

    override fun onTick()
    {
        val teamsWithAlivePlayers = CgsGameTeamEngine.teams
            .values.filter { it.getAlive().isNotEmpty() }

        if (teamsWithAlivePlayers.size == 1)
        {
            // This runnable is run asynchronously
            Tasks.sync {
                engine.winningTeam = teamsWithAlivePlayers[0]
                engine.gameState = CgsGameState.ENDED
            }
        } else if (Bukkit.getOnlinePlayers().isEmpty() || teamsWithAlivePlayers.isEmpty())
        {
            Bukkit.shutdown()
        }
    }
}
