package gg.ginco.markers.menu

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import dev.jonrapp.hytaleReactiveUi.pages.ReactiveUiPage
import gg.ginco.markers.LocationMarkerSystemsRegistrar

/** Lists all markers in the world, sorted by distance. */
class MarkerListMenu(
    player: PlayerRef,
    private val markerRegistrar: LocationMarkerSystemsRegistrar
) :
    ReactiveUiPage(player, CustomPageLifetime.CanDismissOrCloseThroughInteraction) {

    override fun build(
        ref: Ref<EntityStore>,
        commands: UICommandBuilder,
        events: UIEventBuilder,
        store: Store<EntityStore>
    ) {
        commands.append("Markers/MarkerList.ui")

        val world = playerRef.worldUuid?.let { Universe.get().getWorld(it) } ?: return

        val markers = world.chunkStore.store.getResource(markerRegistrar.markerResourceType).getMarkers()

        markers.sortedBy { it.location?.position?.distanceSquaredTo(playerRef.transform.position) }
            .forEachIndexed { index, marker ->
                val element = MarkerElement(marker, index, markerRegistrar.markerResourceType, this)
                element.create("#MarkerList", index, commands, events)
            }
    }

    override fun getRootContentSelector(): String = "#Content"
}