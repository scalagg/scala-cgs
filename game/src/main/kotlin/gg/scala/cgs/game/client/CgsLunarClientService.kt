package gg.scala.cgs.game.client

import com.lunarclient.bukkitapi.LunarClientAPI
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTeammates
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 2/24/2022
 */
@Service
@IgnoreAutoScan
object CgsLunarClientService : Runnable
{
    private val unreadTeamLeader = UUID.randomUUID()

    private val lunarClientAPI by lazy {
        LunarClientAPI.getInstance()
    }

    private val executor = Executors
        .newSingleThreadScheduledExecutor()

    @Configure
    fun configure()
    {
        if (!CgsGameEngine.INSTANCE.gameMode.isSoloGame())
        {
            executor.scheduleAtFixedRate(
                this, 0L, 50L,
                TimeUnit.MILLISECONDS
            )
        }
    }

    @Close
    fun close()
    {
        executor.shutdownNow()
    }

    override fun run()
    {
        if (CgsGameEngine.INSTANCE.gameState != CgsGameState.STARTED)
        {
            return
        }

        for (team in CgsGameTeamService.teams)
        {
            val locations = mutableMapOf<UUID, Map<String, Double>>()

            for (participant in team.value.participants)
            {
                if (!team.value.alive.contains(participant))
                    continue

                val bukkitPlayer = Bukkit
                    .getPlayer(participant)
                    ?: continue

                val location = bukkitPlayer.location

                locations[participant] = mutableMapOf(
                    "x" to location.x,
                    "y" to location.y,
                    "z" to location.z
                )
            }

            for (participant in team.value.participants)
            {
                val bukkitPlayer = Bukkit
                    .getPlayer(participant)
                    ?: continue

                val packet = LCPacketTeammates(
                    unreadTeamLeader,
                    System.currentTimeMillis(),
                    locations
                )

                if (
                    lunarClientAPI
                        .isRunningLunarClient(bukkitPlayer)
                )
                {
                    lunarClientAPI.sendTeammates(
                        bukkitPlayer, packet
                    )
                }
            }
        }
    }
}
