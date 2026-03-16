package gg.ginco.markers.menu

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
import gg.ginco.markers.ext.toPrettyString
import gg.ginco.markers.resource.LocationMarker

/** Lists all markers in the world, sorted by distance. */
class MarkerEditorMenu(player: PlayerRef, marker: LocationMarker) :
    ReactiveUiPage(player, CustomPageLifetime.CanDismissOrCloseThroughInteraction) {

    val currentMarker = marker.clone()

    override fun build(
        ref: Ref<EntityStore>,
        commands: UICommandBuilder,
        events: UIEventBuilder,
        store: Store<EntityStore>
    ) {
        commands.append("Markers/MarkerEditor.ui")

        currentMarker.markerId?.let { commands.set("#MarkerId.Value", it) }
        currentMarker.markerType?.let { commands.set("#MarkerType.Value", it) }

        currentMarker.location?.let {
            commands.set("#MarkerLocation.Value", it.position.toPrettyString())
        }

        bindEvent(
            CustomUIEventBindingType.Activating, "#Save",
            events,
            EventBinding.action("save").onEvent {
                val player = it.store.getComponent(it.ref, Player.getComponentType()) ?: return@onEvent
                val playerRef = it.store.getComponent(it.ref, PlayerRef.getComponentType()) ?: return@onEvent

                playerRef.sendMessage(Message.raw("HEY -> ${it.getParameter<String>("MarkerId")}"))
                playerRef.sendMessage(Message.raw("HEY -> ${it.getParameter<String>("#MarkerId")}"))
                playerRef.sendMessage(Message.raw("HEY -> ${it.getParameter<String>("#MarkerId.Value")}"))

                commands.commands.forEach { command ->
                    playerRef.sendMessage(Message.raw(command.text + " " + command.data + " " + command.selector))
                }
            })
    }

    override fun getRootContentSelector(): String = "#Content"
}