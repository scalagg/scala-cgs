package gg.scala.cgs.common.teams

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.CgsGamePlayer
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

/**
 * @author GrowlyX
 * @since 12/1/2021
 */
object CgsGameTeamEngine
{
    private lateinit var engine: CgsGameEngine<*>

    val teams = ConcurrentHashMap<Int, CgsGameTeam>()

    fun initialLoad(engine: CgsGameEngine<*>)
    {
        this.engine = engine

        for (id in 1..engine.gameMode.getMaxTeams())
        {
            teams[id] = CgsGameTeam(id)
        }
    }

    fun getTeamOf(player: Player): CgsGameTeam?
    {
        return teams.values.firstOrNull {
            it.participants.contains(player.uniqueId)
        }
    }

    fun removePlayerFromTeam(player: Player)
    {
        teams.values.firstOrNull { team ->
            team.participants.removeIf { it == player.uniqueId }
            team.eliminated.removeIf { it == player.uniqueId }
        }
    }

    fun allocatePlayersToAvailableTeam(players: List<CgsGamePlayer>): Boolean
    {
        if (players.isEmpty())
        {
            throw IndexOutOfBoundsException("The player list is empty")
        }

        if (players.size == 1)
        {
            val availableTeams = teams.values.filter {
                it.participants.size < engine.gameMode.getTeamSize()
            }.toMutableList()

            if (availableTeams.isNotEmpty())
            {
                val randomTeam = teams[
                        availableTeams.random().id
                ]!!
                randomTeam.participants.add(players[0].uniqueId)
            }
        } else
        {
            val availableTeams = teams.values.filter {
                it.participants.size + players.size <= engine.gameMode.getTeamSize()
            }.toMutableList()

            if (availableTeams.isEmpty())
            {
                // fallback to individual allocation
                players.forEach {
                    allocatePlayersToAvailableTeam(listOf(it))
                }
                return true
            }

            // This should never be null
            val randomTeam = teams[
                    availableTeams.random().id
            ]!!

            players.forEach {
                randomTeam.participants.add(it.uniqueId)
            }

            return true
        }

        return false
    }
}
