package gg.scala.cgs.common.voting

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
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
    }

    override fun run()
    {
        TODO("Not yet implemented")
    }
}
