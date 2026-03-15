package gg.ginco.markers.component

import com.hypixel.hytale.component.Component
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/** All markers will be visible to any player with this marker. */
class VisibleMarkersComponent : Component<EntityStore> {

    override fun clone(): Component<EntityStore> = VisibleMarkersComponent()
}