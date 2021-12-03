package gg.scala.cgs.common.handler

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.instance.CgsServerInstance
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.common.instance.game.CgsGameServerInfo
import gg.scala.lemon.Lemon

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsInstanceHandler
{
    lateinit var current: CgsServerInstance

    fun initialLoad(type: CgsServerType)
    {
        current = CgsServerInstance(
            Lemon.instance.settings.id, type
        )

        if (type == CgsServerType.GAME_SERVER)
        {
            current.gameServerInfo = CgsGameServerInfo(
                CgsGameEngine.INSTANCE.uniqueId
            )
        }
    }
}
