package gg.scala.cgs.common.information.arena

import gg.scala.cgs.common.information.mode.CgsGameMode
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import java.io.File
import java.nio.file.Files

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsGameArenaHandler
{
    lateinit var world: World
    lateinit var arena: CgsGameArena

    fun initialLoad(gameMode: CgsGameMode)
    {
        arena = gameMode.getArenas().random()
        val directory = arena.getDirectory()

        FileUtils.copyDirectory(directory.toFile(), Bukkit.getWorldContainer(), true)

        world = Bukkit.createWorld(
            WorldCreator(directory.toFile().name)
                .environment(World.Environment.NORMAL)
                .type(WorldType.NORMAL)
        )
        world.setGameRuleValue("doMobSpawning", "false")
        world.setGameRuleValue("", "")
    }

    fun close()
    {
        Bukkit.unloadWorld(world, false)
        Files.delete(File(Bukkit.getWorldContainer(), arena.getBukkitWorldName()).toPath())
    }
}
