package gg.scala.cgs.lobby.modular

import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.tangerine.module.HubModule
import gg.scala.tangerine.module.impl.HubModuleItemAdapter
import net.evilblock.cubed.scoreboard.ScoreboardAdapter

/**
 * The tangerine [HubModule]
 * implementation.
 *
 * @author GrowlyX
 * @since 12/4/2021
 */
object CgsLobbyModule : HubModule
{
    override val itemAdapter: HubModuleItemAdapter
        get() = CgsLobbyModuleItems

    override val scoreboardAdapter: ScoreboardAdapter
        get() = CgsGameLobby.INSTANCE.getScoreboardAdapter()
}
