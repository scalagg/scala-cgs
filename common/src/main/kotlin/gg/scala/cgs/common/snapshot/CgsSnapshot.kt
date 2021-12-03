package gg.scala.cgs.common.snapshot

import gg.scala.cgs.common.teams.CgsGameTeam
import java.util.*

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
interface CgsSnapshot
{
    fun getWinningTeam(): CgsGameTeam

    fun getExtraData(): Map<String, String>
}
