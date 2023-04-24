package gg.scala.cgs.game.command

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.md_5.bungee.api.chat.ClickEvent

/**
 * @author GrowlyX
 * @since 4/23/2023
 */
@AutoRegister
object SpectateCommand : ScalaCommand()
{
    @CommandAlias("spectate|spec")
    fun onSpectate(player: ScalaPlayer, @Optional confirm: String?)
    {
        if (confirm == null)
        {
            if (CgsGameEngine.INSTANCE.gameState == CgsGameState.STARTED)
            {
                throw ConditionFailedException("You cannot become a spectator right now!")
            }

            if (player.bukkit().hasMetadata("spectator"))
            {
                player.sendMessage(
                    FancyMessage()
                        .withMessage("${CC.GRAY}Are you sure you want to leave spectator mode?"),
                    FancyMessage()
                        .withMessage("${CC.RED}[Confirm]")
                        .andHoverOf("${CC.RED}Leave spectator mode.")
                        .andCommandOf(
                            ClickEvent.Action.RUN_COMMAND,
                            "/spectate confirm"
                        )
                )
                return
            }

            player.sendMessage(
                FancyMessage()
                    .withMessage("${CC.GRAY}Are you sure you want to become a spectator?"),
                FancyMessage()
                    .withMessage("${CC.GREEN}[Confirm]")
                    .andHoverOf("${CC.GREEN}Enter spectator mode.")
                    .andCommandOf(
                        ClickEvent.Action.RUN_COMMAND,
                        "/spectate confirm"
                    )
            )
            return
        }
    }
}
