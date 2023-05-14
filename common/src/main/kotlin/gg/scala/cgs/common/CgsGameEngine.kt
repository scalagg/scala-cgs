package gg.scala.cgs.common

import gg.scala.cgs.common.combat.CombatLogService
import gg.scala.cgs.common.deathmatch.DeathmatchConfiguration
import gg.scala.cgs.common.deathmatch.DeathmatchService
import gg.scala.cgs.common.environment.EditableFieldService
import gg.scala.cgs.common.environment.editor.EnvironmentEditorService
import gg.scala.cgs.common.frontend.CgsFrontendService
import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.cgs.common.information.arena.CgsGameArena
import gg.scala.cgs.common.information.arena.CgsGameArenaHandler
import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.cgs.common.instance.handler.CgsInstanceService
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.nametag.CgsGameNametagAdapter
import gg.scala.cgs.common.player.scoreboard.CgsGameScoreboardRenderer
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import gg.scala.cgs.common.player.visibility.CgsGameVisibilityAdapter
import gg.scala.cgs.common.rewards.CoinRewardPlatform
import gg.scala.cgs.common.rewards.impl.DefaultCoinRewardPlatform
import gg.scala.cgs.common.rewards.impl.GrapeCoinRewardPlatform
import gg.scala.cgs.common.runnable.StateRunnableService
import gg.scala.cgs.common.runnable.state.EndedStateRunnable
import gg.scala.cgs.common.snapshot.CgsGameSnapshot
import gg.scala.cgs.common.snapshot.CgsGameSnapshotEngine
import gg.scala.cgs.common.snapshot.inventory.CgsInventorySnapshot
import gg.scala.cgs.common.sponsor.SponsorConfiguration
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.common.states.CgsGameStateService
import gg.scala.cgs.common.states.machine.CgsGameStateMachine
import gg.scala.cgs.common.states.machine.StateMachineAutoRegister
import gg.scala.cgs.common.statistics.CgsStatisticProvider
import gg.scala.cgs.common.teams.CgsGameTeam
import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.cgs.common.voting.CgsVotingMapService
import gg.scala.cgs.common.voting.VotingMapConfiguration
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.custom.CustomAnnotationProcessors
import gg.scala.grape.GrapeSpigotPlugin
import me.lucko.helper.Events
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
abstract class CgsGameEngine<S : GameSpecificStatistics>(
    val plugin: ExtendedScalaPlugin,
    var gameInfo: CgsGameGeneralInfo,
    var gameMode: CgsGameMode,
    override val statisticType: KClass<S>
) : CgsStatisticProvider<S>
{
    companion object
    {
        @JvmStatic
        lateinit var INSTANCE: CgsGameEngine<*>
    }

    var gameArena: CgsGameArena? = null
    val uniqueId: UUID
        get() = uniqueIdGlobal

    var gameState by SmartCgsState()

    var gameStart = 0L

    var originalRemaining =
        mutableListOf<UUID>()

    val audience = BukkitAudiences.create(plugin)

    var platform: CoinRewardPlatform = DefaultCoinRewardPlatform

    fun initialLoad()
    {
        INSTANCE = this

        kotlin.runCatching {
            gameArena = CgsGameArenaHandler.arena
        }

        if (Bukkit.getPluginManager().getPlugin("Grape") != null)
        {
            platform = GrapeCoinRewardPlatform
        }
    }

    fun initialResourceLoad()
    {
        CustomAnnotationProcessors
            .process<StateMachineAutoRegister> {
                if (it is CgsGameStateMachine)
                {
                    CgsGameStateService.register(it)
                }
            }

        Serializers.create {
            registerTypeAdapter(
                GameSpecificStatistics::class.java,
                AbstractTypeSerializer<GameSpecificStatistics>()
            )
        }

        plugin.flavor {
            bind<CgsGameEngine<S>>() to this@CgsGameEngine
            bind<CgsStatisticProvider<S>>() to this@CgsGameEngine

            inject(StateRunnableService)
            inject(CgsPlayerHandler)
            inject(CgsGameTeamService)
            inject(CgsFrontendService)
            inject(EnvironmentEditorService)
            inject(EditableFieldService)
            inject(CgsGameSnapshotEngine)
            inject(CgsGameArenaHandler)
            inject(DeathmatchService)

            if (gameInfo.preStartVoting)
            {
                inject(CgsVotingMapService)
            }

            if (gameInfo.configureCombatLog)
            {
                inject(CombatLogService)
            }
        }

        if (
            !gameInfo.usesCustomArenaWorld &&
            getVotingConfig() == null
        )
        {
            CgsGameArenaHandler.configure(gameMode)
        }

        Events.subscribe(AsyncPlayerPreLoginEvent::class.java)
            .handler {
                if (!EndedStateRunnable.ALLOWED_TO_JOIN)
                {
                    it.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                        "${CC.RED}This server is currently whitelisted."
                    )
                }
            }

        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "game-server", true
            )

        Bukkit.getServer().maxPlayers =
            gameMode.getMaxTeams() * gameMode.getTeamSize()
    }

    fun sendMessage(message: List<String>)
    {
        for (team in CgsGameTeamService.teams.values)
        {
            team.participants.forEach { uuid ->
                val bukkitPlayer = Bukkit.getPlayer(uuid)
                    ?: return@forEach

                if (!bukkitPlayer.hasMetadata("spectator"))
                    message.forEach(bukkitPlayer::sendMessage)
            }
        }

        for (spectator in Bukkit.getOnlinePlayers()
            .filter { it.hasMetadata("spectator") }
        )
        {
            message.forEach(spectator::sendMessage)
        }
    }

    fun sendMessage(message: String)
    {
        for (team in CgsGameTeamService.teams.values)
        {
            team.participants.forEach { uuid ->
                val bukkitPlayer = Bukkit.getPlayer(uuid)
                    ?: return@forEach

                if (!bukkitPlayer.hasMetadata("spectator"))
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
        for (team in CgsGameTeamService.teams.values)
        {
            team.participants.forEach { uuid ->
                val bukkitPlayer = Bukkit.getPlayer(uuid)
                    ?: return@forEach

                if (!bukkitPlayer.hasMetadata("spectator"))
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
        for (team in CgsGameTeamService.teams.values)
        {
            team.participants.forEach { uuid ->
                val bukkitPlayer = Bukkit.getPlayer(uuid)
                    ?: return@forEach

                if (!bukkitPlayer.hasMetadata("spectator"))
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

    fun playSound(sound: Sound, pitch: Float)
    {
        for (team in CgsGameTeamService.teams.values)
        {
            team.participants.forEach { uuid ->
                val bukkitPlayer = Bukkit.getPlayer(uuid)
                        ?: return@forEach

                if (!bukkitPlayer.hasMetadata("spectator"))
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
                    spectator.location, sound, 1.0F, pitch
            )
        }
    }

    fun sendMessage(fancyMessage: FancyMessage)
    {
        for (team in CgsGameTeamService.teams.values)
        {
            team.participants.forEach { uuid ->
                val bukkitPlayer = Bukkit.getPlayer(uuid)
                    ?: return@forEach

                if (!bukkitPlayer.hasMetadata("spectator"))
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

    @Suppress("UNCHECKED_CAST")
    override fun getStatistics(cgsGamePlayer: CgsGamePlayer): S
    {
        return CgsPlayerHandler.statistics[cgsGamePlayer.uniqueId]!! as S
    }

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

    open fun onAsyncPreStartResourceInitialization(): CompletableFuture<Boolean>
    {
        return CompletableFuture.supplyAsync { true }
    }

    abstract fun getScoreboardRenderer(): CgsGameScoreboardRenderer

    abstract fun getVisibilityAdapter(): CgsGameVisibilityAdapter
    abstract fun getNametagAdapter(): CgsGameNametagAdapter

    abstract fun getGameSnapshot(): CgsGameSnapshot

    open fun createTeam(id: Int): CgsGameTeam
    {
        return CgsGameTeam(id)
    }

    open fun getVotingConfig(): VotingMapConfiguration?
    {
        return null
    }

    open fun getSponsorConfig(): SponsorConfiguration?
    {
        return null
    }

    open fun getDeathmatchConfig(): DeathmatchConfiguration?
    {
        return null
    }

    class CgsGameEndEvent : CgsGameEvent()

    class CgsGameStartEvent : CgsGameEvent()
    class CgsGamePreStartEvent : CgsGameEvent()

    class CgsGameForceStartEvent(
        val starter: CommandSender
    ) : CgsGameEvent()

    class CgsGamePreStartCancelEvent : CgsGameEvent()

    class CgsGameParticipantConnectEvent(
        val participant: Player,
        val participantPlayer: CgsGamePlayer,
        val reconnectCalled: Boolean
    ) : CgsGameEvent()

    class CgsGameParticipantReconnectEvent(
        val participant: Player, val connectedWithinTimeframe: Boolean
    ) : CgsGameEvent()

    class CgsGameParticipantReinstateEvent(
        val participant: Player,
        val snapshot: CgsInventorySnapshot,
        val connected: Boolean
    ) : CgsGameEvent()

    class CgsGameParticipantDisconnectEvent(
        val participant: Player,
        val participantInVanish: Boolean = false
    ) : CgsGameEvent()

    class CgsGameParticipantDeathEvent(
        val participant: Player,
        val killer: Player?,
        val deathLocation: Location
    ) : CgsGameEvent()

    class CgsGameSpectatorAddEvent(
        val spectator: Player
    ) : CgsGameEvent()

    class CgsGameSpectatorRemoveEvent(
        val spectator: Player
    ) : CgsGameEvent()

    abstract class CgsGameEvent : Event(), Cancellable
    {
        companion object
        {
            @JvmStatic
            val handlerList = HandlerList()
        }

        private var internalCancelled = false

        override fun getHandlers() = handlerList
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
            if (this.value == CgsGameState.STARTED && value == CgsGameState.STARTING)
            {
                throw RuntimeException(
                    "Some dumb bitch tried to set the game to STARTING from STARTED! what the fuck!"
                )
            }

            val oldValue = this.value
            this.value = value

            onStateChange(oldValue)
        }
    }
}
