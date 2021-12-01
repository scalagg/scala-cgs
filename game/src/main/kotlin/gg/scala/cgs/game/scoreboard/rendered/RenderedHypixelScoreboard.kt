package gg.scala.cgs.game.scoreboard.rendered

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.renderer.CgsGameScoreboardRenderer
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object RenderedHypixelScoreboard : CgsGameScoreboardRenderer
{
    override fun getTitle() = "${CC.B_GOLD}${
        CgsGameEngine.INSTANCE.gameInfo.fancyNameRender.uppercase()
    }"

    override fun getFooter() = listOf(
        "",
        "${CC.SEC}www.tropic.gg"
    )

    override fun getHeader() = listOf(
        "${CC.GRAY}11/11/21  ${CC.D_GRAY}m1",
        ""
    )

    override fun render(lines: LinkedList<String>, player: Player, state: CgsGameState)
    {
        val boardLines = mutableListOf<String>()

        when (state)
        {
            CgsGameState.WAITING, CgsGameState.STARTING ->
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
                boardLines.add("")
                boardLines.add("")

                boardLines.add("Mode: ${CC.GREEN}${
                    CgsGameEngine.INSTANCE.gameMode.getName()
                }")
                boardLines.add("Version: ${CC.GRAY}${
                    CgsGameEngine.INSTANCE.gameInfo.gameVersion
                }")
            }
            else -> boardLines.add("")
        }

        if (state == CgsGameState.WAITING)
        {
            boardLines[4] = "Waiting..."
        } else if (state == CgsGameState.STARTING)
        {
            boardLines[4] = "Starting in ${CC.GREEN}10s"
        }
    }
}
