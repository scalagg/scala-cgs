package gg.scala.cgs.common

import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.cgs.common.information.arena.CgsGameArena
import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import gg.scala.cgs.common.renderer.CgsGameScoreboardRenderer
import gg.scala.cgs.common.spectator.CgsSpectatorHandler
import gg.scala.cgs.common.teams.CgsGameTeamEngine
import gg.scala.lemon.handler.ChatHandler
import me.lucko.helper.plugin.ExtendedJavaPlugin
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializer
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.spigotmc.AsyncCatcher
import java.util.*
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
abstract class CgsGameEngine<S : GameSpecificStatistics>(
    val plugin: ExtendedJavaPlugin,
    val gameInfo: CgsGameGeneralInfo,
    val gameMode: CgsGameMode
)
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<CgsGameEngine<*>>()
    }

    lateinit var statisticType: KClass<*>
    lateinit var gameArena: CgsGameArena

    val uniqueId = UUID.randomUUID()
    var gameState by SmartCgsState()

    inline fun <reified T : GameSpecificStatistics> setStatisticType()
    {
        statisticType = T::class
    }

    fun initialResourceLoad()
    {
        INSTANCE = this

        CgsPlayerHandler.initialLoad()
        CgsGameTeamEngine.initialLoad(this)

        Serializers.useGsonBuilderThenRebuild {
            it.registerTypeAdapter(GameSpecificStatistics::class.java, AbstractTypeSerializer<GameSpecificStatistics>())
        }

        ChatHandler.registerChannelOverride(
            CgsOverridingSpectatorChannel
        )

        Bukkit.getServer().maxPlayers = gameMode.getMaxTeams() * gameMode.getTeamSize()
    }

    @Suppress("UNCHECKED_CAST")
    fun getStatistics(cgsGamePlayer: CgsGamePlayer): S
    {
        return cgsGamePlayer.gameSpecificStatistics[statisticType.simpleName]!! as S
    }

    fun sendMessage(message: String)
    {
        for (team in CgsGameTeamEngine.teams.values)
        {
            team.participants.forEach { uuid ->
                val bukkitPlayer = Bukkit.getPlayer(uuid)
                    ?: return@forEach

                bukkitPlayer.sendMessage(message)
            }
        }

        for (spectator in CgsSpectatorHandler.spectators)
        {
            val bukkitPlayer = Bukkit.getPlayer(spectator)
                ?: continue

            bukkitPlayer.sendMessage(message)
        }
    }

    fun sendMessage(fancyMessage: FancyMessage)
    {
        for (team in CgsGameTeamEngine.teams.values)
        {
            team.participants.forEach { uuid ->
                val bukkitPlayer = Bukkit.getPlayer(uuid)
                    ?: return@forEach

                fancyMessage.sendToPlayer(bukkitPlayer)
            }
        }

        for (spectator in CgsSpectatorHandler.spectators)
        {
            val bukkitPlayer = Bukkit.getPlayer(spectator)
                ?: continue

            fancyMessage.sendToPlayer(bukkitPlayer)
        }
    }

    /**
     * The method which is called in addition to the [onTick]
     * method within the [StateRunnable] instance.
     */
    abstract fun onTick(state: CgsGameState, tickOfState: Int): Boolean

    protected fun onStateChange(oldState: CgsGameState)
    {
        var event: CgsGameEvent? = null

        if (compare(oldState, CgsGameState.WAITING, CgsGameState.STARTING))
        {
            event = CgsGamePreStartEvent()
        } else if (compare(oldState, CgsGameState.STARTING, CgsGameState.WAITING))
        {
            event = CgsGamePreStartCancelEvent()
        } else if (compare(oldState, CgsGameState.STARTING, CgsGameState.STARTED))
        {
            event = CgsGameStartEvent()
        } else if (compare(oldState, CgsGameState.STARTED, CgsGameState.ENDED))
        {
            event = CgsGameEndEvent()
        }

        Tasks.sync {
            event?.callNow()
        }
    }

    private fun compare(oldState: CgsGameState, expected: CgsGameState, newState: CgsGameState): Boolean
    {
        return oldState == expected && newState == gameState
    }

    abstract fun getScoreboardRenderer(): CgsGameScoreboardRenderer

    // Display a Congratulations title to the winner/winning team

    // Give a random ranged coin amount to both
    // participants IF AWARDS ARE ENABLED

    // MAKE SURE TO CHECK FOR AWARD BECAUSE
    // WE MAY HAVE PRIVATE MATCHES

    // Start a new StateRunnable which will run for 10 ticks
    // At the last tick, do bukkit.shutdown
    class CgsGameEndEvent : CgsGameEvent()

    // Send a nice "GAME STARTED" title

    // Track starting participants in a map
    // Track starting TIME

    // Set PREVIOUSLY PLAYED GAME id for all PARTICIPANTS
    // We will add rejoin support here

    // REFRESH ALL VISIBILITY, NAMETAG, Etc

    // Possibly handle cage logic or unfreeze
    // logic in subclasses of BedWars and SkyWars?
    class CgsGameStartEvent : CgsGameEvent()

    // start runnable for starting ticks...
    // The game will start in 10 seconds.

    // USE StateRunnable FOR THIS

    // at important numbers use adventure from KyoriBridge in Cookie to display titles
    // to player with a base title of Component.text(CC.SOMETHING + int) and subtitle of Component.empty()

    // use startingTicks from CgsGameInfo
    // when it is at 0, set state to STARTED
    class CgsGamePreStartEvent : CgsGameEvent()

    // Send a special message indicating that this user/console has
    // force started the game to the STARTING state.

    // We will not be going directly to STARTED as we need STARTING checks to be called.
    class CgsGameForceStartEvent(
        val starter: CommandSender
    ) : CgsGameEvent()

    // teleport everyone back to the
    // LOBBY the CgsGameArena spawn location

    // Make sure to clear everyones titles through
    // KyoriBridge, and teleport possible spectators back
    class CgsGamePreStartCancelEvent : CgsGameEvent()

    // Possibly check if the game is already running
    // If it is, and the player's not eliminated/last
    // game is this current instance, call the reconnect event

    // If the game allows spectating, and the game is
    // running + player is elim'd, call spectator handler add

    // IF GAME IS STARTING/WAITING:
    // Allocate the player to a team, if they have a PARTY and there is a
    // team with the player amount of that party, allocate all players to
    // that team, or else select random teams for them.

    // Teleport the player through PaperLib.teleportAsync()
    // to the LOBBY arena if the game is in WAITING state

    // Send the `Player has joined! (1/12)` message
    class CgsGameParticipantConnectEvent(
        val participant: Player
    ) : CgsGameEvent()

    // If the player's team is considered "disqualified",
    // continue to handle spectator checks, or else re-add the player

    // IF IT HAS BEEN 5+ minutes since disconnection, set to spectator
    class CgsGameParticipantReconnectEvent(
        val participant: Player
    ) : CgsGameEvent()

    // (PLAYING) Save the player's disconnection epoch timestamp to redis

    // Make sure to CLEAR the player on logout, especially spectator metadata.
    // IF THERE is only 1 participant left other than this participant, call
    // end event with the last participant being the WINNER.

    // IF THERE are multiple players who are on the SAME TEAM, call their team as the winner.
    class CgsGameParticipantDisconnectEvent(
        val participant: Player
    ) : CgsGameEvent()

    // (WAITING) Check if this game allows players to enter spectator mode
    // (PLAYING) If the player is a contestant in this game, do not allow them to continue spectator checks
    class CgsGameSpectatorPreAddEvent(
        val spectator: Player
    ) : CgsGameEvent()

    // Set spectators to be invisible to
    // contestants, but a "ghost" to other spectators.

    // Send the player a "You've been made a spectator: <reason>"
    // Apply spectator item set to player and reload nametag, tablist, and visibility
    class CgsGameSpectatorAddEvent(
        val spectator: Player
    ) : CgsGameEvent()

    class CgsGameSpectatorRemoveEvent(
        val spectator: Player
    ) : CgsGameEvent()

    // Set a custom death message within implementations.
    // IF THERE is only 1 participant left other than this participant, call
    // end event with the last participant being the WINNER.

    // IF THERE are multiple players who are on the SAME TEAM, call their team as the winner.
    class CgsGameParticipantDeathEvent(
        val participant: Player
    ) : CgsGameEvent()

    abstract class CgsGameEvent : Event(), Cancellable
    {
        companion object
        {
            @JvmStatic
            val HANDLERS = HandlerList()
        }

        private var internalCancelled = false

        override fun getHandlers() = HANDLERS
        override fun isCancelled() = internalCancelled

        override fun setCancelled(new: Boolean)
        {
            internalCancelled = new
        }

        fun callNow(): Boolean
        {
            if (!Bukkit.isPrimaryThread())
            {
                AsyncCatcher.catchOp("Cannot call event asynchronously")
                return true
            }

            Bukkit.getPluginManager().callEvent(this)

            return internalCancelled
        }
    }

    private inner class SmartCgsState : ReadWriteProperty<Any, CgsGameState>
    {
        private var value = CgsGameState.WAITING

        override fun getValue(thisRef: Any, property: KProperty<*>): CgsGameState
        {
            return value
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: CgsGameState)
        {
            val oldValue = this.value
            this.value = value

            onStateChange(oldValue)
        }
    }
}
