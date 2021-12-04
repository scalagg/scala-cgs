package gg.scala.cgs.game

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.handler.CgsInstanceHandler
import gg.scala.cgs.common.information.arena.CgsGameArenaHandler
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.game.listener.CgsGameEventListener
import gg.scala.cgs.game.listener.CgsGameGeneralListener
import gg.scala.cgs.game.locator.CgsImplLocator
import gg.scala.cgs.game.scoreboard.CgsGameScoreboardProvider
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.lemon.Lemon
import net.evilblock.cubed.scoreboard.ScoreboardHandler
import org.bukkit.Bukkit
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsEnginePlugin : ExtendedScalaPlugin()
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<CgsEnginePlugin>()

        @JvmStatic
        var LOADING_STRING = ""
    }

    override fun enable()
    {
        INSTANCE = this
        logger.info("*** Attempting to find CGS Game implementation! ***")

        server.scheduler.runTaskTimerAsynchronously(this,
            {
                LOADING_STRING = if (LOADING_STRING == "") "." else if (LOADING_STRING == ".") ".." else if (LOADING_STRING == "..") "..." else ""
            }, 0L, 10L
        )

        CgsImplLocator.initialLoad {
            CgsInstanceHandler.initialLoad(CgsServerType.GAME_SERVER)

            Bukkit.getPluginManager().registerEvents(
                CgsGameEventListener, this
            )

            Bukkit.getPluginManager().registerEvents(
                CgsGameGeneralListener, this
            )

            invokeTrackedTask("game resource initialization") {
                CgsGameEngine.INSTANCE.initialResourceLoad()

                ScoreboardHandler.configure(
                    CgsGameScoreboardProvider(CgsGameEngine.INSTANCE)
                )
            }
        }
    }

    override fun disable()
    {
        CgsInstanceHandler.service.runCommand {
           it.hdel("cgs:servers", Lemon.instance.settings.id)
        }

        CgsGameArenaHandler.close()
    }
}
