package gg.scala.cgs.common.instance.game

import java.util.*

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsGameServerInfo(
    val uniqueId: UUID
)
{
    var participants = mutableListOf<UUID>()
    var spectators = mutableListOf<UUID>()
}
