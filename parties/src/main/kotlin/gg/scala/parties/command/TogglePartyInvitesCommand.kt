package gg.scala.parties.command

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
object TogglePartyInvitesCommand : BaseCommand()
{
    @CommandAlias("togglepartyinvites|tpi")
    fun onToggleFriendRequests(player: Player)
    {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)
        val partyInvites = lemonPlayer.getSetting("party-invites-disabled")

        if (partyInvites)
        {
            lemonPlayer remove "party-invites-disabled"
            player.sendMessage("${CC.GREEN}You've enabled party invitations.")
        } else
        {
            lemonPlayer.updateOrAddMetadata("party-invites-disabled", Metadata(true))
            player.sendMessage("${CC.RED}You've disabled party invitations.")
        }
    }
}
