package gg.scala.cgs.common.information.arena

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Service
import me.lucko.helper.scheduler.threadlock.ServerThreadLock
import net.evilblock.cubed.util.bukkit.Tasks
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import java.io.File

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
@Service
object CgsGameArenaHandler
{
    @Inject
    lateinit var engine: CgsGameEngine<*>

    lateinit var world: World
    lateinit var arena: CgsGameArena

    fun configure(
        gameMode: CgsGameMode,
        arenaOverride: CgsGameArena? = null
    )
    {
        arena = arenaOverride
            ?: gameMode.getArenas().random()

        val directory = arena.getDirectory()
            ?: return

        val childDirectory = File(
            Bukkit.getWorldContainer(),
            arena.getBukkitWorldName()
        )

        FileUtils.copyDirectory(directory.toFile(), childDirectory, true)
    }

    @Close
    fun close()
    {
        kotlin
            .runCatching {
                world
            }
            .onSuccess {
                kotlin
                    .runCatching {
                        world.worldFolder.deleteRecursively()
                    }
                    .onFailure {
                        it.printStackTrace()
                    }
            }
    }
}
