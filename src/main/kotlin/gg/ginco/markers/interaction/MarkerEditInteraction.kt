package gg.ginco.markers.interaction

import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.math.vector.Vector3i
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import gg.ginco.markers.LocationMarkerSystemsRegistrar
import gg.ginco.markers.menu.MarkerEditorMenu

class MarkerEditInteraction(private val markerRegistrar: LocationMarkerSystemsRegistrar) : SimpleBlockInteraction() {

    override fun interactWithBlock(
        world: World,
        commandBuffer: CommandBuffer<EntityStore?>,
        type: InteractionType,
        context: InteractionContext,
        itemInHand: ItemStack?,
        pos: Vector3i,
        cooldownHandler: CooldownHandler
    ) {
        val ref = context.entity
        val player = commandBuffer.getComponent(ref, Player.getComponentType()) ?: return

        val closestMarker = world.chunkStore.store.getResource(markerRegistrar.markerResourceType).getMarkers()
            .minByOrNull { it.location?.position?.distanceSquaredTo(pos) ?: Double.MAX_VALUE } ?: return

        val playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType()) ?: return

        player.pageManager.openCustomPage(ref, ref.store, MarkerEditorMenu(playerRef, closestMarker, markerRegistrar))
    }

    override fun simulateInteractWithBlock(
        p0: InteractionType,
        p1: InteractionContext,
        p2: ItemStack?,
        p3: World,
        p4: Vector3i
    ) {
    }


}