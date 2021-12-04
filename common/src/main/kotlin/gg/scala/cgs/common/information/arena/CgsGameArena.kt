package gg.scala.cgs.common.information.arena

import org.bukkit.Location
import org.bukkit.Material
import java.io.File
import java.nio.file.Path

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
interface CgsGameArena
{
    fun getId(): String

    fun getName(): String
    fun getMaterial(): Material
    fun getDescription(): String

    fun getDirectory(): Path
    fun getBukkitWorldName(): String

    fun getPreLobbyLocation(): Location
    fun getSpectatorLocation(): Location
}
