package gg.scala.cgs.common.deathmatch

import java.time.Duration

/**
 * @author GrowlyX
 * @since 8/10/2022
 */
interface DeathmatchConfiguration
{
    fun getTeleportationTime(): Duration
    fun getStartTime(): Duration

    fun getMinimumToStartDeathMatch(): Int

    fun onTeleporation()
    fun onStart()
}
