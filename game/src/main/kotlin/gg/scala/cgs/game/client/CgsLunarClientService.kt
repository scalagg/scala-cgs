package gg.scala.cgs.game.client

import com.lunarclient.bukkitapi.LunarClientAPI
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTeammates
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.cgs.game.listener.CgsGameEventListener
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

    @Configure
    fun configure()
    {
        if (!CgsGameEngine.INSTANCE.gameMode.isSoloGame())
        {
            val executor = Executors
                .newSingleThreadScheduledExecutor()

            executor.scheduleAtFixedRate(
                this, 0L, 10L,
                TimeUnit.MILLISECONDS
            )
        }
    }

    override fun run()
    {
        for (team in CgsGameTeamService.teams)
        {
            val locations = mutableMapOf<UUID, Map<String, Double>>()

            for (participant in team.value.participants)
            {
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
