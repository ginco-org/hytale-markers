package gg.ginco.markers.interaction

import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
import com.hypixel.hytale.server.core.universe.PlayerRef
import gg.ginco.markers.LocationMarkerSystemsRegistrar

class MarkerRotateInteraction(private val markerRegistrar: LocationMarkerSystemsRegistrar) :
    SimpleInstantInteraction() {

    override fun firstRun(
        interactionType: InteractionType,
        interactionContext: InteractionContext,
        cooldownHandler: CooldownHandler
    ) {
        val ref = interactionContext.entity
        val store = ref.store

        val playerRef = store.getComponent(ref, PlayerRef.getComponentType()) ?: return
        val player = store.getComponent(ref, Player.getComponentType()) ?: return
        val world = player.world ?: return

        world.execute {
            val selectedMarker =
                store.getComponent(ref, markerRegistrar.selectedMarkerComponentType)?.selectedMarker ?: return@execute

            val markerToMove = world.chunkStore.store.getResource(markerRegistrar.markerResourceType).getMarker(selectedMarker)

            markerToMove?.location?.apply {
                rotation = playerRef.headRotation.clone()
            }
        }
    }

}