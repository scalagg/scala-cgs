package gg.scala.cgs.common

import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.information.CgsGameGeneralInfo
import gg.scala.cgs.common.information.arena.CgsGameArena
import gg.scala.cgs.common.information.mode.CgsGameMode
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import me.lucko.helper.plugin.ExtendedJavaPlugin
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializer
import org.bukkit.Bukkit
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

    var gameState by SmartCgsState()

    inline fun <reified T : GameSpecificStatistics> setStatisticType()
    {
        statisticType = T::class
    }

    fun initialResourceLoad()
    {
        INSTANCE = this
        CgsPlayerHandler.initialLoad()

        Serializers.useGsonBuilderThenRebuild {
            it.registerTypeAdapter(GameSpecificStatistics::class.java, AbstractTypeSerializer<GameSpecificStatistics>())
        }

        Bukkit.getServer().maxPlayers = gameMode.getMaxTeams() * gameMode.getTeamSize()
    }

    @Suppress("UNCHECKED_CAST")
    fun getStatistics(cgsGamePlayer: CgsGamePlayer): S
    {
        return cgsGamePlayer.gameSpecificStatistics[statisticType.simpleName]!! as S
    }

    // super class will call these methods
    // and add its own logic too
    open fun onGameEnterStarting()
    {
        // start runnable for starting ticks...
        // The game will start in 10 seconds.

        // USE StateRunnable FOR THIS

        // at important numbers use adventure from KyoriBridge in Cookie to display titles
        // to player with a base title of Component.text(CC.SOMETHING + int) and subtitle of Component.empty()

        // use startingTicks from CgsGameInfo
        // when it is at 0, set state to STARTED
    }

    open fun onGameCancelStarting()
    {
        // teleport everyone back to the
        // LOBBY the CgsGameArena spawn location

        // Make sure to clear everyones titles through
        // KyoriBridge, and teleport possible spectators back
    }

    open fun onGameStart()
    {
        // Send a nice "GAME STARTED" title

        // Track starting participants in a map
        // Track starting TIME

        // Set PREVIOUSLY PLAYED GAME id for all PARTICIPANTS
        // We will add rejoin support here

        // REFRESH ALL VISIBILITY, NAMETAG, Etc

        // Possibly handle cage logic or unfreeze
        // logic in subclasses of BedWars and SkyWars?
    }

    open fun onGameEnding()
    {
        // Display a Congratulations title to the winner/winning team

        // Give a random ranged coin amount to both
        // participants IF AWARDS ARE ENABLED

        // MAKE SURE TO CHECK FOR AWARD BECAUSE
        // WE MAY HAVE PRIVATE MATCHES

        // Start a new StateRunnable which will run for 10 ticks
        // At the last tick, do bukkit.shutdown
    }

    // this method hot for real
    protected fun onStateChange(oldState: CgsGameState)
    {
        if (compare(oldState, CgsGameState.WAITING, CgsGameState.STARTING))
        {
            onGameEnterStarting()
        } else if (compare(oldState, CgsGameState.STARTING, CgsGameState.WAITING))
        {
            onGameCancelStarting()
        } else if (compare(oldState, CgsGameState.STARTING, CgsGameState.STARTED))
        {
            onGameStart()
        } else if (compare(oldState, CgsGameState.STARTED, CgsGameState.ENDED))
        {
            onGameEnding()
        }

        // TODO: 11/30/2021 update instance to redis after this
    }

    private fun compare(oldState: CgsGameState, expected: CgsGameState, newState: CgsGameState): Boolean
    {
        return oldState == expected && newState == gameState
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
