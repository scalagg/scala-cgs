package gg.scala.cgs.common.scoreboard

import gg.scala.cgs.common.CgsGameState
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
interface CgsGameScoreboardRenderer
{
    fun getTitle(): String
    fun getFooter(): List<String>
    fun getHeader(): List<String>

    fun render(lines: LinkedList<String>, player: Player, state: CgsGameState)
}
