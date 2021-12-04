package gg.scala.ktp

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.scoreboard.CgsGameScoreboardRenderer
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.game.CgsEnginePlugin
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object KillThePlayerCgsBoardRenderer : CgsGameScoreboardRenderer
{
    override fun getTitle() = "${CC.B_GOLD}KTP"

    override fun getFooter() = listOf(
        "", "${CC.YELLOW}www.scala.gg"
    )

    override fun getHeader() = listOf(
        "${CC.GRAY}2/3/21  ${CC.D_GRAY}ktp1"
    )

    override fun render(lines: LinkedList<String>, player: Player, state: CgsGameState)
    {
        val boardLines = mutableListOf<String>()

        if (state == CgsGameState.WAITING || state == CgsGameState.STARTING)
        {
            boardLines.add("")
            boardLines.add("Map: ${CC.GREEN}${
                CgsGameEngine.INSTANCE.gameArena.getId()
            }")
            boardLines.add("Players: ${CC.GREEN}${
                Bukkit.getOnlinePlayers().size
            }/${
                Bukkit.getMaxPlayers()
            }")

            boardLines.add("")

            if (state == CgsGameState.WAITING)
            {
                boardLines.add("Waiting${
                    CgsEnginePlugin.LOADING_STRING
                }")
            } else if (state == CgsGameState.STARTING)
            {
                boardLines.add("Starting in ${CC.GREEN}${StartingStateRunnable.current}s")
            }

            boardLines.add("")
            boardLines.add("Mode: ${CC.GREEN}${
                CgsGameEngine.INSTANCE.gameMode.getName()
            }")
            boardLines.add("Version: ${CC.GRAY}${
                CgsGameEngine.INSTANCE.gameInfo.gameVersion
            }")
        } else if (state.isAfter(CgsGameState.STARTED))
        {
            val cgsGamePlayer = CgsPlayerHandler.find(player)!!

            val statistics = KillThePlayerCgsEngine.INSTANCE
                .getStatistics(cgsGamePlayer)

            boardLines.add("")
            boardLines.add("Kills: ${CC.GREEN}${statistics.kills.value}")
        }

        lines.addAll(boardLines)
    }
}
