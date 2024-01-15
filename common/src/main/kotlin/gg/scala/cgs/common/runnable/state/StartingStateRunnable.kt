package gg.scala.cgs.common.runnable.state

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.alive
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.runnable.StateRunnable
import gg.scala.cgs.common.startMessage
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object StartingStateRunnable : StateRunnable(
    CgsGameState.STARTING
)
{
    var hasBeenForceStarted = false
    var doWeCareAboutThis = false

    @JvmField
    var PRE_START_TIME = 0

    private val alertTicks = listOf(
        18000, 14400, 10800, 7200, 3600, 2700,
        1800, 900, 600, 300, 240, 180, 120,
        60, 50, 40, 30, 15, 10, 5, 4, 3, 2, 1
    )

    override fun configure()
    {
        val engine = CgsGameEngine.INSTANCE

        PRE_START_TIME = engine.gameInfo
            .startingCountdownSec + 1
    }

    override fun onTick()
    {
        val engine = CgsGameEngine.INSTANCE
        PRE_START_TIME--

        if (
            alive.size < engine.gameInfo.minimumPlayers &&
            !hasBeenForceStarted && doWeCareAboutThis
        )
        {
            engine.gameState = CgsGameState.WAITING
            return
        }

        if (alertTicks.contains(PRE_START_TIME))
        {
            val currentTitle = Title.title(
                Component.text(PRE_START_TIME)
                    .decorate(TextDecoration.BOLD)
                    .color(TextColor.fromHexString("#2acc29")),
                Component.text("The game is starting!")
            )

            engine.sendTitle(currentTitle)
            engine.playSound(Sound.NOTE_PLING, 1.0f)
            engine.sendMessage("${CC.SEC}The game begins in ${CC.PRI}${
                TimeUtil.formatIntoDetailedString((PRE_START_TIME))
            }${CC.SEC}.")
        }

        if (PRE_START_TIME <= 0)
        {
            if (alive.size < engine.gameInfo.minimumPlayers)
            {
                engine.gameState = CgsGameState.WAITING
                return
            }

            val currentTitle = Title.title(
                Component.text("BEGIN")
                    .decorate(TextDecoration.BOLD)
                    .color(TextColor.fromHexString("#2acc29")),
                Component.text("The game has started!")
            )

            engine.gameState = CgsGameState.STARTED

            engine.sendTitle(currentTitle)
            engine.playSound(Sound.NOTE_PLING, 1.5f)
            engine.sendMessage("${CC.GREEN}The game has started!")

            if (engine.gameInfo.gameVersion < 1.0)
            {
                engine.sendMessage(startMessage)
            }
        }
    }
}
