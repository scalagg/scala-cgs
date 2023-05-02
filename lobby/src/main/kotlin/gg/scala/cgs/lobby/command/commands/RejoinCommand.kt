package gg.scala.cgs.lobby.command.commands

import gg.scala.cgs.lobby.rejoin.GameRejoinService
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 5/2/2023
 */
object RejoinCommand : ScalaCommand()
{
    @CommandAlias("rejoin|plsletmebackin")
    fun onRejoin(player: ScalaPlayer)
    {
        val gameSave = GameRejoinService.gameSaves[player.uniqueId]
            ?: throw ConditionFailedException(
                "There is no game for you to rejoin."
            )

        if (System.currentTimeMillis() > gameSave.expirationTimestamp)
        {
            throw ConditionFailedException(
                "Oh noes! Your rejoin ticket expired! You can no longer join the game."
            )
        }

        player.bukkit().sendMessage(
            "${CC.B_GREEN}Found game! ${CC.GREEN}Attempting to put you back into the game..."
        )

        VelocityRedirectSystem
            .redirect(player.bukkit(), gameSave.serverId)
    }
}
