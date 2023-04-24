package gg.scala.cgs.game.command

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.runnable.state.StartingStateRunnable
import gg.scala.cgs.common.voting.CgsVotingMapService
import gg.scala.cgs.game.listener.CgsGameEventListener
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.flavor.inject.Inject
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
@AutoRegister
object ForceStartCommand : ScalaCommand()
{
    val engine by lazy {
        CgsGameEngine.INSTANCE
    }

    @CommandAlias("force-start")
    @CommandPermission("op")
    fun onForceStart(sender: CommandSender)
    {
        if (Bukkit.getOnlinePlayers().size <= 1)
        {
            throw ConditionFailedException("You cannot start the game when you are alone.")
        }

        if (engine.gameState.isAfter(CgsGameState.STARTING))
        {
            throw ConditionFailedException("You cannot start the game at this time.")
        }

        StartingStateRunnable.hasBeenForceStarted = true

        val cgsGameForceStart = CgsGameEngine
            .CgsGameForceStartEvent(sender)

        cgsGameForceStart.callNow()

        if (cgsGameForceStart.isCancelled)
        {
            return
        }

        engine.onAsyncPreStartResourceInitialization()
            .thenAccept {
                engine.gameState = CgsGameState.STARTING
                engine.sendMessage(
                    "${CC.GREEN}The game has been started. ${CC.GRAY}(by ${
                        if (sender is Player) sender.name else "Console"
                    })"
                )

                StartingStateRunnable.PRE_START_TIME = 11
            }
    }

    @CommandAlias("force-start-voting")
    @CommandPermission("op")
    fun onForceStartVoting(sender: CommandSender)
    {
        if (engine.getVotingConfig() == null)
        {
            throw ConditionFailedException("This game does not support voting.")
        }

        if (Bukkit.getOnlinePlayers().size <= 1)
        {
            throw ConditionFailedException("You cannot force-start voting when you are alone.")
        }

        if (CgsVotingMapService.votingEnabled)
        {
            throw ConditionFailedException("Voting is already enabled.")
        }

        if (CgsVotingMapService.votingFinished)
        {
            throw ConditionFailedException("Voting has been finished.")
        }

        CgsVotingMapService.votingForceStarted = true
        CgsVotingMapService.start()
    }
}
