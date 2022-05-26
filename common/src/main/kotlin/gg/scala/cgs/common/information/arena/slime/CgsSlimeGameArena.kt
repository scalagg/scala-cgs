package gg.scala.cgs.common.information.arena.slime

import com.grinderwolf.swm.api.SlimePlugin
import com.grinderwolf.swm.api.world.SlimeWorld
import gg.scala.cgs.common.information.arena.CgsGameArena
import gg.scala.commons.annotations.plugin.SoftDependency
import org.bukkit.Bukkit

@SoftDependency("SlimeWorldManager")
abstract class CgsSlimeGameArena : CgsGameArena
{
    init {
        val slime = (Bukkit.getPluginManager().getPlugin("SlimeWorldManager") as SlimePlugin)
        val loader = slime.getLoader("file")
        val world = slime.loadWorld(loader, getBukkitWorldName(), SlimeWorld.SlimeProperties.builder().build())
        slime.generateWorld(world)
    }
}