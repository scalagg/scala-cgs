package gg.scala.cgs.lobby.updater

import gg.scala.cgs.common.instance.CgsServerInstance
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.common.instance.handler.CgsInstanceService
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 12/4/2021
 */
object CgsGameInfoUpdater : Runnable
{
    var lobbies = mutableSetOf<CgsServerInstance>()

    var gameServers = mutableSetOf<CgsServerInstance>()
    val gameModeCounts = ConcurrentHashMap<String, Int>()

    var lobbyTotalCount = 0
    var playingTotalCount = 0

    fun findAvailableServer(gameMode: String, gameType: String): CgsServerInstance?
    {
        return gameServers
            .asSequence()
            .filter { it.gameServerInfo != null }
            .filter { it.gameServerInfo!!.gameType == gameType }
            .filter { it.gameServerInfo!!.gameMode == gameMode }
            .filter { it.gameServerInfo!!.state == CgsGameState.WAITING || it.gameServerInfo!!.state == CgsGameState.STARTING }
            .firstOrNull()
    }

    fun configure()
    {
        val executor = Executors
            .newSingleThreadScheduledExecutor()

        executor.scheduleAtFixedRate(
            this, 0L,
            250L, TimeUnit.MILLISECONDS
        )
    }

    override fun run()
    {
        val engine = CgsGameLobby.INSTANCE

        val instances = CgsInstanceService.service
            .loadAll(DataStoreStorageType.REDIS)
            .join()

        lobbies = instances.values
            .filter { it.type == CgsServerType.LOBBY }
            .toMutableSet()

        gameServers = instances.values
            .filter { it.type == CgsServerType.GAME_SERVER }
            .toMutableSet()

        val playing = instances.values
            .filter { it.type == CgsServerType.GAME_SERVER }
            .filter { it.gameServerInfo!!.gameType == CgsGameLobby.INSTANCE.getGameInfo().fancyNameRender }

        for (gameMode in engine.getGameInfo().gameModes)
        {
            gameModeCounts[gameMode.getId()] = playing
                .filter { it.gameServerInfo!!.gameMode == gameMode.getId() }
                .sumOf { it.gameServerInfo!!.participants.size }
        }

        playingTotalCount = playing
            .sumOf { it.gameServerInfo!!.participants.size }

        lobbyTotalCount = instances.values
            .filter { it.type == CgsServerType.LOBBY }
            .sumOf { it.online }
    }
}
