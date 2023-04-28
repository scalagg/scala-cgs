package gg.scala.cgs.common.teams

import java.util.*

/**
 * @author GrowlyX
 * @since 12/1/2021
 */
open class CgsGameTeam(
    val id: Int
)
{
    val participants = mutableListOf<UUID>()
    val eliminated = mutableSetOf<UUID>()

    var totalKills = 0

    val alive: List<UUID>
        get() = participants.filter { !eliminated.contains(it) }
}
