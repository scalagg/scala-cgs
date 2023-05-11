package gg.scala.cgs.common.deathmatch

import java.time.Duration

/**
 * @author GrowlyX
 * @since 8/10/2022
 */
enum class DeathmatchStartStrategy
{
    Timed, AlivePlayerCount, Both
}

interface DeathmatchConfiguration
{
    fun getTeleportationTime(): Duration
    fun getStartTime(): Duration
    fun timeUntilForcedDeathmatch(): Duration

    fun getMinimumToStartDeathMatch(): Int
    fun deathmatchStartStrategy(): DeathmatchStartStrategy

    fun onTeleporation()
    fun onStart()
}
