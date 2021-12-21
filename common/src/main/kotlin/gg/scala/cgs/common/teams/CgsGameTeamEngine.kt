package gg.scala.cgs.common.teams

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.parties.service.PartyService
import org.bukkit.Bukkit
import org.bukkit.entity.Horse
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 12/1/2021
 */
object CgsGameTeamEngine
{
    private val engine by lazy {
        CgsGameEngine.INSTANCE
    }

    val teams = ConcurrentHashMap<Int, CgsGameTeam>()

    fun initialLoad()
    {
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

    fun allocatePlayersToAvailableTeam(
        player: CgsGamePlayer, forceRandom: Boolean = false
    ): Boolean
    {
        val availableTeams = teams.values.filter {
            it.participants.size < engine.gameMode.getTeamSize()
        }.toList()

        if (availableTeams.isNotEmpty())
        {
            val playerParty = PartyService
                .findPartyByUniqueId(player.uniqueId)

            if (playerParty != null && !forceRandom)
            {
                val partyLeader = Bukkit
                    .getPlayer(playerParty.leader.uniqueId)

                if (partyLeader != null)
                {
                    val teamOfLeader = getTeamOf(partyLeader)

                    if (teamOfLeader != null && availableTeams.contains(teamOfLeader))
                    {
                        teamOfLeader.participants.add(player.uniqueId)
                    }
                }
            } else
            {
                val randomTeam = teams[
                        availableTeams.random().id
                ]!!

                randomTeam.participants.add(player.uniqueId)
                return true
            }
        }
        // well


        return false
    }
}
