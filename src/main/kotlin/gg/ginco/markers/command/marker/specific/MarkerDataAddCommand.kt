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

/** Stores the provided value on the provided key in the marker's data map. */
class MarkerDataAddCommand(markerRegistrar: LocationMarkerSystemsRegistrar) :
    AbstractMarkerCommand("add", "Adds the provided data value to the provided data key on this marker's data map.", markerRegistrar) {

    private val dataKey = withRequiredArg("dataKey", "The key of the data you want to store.", ArgTypes.STRING)
    private val dataValue = withRequiredArg("dataValue", "The value of the data you want to store.", ArgTypes.STRING)

    override fun executeMarkerAction(
        context: CommandContext,
        store: Store<EntityStore>,
        ref: Ref<EntityStore>,
        playerRef: PlayerRef,
        world: World,
        marker: LocationMarker
    ) {
        val dataKey = context.get(dataKey)
        val dataValue = context.get(dataValue)

        val currentData = marker.data ?: mapOf()
        marker.data = currentData.plus(Pair(dataKey, dataValue))

        playerRef.sendMessage(Message.translation("ginco.general.marker.data_saved"))
    }

}