package gg.scala.cgs.game

import gg.scala.cgs.common.handler.CgsInstanceHandler
import gg.scala.cgs.common.instance.CgsServerType
import me.lucko.helper.plugin.ExtendedJavaPlugin

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsEnginePlugin : ExtendedJavaPlugin()
{
    override fun enable()
    {
        CgsInstanceHandler.initialLoad(CgsServerType.GAME_SERVER)
    }
}
