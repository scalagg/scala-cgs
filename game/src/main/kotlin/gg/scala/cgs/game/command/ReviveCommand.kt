package gg.scala.cgs.game.command

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.snapshot.inventory.CgsInventorySnapshotEngine
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.player.LemonPlayer
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 12/20/2021
 */
@AutoRegister
object ReviveCommand : ScalaCommand()
{
    @CommandAlias("revive")
    @CommandPermission("cgs.command.revive")
    fun onRevive(sender: CommandSender, target: LemonPlayer)
    {
        if (!target.bukkitPlayer!!.hasMetadata("spectator"))
            throw ConditionFailedException(
                "${target.getColoredName()}${CC.RED} is not spectating."
            )

        val snapshot = CgsInventorySnapshotEngine
            .snapshots[target.uniqueId]
            ?: throw ConditionFailedException("${target.getColoredName()}${CC.RED} is unable to be revived.")

        val playerReinstateEvent = CgsGameEngine
            .CgsGameParticipantReinstateEvent(
                target.bukkitPlayer!!, snapshot, false
            )

        playerReinstateEvent.callNow()

        sender.sendMessage("${CC.SEC}You've put ${target.getColoredName()}${CC.SEC} back into the game.")
    }
}
