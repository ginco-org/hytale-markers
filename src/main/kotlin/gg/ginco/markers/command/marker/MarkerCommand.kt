package gg.ginco.markers.command.marker

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection
import com.hypixel.hytale.server.core.permissions.HytalePermissions
import gg.ginco.markers.LocationMarkerSystemsRegistrar

/**
 * Contains all marker managements commands:
 * - Marker create command
 * - Marker list command
 * - Marker reveal command
 * - Marker data commands (add and remove)
 */
class MarkerCommand(markerRegistrar: LocationMarkerSystemsRegistrar) :
    AbstractCommandCollection("markers", "Provides access to view, modify and create markers.") {

    init {
        addSubCommand(MarkerCreateCommand(markerRegistrar))
        addSubCommand(MarkerListCommand(markerRegistrar))
        addSubCommand(MarkerRevealCommand(markerRegistrar))

        // Collection of "/marker data [add/remove]" commands.
        addSubCommand(MarkerDataCommand(markerRegistrar))

        // Also allow players to use /marker and /m.
        addAliases("marker", "m")
        requirePermission(HytalePermissions.fromCommand("marker"));
    }

}