package gg.scala.cgs.common.scoreboard

import gg.scala.cgs.common.CgsGameEngine
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsGameScoreboardProvider(
    private val engine: CgsGameEngine<*>
) : ScoreboardAdapter()
{
    override fun getLines(board: LinkedList<String>, player: Player)
    {
        val renderer = engine.getScoreboardRenderer()
        board.addAll(renderer.getHeader())
        renderer.render(board, player, engine.gameState)

        board.addAll(renderer.getFooter())
    }

    override fun getTitle(player: Player) = engine.getScoreboardRenderer().getTitle()
}
