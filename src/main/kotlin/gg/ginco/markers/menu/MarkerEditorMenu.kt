package gg.ginco.markers.menu

import com.hypixel.hytale.codec.codecs.simple.StringCodec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import dev.jonrapp.hytaleReactiveUi.events.EventBinding
import dev.jonrapp.hytaleReactiveUi.pages.ReactiveUiPage
import gg.ginco.markers.LocationMarkerSystemsRegistrar
import gg.ginco.markers.ext.toPrettyString
import gg.ginco.markers.resource.LocationMarker

/** Lists all markers in the world, sorted by distance. */
class MarkerEditorMenu(
    player: PlayerRef,
    private val marker: LocationMarker,
    private val markerRegistrar: LocationMarkerSystemsRegistrar,
) :
    ReactiveUiPage(player, CustomPageLifetime.CanDismissOrCloseThroughInteraction) {

    val markerClone = marker.clone()

    override fun build(
        ref: Ref<EntityStore>,
        commands: UICommandBuilder,
        events: UIEventBuilder,
        store: Store<EntityStore>
    ) {
        commands.append("Markers/MarkerEditor.ui")

        markerClone.markerId?.let { commands.set("#MarkerId.Value", it) }
        markerClone.markerType?.let { commands.set("#MarkerType.Value", it) }

        markerClone.location?.let {
            commands.set("#MarkerLocation.Value", it.position.toPrettyString())
        }

        bindEvent(
            CustomUIEventBindingType.Activating, "#Cancel",
            events,
            EventBinding.action("back")
                .onEvent {
                    val player = it.store.getComponent(it.ref, Player.getComponentType()) ?: return@onEvent
                    player.pageManager.openCustomPage(ref, store, MarkerListMenu(playerRef, markerRegistrar))
                })

        bindEvent(
            CustomUIEventBindingType.Activating, "#Save",
            events,
            EventBinding.action("save")
                .withEventData("@MarkerId", StringCodec.STRING, "#MarkerId.Value")
                .withEventData("@MarkerType", StringCodec.STRING, "#MarkerType.Value")
                .onEvent {
                    val player = it.store.getComponent(it.ref, Player.getComponentType()) ?: return@onEvent

                    val world = player.world

                    if (world == null) {
                        player.sendMessage(Message.translation("ginco.general.marker.invalid_world"))
                        return@onEvent
                    }

                    val inputMarkerId = it.getParameter<String>("@MarkerId")
                    val inputMarkerType = it.getParameter<String>("@MarkerType")

                    markerClone.markerId = inputMarkerId
                    markerClone.markerType = inputMarkerType

                    // If the id has been edited, remove the original marker from the list and add the copy
                    // with the new id.
                    marker.markerId?.let { id ->
                        if (inputMarkerId != id) world.chunkStore.store.getResource(markerRegistrar.markerResourceType).removeMarker(id)
                    }

                    world.chunkStore.store.getResource(markerRegistrar.markerResourceType).addMarker(markerClone)
                    player.sendMessage(Message.translation("ginco.general.marker.saved"))

                    player.pageManager.openCustomPage(ref, store, MarkerListMenu(playerRef, markerRegistrar))
                })
    }

    override fun getRootContentSelector(): String = "#Content"
}