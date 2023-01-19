package gg.scala.cgs.common.login

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.redirection.expectation.PlayerRedirectExpectationEvent
import me.lucko.helper.Events
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

/**
 * @author GrowlyX
 * @since 1/18/2023
 */
@Service
object PlayerLoginService
{
    val cached = mutableMapOf<UUID, String>()

    @Configure
    fun configure()
    {
        Events
            .subscribe(PlayerRedirectExpectationEvent::class.java)
            .handler {
                cached[it.uniqueId] = it.from
            }

        Events
            .subscribe(PlayerQuitEvent::class.java)
            .handler {
                cached.remove(it.player.uniqueId)
            }
    }
}
