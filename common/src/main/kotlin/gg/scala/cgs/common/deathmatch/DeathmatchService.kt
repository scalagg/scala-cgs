package gg.scala.cgs.common.deathmatch

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.util.task.DiminutionRunnable
import me.lucko.helper.Schedulers
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.time.TimeUtil

/**
 * @author GrowlyX
 * @since 8/10/2022
 */
@Service
@IgnoreAutoScan
object DeathmatchService
{
    @Inject
    lateinit var engine: CgsGameEngine<*>

    private val seconds = listOf(
        18000, 14400, 10800, 7200, 3600, 2700, 1800,
        900, 600, 300, 240, 180, 120, 60, 50, 40, 30,
        15, 10, 5, 4, 3, 2, 1
    )

    @Configure
    fun configure()
    {
        val deathmatchConfig = engine
            .getDeathmatchConfig()
            ?: return

        Schedulers.async()
            .runRepeating({ task ->
                if (this.engine.gameState == CgsGameState.STARTED)
                {
                    val nonSpectators = Players.all()
                        .filter {
                            !it.hasMetadata("spectator")
                        }

                    if (
                        nonSpectators.size <= deathmatchConfig
                            .getMinimumToStartDeathMatch()
                    )
                    {
                        task.closeAndReportException()
                        this.configureTeleportation(deathmatchConfig)
                    }
                }
            }, 0L, 20L)
    }

    private fun configureTeleportation(config: DeathmatchConfiguration)
    {
        val runnable = object : DiminutionRunnable(
            config.getTeleportationTime().seconds.toInt() + 1
        )
        {
            override fun getSeconds() = this@DeathmatchService.seconds

            override fun onEnd()
            {
                Tasks.sync {
                    config.onTeleporation()

                    broadcast("${CC.GREEN}You have been teleported to the deathmatch!")

                    this@DeathmatchService
                        .configureDeathmatch(config)
                }
            }

            override fun onRun()
            {
                broadcast("${CC.SEC}Deathmatch teleportation will start in ${CC.PRI}${
                    TimeUtil.formatIntoDetailedString(this.seconds)
                }${CC.SEC}.")
            }
        }

        runnable.runTaskTimerAsynchronously(this.engine.plugin, 0L, 20L)
    }

    private fun configureDeathmatch(config: DeathmatchConfiguration)
    {
        val runnable = object : DiminutionRunnable(
            config.getTeleportationTime().seconds.toInt() + 1
        )
        {
            override fun getSeconds() = this@DeathmatchService.seconds

            override fun onEnd()
            {
                Tasks.sync {
                    config.onStart()

                    broadcast("${CC.GREEN}Deathmatch has begun, you may now fight!")
                }
            }

            override fun onRun()
            {
                broadcast("${CC.SEC}Deathmatch will start in ${CC.PRI}${
                    TimeUtil.formatIntoDetailedString(this.seconds)
                }${CC.SEC}.")
            }
        }

        runnable.runTaskTimerAsynchronously(this.engine.plugin, 0L, 20L)
    }
}
