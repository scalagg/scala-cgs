package gg.scala.cgs.common.voting.event

import gg.scala.cgs.common.voting.VotingMapEntry
import net.evilblock.cubed.event.PluginEvent

/**
 * @author GrowlyX
 * @since 7/16/2022
 */
class VoteCompletionEvent(
    val selected: VotingMapEntry,
    val tie: Boolean
) : PluginEvent()
