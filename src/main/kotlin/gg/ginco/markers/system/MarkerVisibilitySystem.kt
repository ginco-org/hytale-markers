package gg.ginco.markers.system

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem
import com.hypixel.hytale.math.matrix.Matrix4d
import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.math.vector.Transform
import com.hypixel.hytale.protocol.DebugShape
import com.hypixel.hytale.protocol.Vector3f
import com.hypixel.hytale.protocol.packets.player.ClearDebugShapes
import com.hypixel.hytale.protocol.packets.player.DisplayDebug
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.modules.debug.DebugUtils
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import gg.ginco.markers.LocationMarkerSystemsRegistrar
import gg.ginco.markers.resource.LocationMarker

/** Shows marker locations to any player with the [selectedMarkerComponent]. */
class MarkerVisibilitySystem(
    private val registrar: LocationMarkerSystemsRegistrar
) :
    DelayedEntitySystem<EntityStore>(
        0.2f
    ) {

    companion object {
        val CLEAR_SHAPES_PACKET: ClearDebugShapes = ClearDebugShapes()

        private val YELLOW_COLOR = Vector3f(1.0f, 0.90588236f, 0.28235295f)
        private val GREEN_COLOR = Vector3f(0.21568628f, 0.87058824f, 0.0f)

        // Display markers for 1 full second even though we re-send them every 0.2s
        // to account for unstable connections or higher ping.
        private const val DISPLAY_TIME: Float = 1.0f
    }

    private val playerRefComponent = PlayerRef.getComponentType()
    private val playerComponent = Player.getComponentType()
    private val transformComponent = TransformComponent.getComponentType()
    private val visibleMarkersComponent = registrar.visibleMarkersComponentType
    private val selectedMarkerComponent = registrar.selectedMarkerComponentType

    private var packetsToSend: Map<LocationMarker, List<DisplayDebug>> = mapOf()

    private val query =
        Query.and(visibleMarkersComponent, playerRefComponent, playerComponent, transformComponent)

    override fun tick(
        dt: Float,
        index: Int,
        chunk: ArchetypeChunk<EntityStore>,
        store: Store<EntityStore>,
        buffer: CommandBuffer<EntityStore>
    ) {
        val player = chunk.getComponent(index, playerComponent) ?: return
        val world = player.world ?: return

        // Pre-compute the marker display packets with all the matrix and weird math.
        // Once per full tick loop, as it is shared for all players.
        if (index == 0) {
            val markers = world.chunkStore.store.getResource(registrar.markerResourceType).getMarkers()
            val currentPackets: MutableMap<LocationMarker, List<DisplayDebug>> = mutableMapOf()

            markers.forEach { marker ->
                if (marker.markerId == null) return@forEach

                val transform = marker.location ?: return@forEach
                val position = transform.position

                // If the chunk where this marker is placed is not loaded, don't even bother!
                if (world.chunkStore.getChunkReference(
                        ChunkUtil.indexChunkFromBlock(
                            position.x,
                            position.z
                        )
                    ) == null
                ) return@forEach

                // Actually save all the packets needed to visualize this marker!
                currentPackets[marker] = transform.getPacketList()
            }

            packetsToSend = currentPackets
        }

        val playerRef = chunk.getComponent(index, playerRefComponent) ?: return
        val selectedMarkerId = chunk.getComponent(index, selectedMarkerComponent)?.selectedMarker

        // Append the clear shapes packet first, to disregard old visuals and then only
        // send visuals for markers at 120 blocks or fewer.
        val allPackets = listOf(CLEAR_SHAPES_PACKET).plus(packetsToSend.flatMap { (markerId, packets) ->
            val distance = markerId.location?.position?.distanceSquaredTo(playerRef.transform.position)
                ?: return@flatMap emptyList()
            if (distance >= 120 * 120) return@flatMap emptyList()

            // If this marker is currently selected by the player use the green color on its visuals.
            if (selectedMarkerId != null && selectedMarkerId == markerId.markerId)
                packets.map { it.clone().apply { color = GREEN_COLOR } }
            else packets
        })

        // Send all the packets bundled together to avoid flickering as much as possible.
        playerRef.packetHandler.write(*allPackets.toTypedArray())
    }

    /** Returns all packets needed to visualize this transform: A small cube and an arrow for the rotation. */
    private fun Transform.getPacketList(): List<DisplayDebug> {
        val packetList = mutableListOf<DisplayDebug>()

        val cubeMatrix = Matrix4d().apply {
            identity()
            translate(position.x, position.y + 0.26, position.z)
            scale(0.5, 0.5, 0.5)
        }

        packetList += DisplayDebug(
            DebugShape.Cube,
            cubeMatrix.asFloatData(),
            YELLOW_COLOR,
            DISPLAY_TIME,
            DebugUtils.FLAG_NONE.toByte(),
            null,
            0.8f
        )

        val lookYaw = rotation.yaw
        val lookPitch = rotation.pitch

        val matrix = Matrix4d().apply {
            val tmp = Matrix4d()

            identity()
            translate(position.x, position.y + 2.0, position.z)
            rotateAxis((-lookYaw).toDouble(), 0.0, 1.0, 0.0, tmp)
            rotateAxis((Math.PI / 2.0) - lookPitch.toDouble(), 1.0, 0.0, 0.0, tmp)
        }

        // Arrow Cylinder
        val cylinderMatrix = Matrix4d(matrix).apply {
            translate(0.0, 0.7 * 0.5, 0.0)
            scale(0.1, 0.7, 0.1)
        }

        packetList += DisplayDebug(
            DebugShape.Cylinder,
            cylinderMatrix.asFloatData(),
            YELLOW_COLOR,
            DISPLAY_TIME,
            DebugUtils.FLAG_NONE.toByte(),
            null,
            0.8f
        )

        val arrowMatrix = Matrix4d(matrix).apply {
            translate(0.0, 0.7 + 0.15, 0.0)
            scale(0.3, 0.3, 0.3)
        }

        packetList += DisplayDebug(
            DebugShape.Cone,
            arrowMatrix.asFloatData(),
            YELLOW_COLOR,
            DISPLAY_TIME,
            DebugUtils.FLAG_NONE.toByte(),
            null,
            0.8f
        )

        return packetList
    }

    override fun getQuery(): Query<EntityStore> = query
}