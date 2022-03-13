package gg.scala.parties.stream

import gg.scala.parties.model.Party
import net.evilblock.cubed.util.bukkit.FancyMessage

/**
 * @author GrowlyX
 * @since 12/31/2021
 */
object PartyMessageStream
{
    fun pushToStream(
        party: Party, message: FancyMessage
    )
    {
        party.members.values.toMutableList()
            .apply { add(party.leader) }
            .forEach {
                it.sendMessage(message)
            }
    }
}
