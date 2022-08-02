package gg.scala.cgs.common.voting

import gg.scala.cgs.common.voting.selection.VoteSelectionType
import org.bukkit.Location
import java.time.Duration

/**
 * @author GrowlyX
 * @since 7/16/2022
 */
interface VotingMapConfiguration
{
    val selectionType: VoteSelectionType

    val minimumPlayersForVotingStart: Int
    val votingAutoCloseDuration: Duration

    fun preStartLobby(): Location
    fun entries(): List<VotingMapEntry>
}
