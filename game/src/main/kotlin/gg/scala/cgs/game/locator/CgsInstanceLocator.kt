package gg.scala.cgs.game.locator

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.game.CgsEnginePlugin
import me.lucko.helper.Schedulers
import me.lucko.helper.scheduler.Task
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsInstanceLocator : Runnable
{
    var found = false

    private var attempts = -1
    private var lambda = {}

    private lateinit var task: Task

    fun initialLoad(lambda: () -> Unit)
    {
        task = Schedulers.sync().runRepeating(
            this, 0L, 20L
        )

        this.lambda = lambda
    }

    override fun run()
    {
        if (found)
        {
            task.closeAndReportException()
            return
        }

        attempts++

        try
        {
            // The not null delegate will throw an exception
            val engine = CgsGameEngine.INSTANCE

            println(
                """
                    *** IMPLEMENTATION WAS FOUND! INFORMATION BELOW ***
                    *** Mini-game: ${engine.gameInfo.fancyNameRender} v${engine.gameInfo.gameVersion} ***
                    *** Game Mode: ${engine.gameMode.getName()} ***
                    *** Map: ${engine.gameArena.getId()} ***
                """.trimIndent()
            )

            CgsEnginePlugin.INSTANCE.logger.info(
                "CGS found an implementation, now booting into the WAITING state..."
            )

            task.closeAndReportException()

            found = true
            lambda.invoke()
        } catch (e: Exception)
        {
            e.printStackTrace()

            if (attempts >= 5)
            {
                CgsEnginePlugin.INSTANCE.logger.severe(
                    "*** IMPLEMENTATION WAS NOT FOUND, SHUTTING DOWN ***"
                )

                Bukkit.shutdown()
                return
            }

            CgsEnginePlugin.INSTANCE.logger.warning(
                "Waiting for implementation to be located..."
            )
        }
    }
}
