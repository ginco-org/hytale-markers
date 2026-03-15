package gg.ginco.markers.component

import com.hypixel.hytale.component.Component
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/** Contains the selected marker for this player. */
class SelectedMarkerComponent(var selectedMarker: String? = null) : Component<EntityStore> {

    override fun clone(): Component<EntityStore> = SelectedMarkerComponent(selectedMarker)
}