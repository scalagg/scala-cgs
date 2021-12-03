package gg.scala.cgs.common.runnable.state

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.adventure
import gg.scala.cgs.common.giveCoins
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
object EndedStateRunnable : StateRunnable(
    CgsGameState.ENDED
)
{
    private val engine = CgsGameEngine.INSTANCE

    private val alertTicks = listOf(
        20, 15, 10, 5, 4, 3, 2, 1
    )

    override fun onTick()
    {
        if (currentTick == 0)
        {
            val description = mutableListOf<String>()
            description.add(" ")
            description.add(" ${CC.B_PRI}${engine.gameInfo.fancyNameRender} Game Overview:")
            description.add(" ${CC.GRAY}Duration: ${CC.WHITE}${
                DurationFormatUtils.formatDurationWords(
                    System.currentTimeMillis() - engine.gameStart,
                    true, true
                )
            }")
            description.add(" ${CC.GRAY}Winner${
                if (engine.gameMode.getTeamSize() == 1) "" else "s"
            }: ${CC.WHITE}${
                engine.winningTeam.getAlive().joinToString(
                    separator = ", "
                ) { 
                    Bukkit.getPlayer(it)?.name ?: "???"
                }
            }")

            description.add("")
            description.addAll(engine.getExtraWinInformation())
            description.add("")

            description.add(" ${CC.GREEN}Thanks for playing ${engine.gameInfo.fancyNameRender}!")
            description.add("")

            // lol makeshift solution for now
            engine.sendMessage(
                FancyMessage().withMessage(*description.toTypedArray())
            )

            val currentTitle = Title.title(
                Component.text("YOU WON")
                    .decorate(TextDecoration.BOLD)
                    .color(TextColor.fromHexString("#2acc29")),
                Component.text("Congratulations!")
            )

            engine.winningTeam.getAlive().forEach {
                val bukkitPlayer = Bukkit.getPlayer(it)
                    ?: return@forEach

                bukkitPlayer giveCoins (engine.gameInfo.awards.winningCoinRange.random() to "Winning a ${engine.gameInfo.fancyNameRender} game")
                bukkitPlayer adventure { audi ->
                    audi.showTitle(currentTitle)
                }
            }
        }

        if (alertTicks.contains(10 - currentTick))
        {
            engine.sendMessage("${CC.B_RED}The server will automatically reboot in ${10 - currentTick} seconds.")
        }

        if (currentTick == 10)
        {
            Bukkit.shutdown()
        }
    }
}
