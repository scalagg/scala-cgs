package gg.scala.cgs.common.information.arena

import com.google.common.io.Files
import gg.scala.cgs.common.information.mode.CgsGameMode
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import java.io.File
import kotlin.io.path.name

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
        )
    }

    fun close()
    {
        java.nio.file.Files.delete(
            File(Bukkit.getWorldContainer(), arena.getBukkitWorldName()).toPath()
        )
        Bukkit.unloadWorld(world, false)
    }
}
