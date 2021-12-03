package gg.scala.cgs.common

import gg.scala.cgs.common.channel.CgsOverridingSpectatorChannel
import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.cgs.common.information.arena.CgsGameArena
import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.cgs.common.nametag.CgsGameNametag
import gg.scala.cgs.common.nametag.CgsGameNametagAdapter
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import gg.scala.cgs.common.renderer.CgsGameScoreboardRenderer
import gg.scala.cgs.common.teams.CgsGameTeam
import gg.scala.cgs.common.teams.CgsGameTeamEngine
import gg.scala.cgs.common.visibility.CgsGameVisibility
import gg.scala.cgs.common.visibility.CgsGameVisibilityAdapter
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.ChatHandler
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializer
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.visibility.VisibilityHandler
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
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
    val plugin: ExtendedScalaPlugin,
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

    var gameStart = 0L
    var originalRemaining = 0

    val audience = BukkitAudiences.create(plugin)

    inline fun <reified T : GameSpecificStatistics> withStatistics()
    {
        statisticType = T::class
    }

    fun initialLoad()
    {
        INSTANCE = this
    }

    fun initialResourceLoad()
    {
        plugin.invokeTrackedTask("initial loading CGS resources") {
            CgsPlayerHandler.initialLoad()
            CgsGameTeamEngine.initialLoad(this)

            Serializers.useGsonBuilderThenRebuild {
                it.registerTypeAdapter(
                    GameSpecificStatistics::class.java,
                    AbstractTypeSerializer<GameSpecificStatistics>()
                )
            }

            ChatHandler.registerChannelOverride(
                CgsOverridingSpectatorChannel
            )

            VisibilityHandler.registerAdapter(
                "cgs", CgsGameVisibility
            )

            NametagHandler.registerProvider(
                CgsGameNametag
            )

            Lemon.instance.localInstance
                .metaData["game-server"] = "true"

            Bukkit.getServer().maxPlayers =
                gameMode.getMaxTeams() * gameMode.getTeamSize()
        }
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

        for (spectator in Bukkit.getOnlinePlayers()
            .filter { it.hasMetadata("spectator") }
        )
        {
            spectator.sendMessage(message)
        }
    }

    fun sendTitle(title: Title)
    {
        for (team in CgsGameTeamEngine.teams.values)
        {
            team.participants.forEach { uuid ->
                val bukkitPlayer = Bukkit.getPlayer(uuid)
                    ?: return@forEach

                bukkitPlayer adventure {
                    it.showTitle(title)
                }
            }
        }

        for (spectator in Bukkit.getOnlinePlayers()
            .filter { it.hasMetadata("spectator") }
        )
        {
            spectator adventure {
                it.showTitle(title)
            }
        }
    }

    fun playSound(sound: Sound)
    {
        for (team in CgsGameTeamEngine.teams.values)
        {
            team.participants.forEach { uuid ->
                val bukkitPlayer = Bukkit.getPlayer(uuid)
                    ?: return@forEach

                bukkitPlayer.playSound(
                    bukkitPlayer.location, sound, 1F, 1F
                )
            }
        }

        for (spectator in Bukkit.getOnlinePlayers()
            .filter { it.hasMetadata("spectator") }
        )
        {
            spectator.playSound(
                spectator.location, sound, 1F, 1F
            )
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

        for (spectator in Bukkit.getOnlinePlayers()
            .filter { it.hasMetadata("spectator") }
        )
        {
            fancyMessage.sendToPlayer(spectator)
        }
    }

    /**
     * The method which is called in addition to the [onTick]
     * method within the [StateRunnable] instance.
     */
    abstract fun onTick(state: CgsGameState, tickOfState: Int): Boolean

    lateinit var winningTeam: CgsGameTeam

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
            event = CgsGameEndEvent(winningTeam)
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

    abstract fun getVisibilityAdapter(): CgsGameVisibilityAdapter
    abstract fun getNametagAdapter(): CgsGameNametagAdapter

    abstract fun getExtraWinInformation(): List<String>

    // Display a Congratulations title to the winner/winning team

    // Give a random ranged coin amount to both
    // participants IF AWARDS ARE ENABLED

    // MAKE SURE TO CHECK FOR AWARD BECAUSE
    // WE MAY HAVE PRIVATE MATCHES

    // Start a new StateRunnable which will run for 10 ticks
    // At the last tick, do bukkit.shutdown
    class CgsGameEndEvent(
        val cgsGameTeam: CgsGameTeam
    ) : CgsGameEvent()

    class CgsGameStartEvent : CgsGameEvent()
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

    class CgsGameParticipantConnectEvent(
        val participant: Player, val reconnectCalled: Boolean
    ) : CgsGameEvent()

    class CgsGameParticipantReconnectEvent(
        val participant: Player, val connectedWithinTimeframe: Boolean
    ) : CgsGameEvent()

    class CgsGameParticipantReinstateEvent(
        val participant: Player, val gameTeam: CgsGameTeam
    ) : CgsGameEvent()

    class CgsGameParticipantDisconnectEvent(
        val participant: Player
    ) : CgsGameEvent()

    class CgsGameSpectatorAddEvent(
        val spectator: Player
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
                Tasks.sync {
                    Bukkit.getPluginManager().callEvent(this)
                }
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
