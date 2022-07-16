package gg.scala.cgs.common.voting

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import java.util.UUID

/**
 * @author GrowlyX
 * @since 7/16/2022
 */
@Service
@IgnoreAutoScan
object CgsVotingMapService : Runnable
{
    @Inject
    lateinit var engine: CgsGameEngine<*>

    lateinit var configuration: VotingMapConfiguration

    var votingEnabled = false
    val selections = mutableMapOf<String, Map<UUID, Int>>()

    @Configure
    fun configure()
    {
        val config = engine.getVotingConfig()
            ?: throw IllegalArgumentException(
                "Voting is enabled yet no voting config was supplied in game implementation."
            )

        this.configuration = config

        for (entry in config.entries())
        {
            this.selections[entry.id] = mutableMapOf()
        }

        Events.subscribe(PlayerJoinEvent::class.java)
            .filter {
                engine.gameState == CgsGameState.WAITING
            }
            .handler {
                if (Bukkit.getOnlinePlayers().size < configuration.minimumPlayersForVotingStart)
                {
                    val required = configuration.minimumPlayersForVotingStart - Bukkit.getOnlinePlayers().size

                    it.player.sendMessage("${CC.PRI}$required${CC.SEC} more player${
                        if (required == 1) "" else "s"
                    } required for map voting to open!")
                } else if (
                    Bukkit.getOnlinePlayers().size >= configuration.minimumPlayersForVotingStart &&
                    !votingEnabled
                )
                {
                    this.votingEnabled = true
                    // TODO: map voting start logic
                } else if (
                    Bukkit.getOnlinePlayers().size < configuration.minimumPlayersForVotingStart &&
                    votingEnabled
                )
                {
                    this.votingEnabled = false
                    // TODO: map voting termination logic
                }
            }
    }

    override fun run()
    {

    }
}
