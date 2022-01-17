package gg.scala.cgs.game

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.enviornment.editor.EnvironmentEditor
import gg.scala.cgs.common.instance.handler.CgsInstanceHandler
import gg.scala.cgs.common.information.arena.CgsGameArenaHandler
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.game.listener.CgsGameEventListener
import gg.scala.cgs.game.listener.CgsGameGeneralListener
import gg.scala.cgs.game.locator.CgsInstanceLocator
import gg.scala.cgs.game.command.AnnounceCommand
import gg.scala.cgs.game.command.EditCommand
import gg.scala.cgs.game.command.ForceStartCommand
import gg.scala.cgs.game.command.ReviveCommand
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.handler.RedisHandler.buildMessage
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
@Plugin(
    name = "CGS-Engine",
    description = "CGS Game Engine",
    depends = [
        PluginDependency("Cubed"),
        PluginDependency("helper"),
        PluginDependency("Lemon")
    ]
)
class CgsEnginePlugin : ExtendedScalaPlugin()
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<CgsEnginePlugin>()

        @JvmStatic
        var LOADING_STRING = ""
    }

    override fun enable()
    {
        INSTANCE = this
        logger.info("*** Attempting to find CGS Game implementation! ***")

        server.scheduler.runTaskTimerAsynchronously(this,
            {
                LOADING_STRING = if (LOADING_STRING == "") "." else if (LOADING_STRING == ".") ".." else if (LOADING_STRING == "..") "..." else ""
            }, 0L, 10L
        )

        CgsInstanceLocator.initialLoad {
            CgsInstanceHandler.initialLoad(CgsServerType.GAME_SERVER)

            Bukkit.getPluginManager().registerEvents(
                CgsGameEventListener, this
            )

            Bukkit.getPluginManager().registerEvents(
                CgsGameGeneralListener, this
            )

            invokeTrackedTask("game resource initialization") {
                CgsGameEngine.INSTANCE.initialResourceLoad()

                val manager = CubedCommandManager(
                    CgsGameEngine.INSTANCE.plugin
                )

                Lemon.instance.registerCompletionsAndContexts(manager)

                manager.commandCompletions
                    .registerAsyncCompletion("fields") {
                        return@registerAsyncCompletion EnvironmentEditor
                            .editable.map { it.field.name }
                    }

                manager.registerCommand(AnnounceCommand)
                manager.registerCommand(ForceStartCommand)
                manager.registerCommand(EditCommand)
                manager.registerCommand(ReviveCommand)
            }

            buildMessage(
                "add-server",
                "id" to Lemon.instance
                    .settings.id,
                "address" to "127.0.0.1",
                "port" to Bukkit.getPort()
                    .toString()
            ).dispatch(
                "cocoa",
                Lemon.instance.banana
            )

            Tasks.asyncTimer({
                Lemon.instance.localInstance
                    .metaData["game-state"] = CgsGameEngine.INSTANCE
                        .gameState.name.replace("STARTED", "IN_GAME")

                Lemon.instance.localInstance
                    .metaData["remaining"] = Bukkit.getOnlinePlayers()
                    .count { !it.hasMetadata("spectator") }.toString()
            }, 0L, 20L)
        }
    }

    override fun disable()
    {
        buildMessage(
            "remove-server",
            "id" to Lemon.instance
                .settings.id
        ).dispatch(
            "cocoa",
            Lemon.instance.banana
        )

        Lemon.instance.banana.useResource {
           it.hdel("cgs:servers", Lemon.instance.settings.id)
        }

        CgsGameArenaHandler.close()
    }
}
