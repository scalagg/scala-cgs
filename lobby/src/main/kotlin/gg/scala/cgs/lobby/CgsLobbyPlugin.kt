package gg.scala.cgs.lobby

import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.cgs.lobby.locator.CgsInstanceLocator
import gg.scala.commons.ExtendedScalaPlugin
import me.lucko.helper.plugin.ExtendedJavaPlugin
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsLobbyPlugin : ExtendedScalaPlugin()
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<CgsLobbyPlugin>()
    }

    override fun enable()
    {
        INSTANCE = this

        CgsInstanceLocator.initialLoad {
            invokeTrackedTask("lobby loading") {
                CgsGameLobby.INSTANCE.initialResourceLoad()
            }
        }
    }
}
