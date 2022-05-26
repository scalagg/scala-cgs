package gg.scala.cgs.game.command

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.flavor.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
@AutoRegister
object ForceStartCommand : ScalaCommand()
{
    @Inject
    lateinit var engine: CgsGameEngine<*>

    @CommandAlias("force-start")
    @CommandPermission("op")
    fun onForceStart(sender: CommandSender)
    {
        if (Bukkit.getOnlinePlayers().size <= 1)
        {
            throw ConditionFailedException("You cannot force-start the game when you are alone.")
        }

        if (engine.gameState.isAfter(CgsGameState.STARTING))
        {
            throw ConditionFailedException("You cannot force-start the game at this time.")
        }

        StartingStateRunnable.hasBeenForceStarted = true

        engine.onAsyncPreStartResourceInitialization()
            .thenAccept {
                engine.gameState = CgsGameState.STARTING

                val cgsGameForceStart = CgsGameEngine
                    .CgsGameForceStartEvent(sender)

                cgsGameForceStart.callNow()

                StartingStateRunnable.PRE_START_TIME = 11
            }
    }
}
