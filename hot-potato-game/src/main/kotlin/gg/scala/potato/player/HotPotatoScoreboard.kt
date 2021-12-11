package gg.scala.potato.player

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.scoreboard.CgsGameScoreboardRenderer
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.game.CgsEnginePlugin
import gg.scala.potato.HotPotatoEngine
import gg.scala.lemon.Lemon
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object HotPotatoScoreboard : CgsGameScoreboardRenderer
{
    private val dateDisplay = TimeUtil.formatIntoDateString(Date())

    override fun getTitle() = "${CC.B_GOLD}Hot Potato"

    override fun render(lines: LinkedList<String>, player: Player, state: CgsGameState)
    {
        lines.add("${CC.GRAY + dateDisplay} ${CC.D_GRAY + Lemon.instance.settings.id}")

        if (state == CgsGameState.WAITING || state == CgsGameState.STARTING)
        {
            lines.add("")
            lines.add("Map: ${CC.GREEN}${
                CgsGameEngine.INSTANCE.gameArena.getId()
            }")
            lines.add("Players: ${CC.GREEN}${
                Bukkit.getOnlinePlayers().size
            }/${
                Bukkit.getMaxPlayers()
            }")

            lines.add("")

            if (state == CgsGameState.WAITING)
            {
                lines.add("Waiting${
                    CgsEnginePlugin.LOADING_STRING
                }")
            } else if (state == CgsGameState.STARTING)
            {
                lines.add("Starting in ${CC.GREEN}${TimeUtil.formatIntoAbbreviatedString(StartingStateRunnable.startingTime)}")
                lines.add("to allow time for")
                lines.add("additional players")
            }

            lines.add("")
            lines.add("Version: ${CC.GRAY}${
                CgsGameEngine.INSTANCE.gameInfo.gameVersion
            }")
        } else if (state.isAfter(CgsGameState.STARTED))
        {
            lines.add("")
            lines.add("${CC.WHITE}Growly${CC.YELLOW} will burn in")
            lines.add("${CC.GREEN}30 seconds${CC.YELLOW}!")
            lines.add("")
            lines.add("Alive: ${CC.GREEN}40 players")
            lines.add("")
            lines.add("${CC.GRAY}Round #1")
        }

        lines.add("")
        lines.add("${CC.YELLOW}www.scala.gg")
    }
}
