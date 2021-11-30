package gg.scala.cgs.common.information.mode

import org.bukkit.Material

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
interface CgsGameMode
{
    fun getId(): String

    fun getName(): String
    fun getMaterial(): Material
    fun getDescription(): String

    fun getTeamSize(): Int
    fun getMaxTeams(): Int

    fun isSoloGame(): Boolean = getTeamSize() == 1
}
