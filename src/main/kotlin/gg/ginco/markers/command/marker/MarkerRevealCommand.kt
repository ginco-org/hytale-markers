package gg.ginco.markers.command.marker

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import gg.ginco.markers.LocationMarkerSystemsRegistrar
import gg.ginco.markers.component.VisibleMarkersComponent
import gg.ginco.markers.system.MarkerVisibilitySystem

/** Reveals all markers in the chunks loaded by the player. */
class MarkerRevealCommand(private val markerRegistrar: LocationMarkerSystemsRegistrar) : AbstractPlayerCommand("reveal", "Reveals all markers in the world to all the players.") {

    override fun execute(
        context: CommandContext,
        store: Store<EntityStore>,
        ref: Ref<EntityStore>,
        playerRef: PlayerRef,
        world: World
    ) {
        if (store.getComponent(ref, markerRegistrar.visibleMarkersComponentType) != null) {
            store.removeComponent(ref, markerRegistrar.visibleMarkersComponentType)
            playerRef.packetHandler.write(MarkerVisibilitySystem.CLEAR_SHAPES_PACKET)

            playerRef.sendMessage(Message.translation("ginco.general.marker.reveal.toggle.off"))
        } else {
            store.addComponent(ref, markerRegistrar.visibleMarkersComponentType, VisibleMarkersComponent())
            playerRef.sendMessage(Message.translation("ginco.general.marker.reveal.toggle.on"))
        }
    }

}