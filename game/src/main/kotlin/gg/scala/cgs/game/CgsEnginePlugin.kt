package gg.scala.cgs.game

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.handler.CgsInstanceHandler
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.game.listener.CgsGameEventListener
import gg.scala.cgs.game.listener.CgsGameGeneralListener
import gg.scala.cgs.game.locator.CgsImplLocator
import gg.scala.commons.ExtendedScalaPlugin
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
    }

    override fun enable()
    {
        INSTANCE = this
        logger.info("*** Attempting location of CGS Game implementation! ***")

        CgsImplLocator.initialLoad {
            Bukkit.getPluginManager().registerEvents(
                CgsGameEventListener, this
            )

            Bukkit.getPluginManager().registerEvents(
                CgsGameGeneralListener, this
            )

            invokeTrackedTask("game resource initialization") {
                CgsGameEngine.INSTANCE.initialResourceLoad()
            }

            invokeTrackedTask("entity event & instance initialization") {
                CgsInstanceHandler.initialLoad(CgsServerType.GAME_SERVER)
            }
        }
    }
}
