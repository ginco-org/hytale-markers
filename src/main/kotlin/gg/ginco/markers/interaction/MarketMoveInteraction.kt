package gg.ginco.markers.interaction

import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.math.vector.Transform
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.math.vector.Vector3i
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.TargetUtil
import gg.ginco.markers.LocationMarkerSystemsRegistrar
import gg.ginco.markers.component.SelectedMarkerComponent

class MarketMoveInteraction(private val markerRegistrar: LocationMarkerSystemsRegistrar) : SimpleBlockInteraction() {

    override fun interactWithBlock(
        world: World,
        commandBuffer: CommandBuffer<EntityStore?>,
        type: InteractionType,
        context: InteractionContext,
        itemInHand: ItemStack?,
        pos: Vector3i,
        cooldownHandler: CooldownHandler
    ) {
        world.execute {
            val ref = context.entity
            val store = ref.store

            val selectedMarker = store.getComponent(ref, markerRegistrar.selectedMarkerComponentType)?.selectedMarker

            if (selectedMarker == null) {
                val closestMarker = world.chunkStore.store.getResource(markerRegistrar.markerResourceType).getMarkers()
                    .minByOrNull { it.location?.position?.distanceSquaredTo(pos) ?: Double.MAX_VALUE }

                store.addComponent(
                    ref,
                    markerRegistrar.selectedMarkerComponentType,
                    SelectedMarkerComponent(closestMarker?.markerId)
                )

                // TODO: Some sound?
                return@execute
            }

            val transformComponent = store.getComponent(ref, TransformComponent.getComponentType()) ?: return@execute
            val headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType())?:return@execute

            val playerPosition = transformComponent.position
            val headRotation = headRotationComponent.rotation
            val direction = Transform.getDirection(headRotation.pitch, headRotation.yaw)

            val lookTarget = TargetUtil.getTargetLocation(ref, 15.0, store)

            val newPosition: Vector3d?
            if (lookTarget != null) {
                newPosition = lookTarget
            } else {
                val aheadPosition = playerPosition.clone().add(direction.clone().scale(15.0))
                val world = store.getExternalData().world
                val groundTarget = TargetUtil.getTargetBlock(
                    world,
                    { blockId: Int, _: Int -> blockId != 0 },
                    aheadPosition.x,
                    aheadPosition.y + 0.5,
                    aheadPosition.z,
                    0.0,
                    -1.0,
                    0.0,
                    3.0
                )
                newPosition = if (groundTarget != null) {
                    Vector3d(
                        groundTarget.x.toDouble() + 0.5,
                        (groundTarget.y + 1).toDouble(),
                        groundTarget.z.toDouble() + 0.5
                    )
                } else {
                    aheadPosition
                }
            }

            val markerToMove = world.chunkStore.store.getResource(markerRegistrar.markerResourceType).getMarker(selectedMarker)
                ?: return@execute
            val oldLocation = markerToMove.location

            markerToMove.location = Transform(
                newPosition,
                oldLocation?.rotation ?: Vector3f(Float.NaN, Float.NaN, Float.NaN)
            )

            store.removeComponent(ref, markerRegistrar.selectedMarkerComponentType)
        }
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