package gg.scala.cgs.common.runnable.state

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.adventure
import gg.scala.cgs.common.giveCoins
import gg.scala.cgs.common.login.PlayerLoginService
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.runnable.StateRunnable
import gg.scala.cgs.common.snapshot.CgsGameSnapshotEngine
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.scala.lemon.util.CubedCacheUtil
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object EndedStateRunnable : StateRunnable(
    CgsGameState.ENDED
)
{
    @JvmField
    var ALLOWED_TO_JOIN = true

    private val alertTicks = listOf(
        60, 50, 40, 30, 20, 15, 10, 5, 4, 3, 2, 1
    )

    override fun onTick()
    {
        val engine = CgsGameEngine.INSTANCE

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
                engine.winningTeam.alive.joinToString(
                    separator = ", "
                ) { 
                    Bukkit.getPlayer(it)?.name ?: "???"
                }
            }")

            val info = engine.getGameSnapshot().getExtraInformation()

            if (info.isNotEmpty())
            {
                description.add("")
                description.addAll(info)
                description.add("")
            }

            description.add(" ${CC.GREEN}Thanks for playing ${engine.gameInfo.fancyNameRender}!")
            description.add("")

            engine.sendMessage(description)

            val currentTitle = Title.title(
                Component.text("YOU WON")
                    .decorate(TextDecoration.BOLD)
                    .color(TextColor.fromHexString("#2acc29")),
                Component.text("Congratulations!")
            )

            engine.winningTeam.alive.forEach {
                val bukkitPlayer = Bukkit.getPlayer(it)
                    ?: return@forEach
                val cgsGamePlayer = CgsPlayerHandler.find(bukkitPlayer)!!

                val statistics = engine.getStatistics(cgsGamePlayer)
                statistics.wins++

                bukkitPlayer giveCoins (engine.gameInfo.awards.winningCoinRange.random() to "Winning a ${engine.gameInfo.fancyNameRender} game")
                bukkitPlayer adventure { audi ->
                    audi.showTitle(currentTitle)
                }
            }

            CgsGameSnapshotEngine.submitWrappedSnapshot()
        }

        if (alertTicks.contains(engine.gameInfo.timeUntilShutdown - currentTick))
        {
            engine.sendMessage("${CC.RED}The server will automatically reboot in ${engine.gameInfo.timeUntilShutdown - currentTick} seconds.")
        }

        if (currentTick >= engine.gameInfo.timeUntilShutdown)
        {
            val kickMessage = CC.YELLOW + engine.winningTeam.alive.joinToString(
                separator = "${CC.GREEN}, ${CC.YELLOW}"
            ) {
                CubedCacheUtil.fetchName(it) ?: "???"
            } + CC.GREEN + " won the game. Thanks for playing!"

            Tasks.sync {
                for (onlinePlayer in Bukkit.getOnlinePlayers())
                {
                    onlinePlayer.kickPlayer(kickMessage)

                    val cached = PlayerLoginService
                        .cached[onlinePlayer.uniqueId]

                    if (cached == null)
                    {
                        onlinePlayer.kickPlayer("")
                        continue
                    }

                    VelocityRedirectSystem
                        .redirect(onlinePlayer, cached)
                }
            }

            ALLOWED_TO_JOIN = false

            Schedulers.sync().runLater(
                {
                    Bukkit.shutdown()
                }, 40L
            )
        }
    }
}
