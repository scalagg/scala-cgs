package gg.scala.cgs.common.teams

import java.util.*

/**
 * @author GrowlyX
 * @since 12/1/2021
 */
class CgsGameTeam(
    val id: Int
)
{
    val participants = mutableSetOf<UUID>()
    val eliminated = mutableSetOf<UUID>()

    fun getAlive(): List<UUID> = participants
        .filter { !eliminated.contains(it) }
}
