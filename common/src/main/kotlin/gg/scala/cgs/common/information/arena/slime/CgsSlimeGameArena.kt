package gg.scala.cgs.common.information.arena.slime

import com.grinderwolf.swm.api.SlimePlugin
import com.grinderwolf.swm.api.world.SlimeWorld
import gg.scala.cgs.common.information.arena.CgsGameArena
import org.bukkit.Bukkit
import org.bukkit.Difficulty

abstract class CgsSlimeGameArena : CgsGameArena
{
    override fun getBukkitWorldName(): String
    {
        return getId() + "-clone"
    }

    init {
        val slime = (Bukkit.getPluginManager().getPlugin("SlimeWorldManager") as SlimePlugin)
        val loader = slime.getLoader("file")
        val world = slime.loadWorld(loader, getId(), SlimeWorld.SlimeProperties.builder().allowAnimals(true).difficulty(2).build()).clone(getBukkitWorldName())
        slime.generateWorld(world)
    }
}