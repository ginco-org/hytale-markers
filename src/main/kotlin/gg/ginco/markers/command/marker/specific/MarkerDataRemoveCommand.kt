package gg.ginco.markers.command.marker.specific

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import gg.ginco.markers.LocationMarkerSystemsRegistrar
import gg.ginco.markers.resource.LocationMarker

/** Removes the data held in the provided key on this marker. */
class MarkerDataRemoveCommand(markerRegistrar: LocationMarkerSystemsRegistrar) : AbstractMarkerCommand("remove", "Removes the data assigned to a key on a marker.", markerRegistrar) {

    private val dataKey = withRequiredArg("dataKey", "The key of the data you want to remove.", ArgTypes.STRING)

    override fun executeMarkerAction(
        context: CommandContext,
        store: Store<EntityStore>,
        ref: Ref<EntityStore>,
        playerRef: PlayerRef,
        world: World,
        marker: LocationMarker
    ) {
        val dataKey = context.get(dataKey)

        val currentData = marker.data ?: mapOf()
        marker.data = currentData.minus(dataKey)

        playerRef.sendMessage(Message.translation("ginco.general.marker.data_removed"))
    }

}