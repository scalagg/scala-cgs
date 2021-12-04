package gg.scala.cgs.game.command

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object ForceStartCommand : BaseCommand()
{
    private val engine = CgsGameEngine.INSTANCE

    @CommandAlias("force-start")
    @CommandPermission("op")
    fun onForceStart(sender: CommandSender)
    {
        if (Bukkit.getOnlinePlayers().size <= 1)
        {
            throw ConditionFailedException("You cannot force-start the game when you are alone.")
        }

        if (engine.gameState.isAfter(CgsGameState.STARTED))
        {
            throw ConditionFailedException("You cannot force-start the game at this time.")
        }

        val cgsGameForceStart = CgsGameEngine
            .CgsGameForceStartEvent(sender)
        engine.gameState = CgsGameState.STARTING

        cgsGameForceStart.callNow()
    }
}
