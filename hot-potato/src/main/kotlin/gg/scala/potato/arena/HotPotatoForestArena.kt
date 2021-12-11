package gg.scala.potato.arena

import gg.scala.cgs.common.information.arena.CgsGameArena
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object HotPotatoForestArena : CgsGameArena
{
    override fun getId() = "forest"
    override fun getName() = "Forest"

    override fun getMaterial() = Pair(Material.SAPLING, 2)
    override fun getDescription() = "A forest arena."

    override fun getDirectory(): Path = Paths.get(
        "/root/home/cgs/hot-potato/forest"
    )

    override fun getBukkitWorldName() = "forest"

    override fun getPreLobbyLocation() = Location(
        Bukkit.getWorld("forest"), 1.0, 64.0, 1.0
    )

    override fun getSpectatorLocation() = Location(
        Bukkit.getWorld("forest"), 1.0, 64.0, 1.0
    )
}
