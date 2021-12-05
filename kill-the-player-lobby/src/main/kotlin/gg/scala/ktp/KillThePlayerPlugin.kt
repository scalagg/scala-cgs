package gg.scala.ktp

import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.ktp.game.KillThePlayerCgsStatistics

/**
 * @author GrowlyX
 * @since 12/3/2021
 */
class KillThePlayerPlugin : ExtendedScalaPlugin()
{
    override fun enable()
    {
        val engine = KillThePlayerLobby
        engine.statisticType = KillThePlayerCgsStatistics::class

        CgsGameLobby.INSTANCE = engine
    }
}
