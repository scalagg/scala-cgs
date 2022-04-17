package gg.scala.parties.model

import net.evilblock.cubed.util.CC
import java.util.*

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
enum class PartyStatus(
    val formatted: String
)
{
    PUBLIC("${CC.GREEN}Public"),
    PROTECTED("${CC.GOLD}Password Protected"),
    PRIVATE("${CC.RED}Private");

    val capitalized = name
        .lowercase()
        .capitalize()
}
