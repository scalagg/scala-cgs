package gg.scala.cgs.game.locator

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.game.CgsEnginePlugin
import me.lucko.helper.Schedulers
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsImplLocator : BukkitRunnable()
{
    var found = false
    private var attempts = -1

    fun initialLoad()
    {
        Schedulers.sync().runRepeating(
            this, 0L, 20L
        )
    }

    override fun run()
    {
        attempts++

        try
        {
            // The not null delegate will throw an exception
            val engine = CgsGameEngine.INSTANCE

            println(
                """
                    *** IMPLEMENTATION WAS FOUND! INFORMATION BELOW ***
                    *** MiniGame: ${engine.gameInfo.fancyNameRender} v${engine.gameInfo.gameVersion} ***
                    *** Game Mode: ${engine.gameMode.getName()} ***
                    *** Map: Not yet initialized... ***
                """.trimIndent()
            )

            CgsEnginePlugin.INSTANCE.logger.info(
                "CGS Game Server found an implementation. Now booting into the WAITING state..."
            )

            found = true

            cancel()
        } catch (ignored: Exception)
        {
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
