package gg.scala.cgs.common.runnable.state

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.runnable.StateRunnable
import gg.scala.lemon.Lemon
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.time.TimeUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.md_5.bungee.api.chat.ClickEvent
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
    private val engine = CgsGameEngine.INSTANCE

    var hasBeenForceStarted = false

    var startingTime = engine.gameInfo
        .startingCountdownSec + 1

    private val alertTicks = listOf(
        18000, 14400, 10800, 7200, 3600, 2700,
        1800, 900, 600, 300, 240, 180, 120,
        60, 50, 40, 30, 15, 10, 5, 4, 3, 2, 1
    )

    private val rangeToColor = mutableMapOf(
        0..5 to CC.RED,
        5..10 to CC.GOLD
    )

    override fun onTick()
    {
        startingTime--

        if (Bukkit.getOnlinePlayers().size < engine.gameInfo.minimumPlayers && !hasBeenForceStarted)
        {
            engine.gameState = CgsGameState.WAITING
            return
        }

        if (alertTicks.contains(startingTime))
        {
            val currentTitle = Title.title(
                Component.text(startingTime)
                    .decorate(TextDecoration.BOLD)
                    .color(TextColor.fromHexString("#2acc29")),
                Component.text("The game is starting!")
            )

            engine.sendTitle(currentTitle)
            engine.playSound(Sound.ORB_PICKUP)
            engine.broadcast("${CC.SEC}The game starts in ${getCurrentColor()}${
                TimeUtil.formatIntoDetailedString((startingTime))
            }${CC.SEC}.")
        }

        if (startingTime <= 0)
        {
            val currentTitle = Title.title(
                Component.text("BEGIN")
                    .decorate(TextDecoration.BOLD)
                    .color(TextColor.fromHexString("#2acc29")),
                Component.text("The game has started!")
            )

            engine.gameState = CgsGameState.STARTED

            engine.sendTitle(currentTitle)
            engine.playSound(Sound.LEVEL_UP)
            engine.broadcast("${CC.GREEN}The game has commenced!")

            if (engine.gameInfo.gameVersion < 1.0)
            {
                val fancyMessage = FancyMessage()
                fancyMessage.withMessage(
                    "",
                    " ${CC.B_RED}${engine.gameInfo.fancyNameRender} is currently in BETA!",
                    " ${CC.RED}Remember, there may be bugs/incomplete features!",
                    "",
                    " ${CC.RED}If you think you have found a bug, report it at:",
                    " ${CC.WHITE}${Lemon.instance.lemonWebData.discord}",
                    ""
                )
                fancyMessage.andHoverOf(
                    "${CC.WHITE}Click to join our discord!"
                )
                fancyMessage.andCommandOf(
                    ClickEvent.Action.OPEN_URL,
                    Lemon.instance.lemonWebData.discord
                )

                engine.broadcast(fancyMessage)
            }
        }
    }

    private fun getCurrentColor(): String
    {
        return rangeToColor.entries
            .firstOrNull { it.key.contains(startingTime) }?.value
            ?: CC.GREEN
    }
}
