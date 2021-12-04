package gg.scala.ktp.arena

import gg.scala.cgs.common.information.arena.CgsGameArena
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object KillThePlayerNv6Arena : CgsGameArena
{
    override fun getId() = "nv6"
    override fun getName() = "Novel Virus 6"

    override fun getMaterial() = Material.SPIDER_EYE
    override fun getDescription() = "Stay away, he's coming to ruin 2022."

    override fun getDirectory(): Path = Paths.get(
        "/root/home/cgs/ktp/nv6"
    )

    override fun getBukkitWorldName() = "nv6"

    override fun getPreLobbyLocation() = Location(
        Bukkit.getWorld("nv6"), 1.0, 64.0, 1.0
    )

    override fun getSpectatorLocation() = Location(
        Bukkit.getWorld("nv6"), 1.0, 64.0, 1.0
    )
}
