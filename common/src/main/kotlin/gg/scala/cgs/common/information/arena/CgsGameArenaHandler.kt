package gg.scala.cgs.common.information.arena

import gg.scala.cgs.common.information.mode.CgsGameMode
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsGameArenaHandler
{
    lateinit var world: World
    lateinit var path: Path

    fun initialLoad(gameMode: CgsGameMode)
    {
        val random = gameMode.getArenas().random()
        val directory = random.getDirectory()

        // copy/replacing the previous world
        path = Files.copy(
            directory.toPath(),
            Bukkit.getWorldContainer().toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        world = Bukkit.createWorld(
            WorldCreator(directory.name)
        )
    }

    fun close()
    {
        Files.delete(path)
        Bukkit.unloadWorld(world, false)
    }
}
