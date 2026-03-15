package gg.ginco.markers.command.marker

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import gg.ginco.markers.LocationMarkerSystemsRegistrar
import gg.ginco.markers.command.marker.specific.MarkerDataAddCommand
import gg.ginco.markers.command.marker.specific.MarkerDataRemoveCommand

/** Provides add and remove commands for marker data. */
class MarkerDataCommand(markerRegistrar: LocationMarkerSystemsRegistrar) : AbstractCommandCollection("data", "Provides add and remove commands for marker data.") {
    init {
        addSubCommand(MarkerDataAddCommand(markerRegistrar))
        addSubCommand(MarkerDataRemoveCommand(markerRegistrar))
    }
}