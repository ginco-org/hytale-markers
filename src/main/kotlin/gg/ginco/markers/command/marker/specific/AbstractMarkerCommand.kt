package gg.ginco.markers.command.marker.specific

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

/** Provides a base for commands that act on a specific marker. */
abstract class AbstractMarkerCommand(
    name: String,
    description: String,
    protected val markerRegistrar: LocationMarkerSystemsRegistrar
) : AbstractPlayerCommand(name, description) {

    private val markerId = withRequiredArg("markerId", "The unique id for the marker.", ArgTypes.STRING)

    /** Easy access to a validated marker type. */
    abstract fun executeMarkerAction(
        context: CommandContext,
        store: Store<EntityStore>,
        ref: Ref<EntityStore>,
        playerRef: PlayerRef,
        world: World,
        marker: LocationMarker
    )

    final override fun execute(
        context: CommandContext,
        store: Store<EntityStore>,
        ref: Ref<EntityStore>,
        playerRef: PlayerRef,
        world: World
    ) {
        val markerId = context.get(markerId)

        val marker = world.chunkStore.store.getResource(markerRegistrar.markerResourceType).getMarker(markerId)

        if (marker == null) {
            playerRef.sendMessage(Message.translation("ginco.general.marker.unknown"))
            return
        }

        executeMarkerAction(context, store, ref, playerRef, world, marker)
    }

}