package gg.ginco.markers.command.marker

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import gg.ginco.markers.LocationMarkerSystemsRegistrar
import gg.ginco.markers.resource.LocationMarker

/**
 * Creates a marker in the player's location with the specified id and type.
 * If no marker information is provided a temporary marker is created and an edit
 * menu is opened to fill it with any data.
 */
class MarkerCreateCommand(private val markerRegistrar: LocationMarkerSystemsRegistrar) :
    AbstractPlayerCommand("create", "Creates a marker in your current location.") {

    private val markerType = withOptionalArg("markerType", "The type of marker you want to place.", ArgTypes.STRING)
    private val markerId = withOptionalArg("markerId", "The unique id for this marker.", ArgTypes.STRING)

    override fun execute(
        context: CommandContext,
        store: Store<EntityStore>,
        ref: Ref<EntityStore>,
        playerRef: PlayerRef,
        world: World
    ) {
        val markerType = context.get(markerType)
        val markerId = context.get(markerId)

        // Clone the transform, as it mutable and it does mutate as the player moves.
        val currentTransform = playerRef.transform.clone().apply {
            rotation = playerRef.headRotation
        }

        if (markerType != null && markerId != null) {
            world.chunkStore.store.getResource(markerRegistrar.markerResourceType).addMarker(
                LocationMarker(transform = currentTransform, markerType = markerType, markerId = markerId)
            )

            playerRef.sendMessage(Message.translation("ginco.general.marker.set"))
        } else {
            val marker = LocationMarker(transform = currentTransform, "")

            // TODO: Open a marker editor and add to world once save is clicked.
        }
    }

}