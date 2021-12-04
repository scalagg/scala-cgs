package gg.scala.ktp

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.snapshot.CgsSnapshot

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
object KillThePlayerCgsSnapshot : CgsSnapshot
{
    override fun getWinningTeam() =
        CgsGameEngine.INSTANCE.winningTeam

    override fun getExtraData(): Map<String, String>
    {
        TODO("Not yet implemented")
    }
}
