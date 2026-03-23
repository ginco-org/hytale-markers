package gg.ginco.markers

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.component.ResourceType
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import gg.ginco.markers.command.marker.MarkerCommand
import gg.ginco.markers.component.SelectedMarkerComponent
import gg.ginco.markers.component.VisibleMarkersComponent
import gg.ginco.markers.interaction.MarkerEditInteraction
import gg.ginco.markers.interaction.MarketMoveInteraction
import gg.ginco.markers.interaction.MarkerRotateInteraction
import gg.ginco.markers.resource.LocationMarkerResource
import gg.ginco.markers.system.MarkerVisibilitySystem

/**
 * Registers the needed resources, components and systems for location markers to work.
 * If [registerCommands], marker setup and edit commands will also be registered.
 */
class LocationMarkerSystemsRegistrar(plugin: JavaPlugin, registerCommands: Boolean) {

    companion object {
        private val LOGGER = HytaleLogger.forEnclosingClass()
        const val EDIT_MARKER_INTERACTION: String = "EditLocationMarker"
        const val MOVE_MARKER_INTERACTION: String = "MoveLocationMarker"
        const val ROTATE_MARKER_INTERACTION: String = "RotateLocationMarker"
    }

    /** The resource type that saves and loads world markers. */
    val markerResourceType: ResourceType<ChunkStore, LocationMarkerResource> =
        plugin.chunkStoreRegistry.registerResource(
            LocationMarkerResource::class.java,
            "LocationMarkers",
            LocationMarkerResource.CODEC
        ).also { LOGGER.atInfo().log("Registered resource LocationMarker.") }

    /** Component added onto players to make markers in the world visible. */
    val visibleMarkersComponentType: ComponentType<EntityStore, VisibleMarkersComponent> =
        plugin.entityStoreRegistry.registerComponent(VisibleMarkersComponent::class.java)
        { VisibleMarkersComponent() }.also {
            LOGGER.atInfo().log("Registered visible markers component.")
        }

    /** Component containing which markers has been selected by an entity. */
    val selectedMarkerComponentType: ComponentType<EntityStore, SelectedMarkerComponent> =
        plugin.entityStoreRegistry.registerComponent(SelectedMarkerComponent::class.java)
        { SelectedMarkerComponent() }.also {
            LOGGER.atInfo().log("Registered selected marker component.")
        }

    init {
        plugin.getCodecRegistry(Interaction.CODEC).register(
            EDIT_MARKER_INTERACTION, MarkerEditInteraction::class.java,
            BuilderCodec.builder(MarkerEditInteraction::class.java) { MarkerEditInteraction(this) }.build()
        )

        plugin.getCodecRegistry(Interaction.CODEC).register(
            MOVE_MARKER_INTERACTION, MarketMoveInteraction::class.java,
            BuilderCodec.builder(MarketMoveInteraction::class.java) { MarketMoveInteraction(this) }.build()
        )


        plugin.getCodecRegistry(Interaction.CODEC).register(
            ROTATE_MARKER_INTERACTION, MarkerRotateInteraction::class.java,
            BuilderCodec.builder(MarkerRotateInteraction::class.java) { MarkerRotateInteraction(this) }.build()
        )

        if (registerCommands) {
            // Runs the logic for showing/hiding markers to players.
            plugin.entityStoreRegistry.registerSystem(MarkerVisibilitySystem(this))
            LOGGER.atInfo().log("Registered custom marker components.")

            plugin.commandRegistry.registerCommand(MarkerCommand(this))
            LOGGER.atInfo().log("Registered markers command.")
        }
    }
}