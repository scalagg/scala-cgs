package gg.scala.cgs.common.voting

import com.cryptomorin.xseries.XMaterial

/**
 * @author GrowlyX
 * @since 7/16/2022
 */
data class VotingMapEntry(
    val id: String,
    val item: XMaterial,
    val description: String,
    val displayName: String
)
