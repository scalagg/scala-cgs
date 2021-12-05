package gg.scala.ktp

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.lobby.updater.CgsGameInfoUpdater
import gg.scala.lemon.Lemon
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 12/5/2021
 */
object KillThePlayerScoreboard : ScoreboardAdapter()
{
    private val dateDisplay = TimeUtil.formatIntoDateString(Date())

    override fun getTitle(player: Player) = "${CC.B_GOLD}KTP"

    override fun getLines(board: LinkedList<String>, player: Player)
    {
        val cgsGamePlayer = CgsPlayerHandler.find(player)!!
        val statistics = cgsGamePlayer.gameSpecificStatistics["KillThePlayerCgsStatistics"]

        board.add("${CC.GRAY + dateDisplay} ${CC.D_GRAY + Lemon.instance.settings.id}")
        board.add("")
        board.add("${CC.WHITE}Lobby: ${CC.GREEN}${
            CgsGameInfoUpdater.lobbyTotalCount
        }")
        board.add("${CC.WHITE}Playing: ${CC.GREEN}${
            CgsGameInfoUpdater.playingTotalCount
        }")
        board.add("")
        board.add("${CC.WHITE}Total Kills: ${CC.GREEN}${
            statistics?.kills?.value ?: 0
        }")
        board.add("${CC.WHITE}Total Wins: ${CC.GREEN}${
            statistics?.wins?.value ?: 0
        }")
        board.add("")
        board.add("${CC.YELLOW}www.scala.gg")
    }
}
