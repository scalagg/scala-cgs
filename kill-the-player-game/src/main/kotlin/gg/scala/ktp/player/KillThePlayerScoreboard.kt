package gg.scala.ktp.player

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.scoreboard.CgsGameScoreboardRenderer
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.game.CgsEnginePlugin
import gg.scala.ktp.KillThePlayerCgsEngine
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
object KillThePlayerScoreboard : CgsGameScoreboardRenderer
{
    private val dateDisplay = TimeUtil.formatIntoDateString(Date())

    override fun getTitle() = "${CC.B_GOLD}KTP"

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
                lines.add("Starting in ${CC.GREEN}${TimeUtil.formatIntoAbbreviatedString(StartingStateRunnable.PRE_START_TIME)}")
            }

            lines.add("")
            lines.add("Mode: ${CC.GREEN}${
                CgsGameEngine.INSTANCE.gameMode.getName()
            }")
            lines.add("Version: ${CC.GRAY}${
                CgsGameEngine.INSTANCE.gameInfo.gameVersion
            }")
        } else if (state.isAfter(CgsGameState.STARTED))
        {
            val cgsGamePlayer = CgsPlayerHandler.find(player)!!

            val statistics = KillThePlayerCgsEngine.INSTANCE
                .getStatistics(cgsGamePlayer)

            lines.add("")
            lines.add("Kills: ${CC.GREEN}${statistics.kills.value}")
        }

        lines.add("")
        lines.add("${CC.YELLOW}www.scala.gg")
    }
}
