package gg.ginco.markers.resource

import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Resource
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
import gg.ginco.markers.resource.LocationMarkerResource.Companion.toConcurrentMarkerMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Serves as a general-purpose location storage that can be read independently of the state of the chunk.
 * Currently used to define spawnpoints in minigames, spaces on boards, etc.
 */
class LocationMarkerResource(initialMarkers: Collection<LocationMarker> = emptyList()) : Resource<ChunkStore> {

    private var markers: ConcurrentMap<String, LocationMarker> = initialMarkers.toConcurrentMarkerMap()

    companion object {
        fun Iterable<LocationMarker>.toConcurrentMarkerMap(): ConcurrentMap<String, LocationMarker> =
            ConcurrentHashMap(this.associateBy { requireNotNull(it.markerId) { "Marker has no id at ${it.location}" } })

        @JvmField
        val CODEC = BuilderCodec.builder(LocationMarkerResource::class.java) { LocationMarkerResource() }
            .append(
                KeyedCodec("LocationMarkers", LocationMarker.ARRAY_CODEC),
                { marker, value -> marker.markers = value.toList().toConcurrentMarkerMap() },
                { marker -> marker.markers.values.toTypedArray() })
            .add()
            .build()
    }

    /** Returns the marker with id [id]. */
    fun getMarker(id: String): LocationMarker? = markers[id]

    /** Returns all available markers in this world. */
    fun getMarkers(): Collection<LocationMarker> = markers.values

    /** Adds [marker] to this world. */
    fun addMarker(marker: LocationMarker) {
        markers[marker.markerId] = marker
    }

    /** Removes marker with id [id] from this world. */
    fun removeMarker(id: String) {
        markers.remove(id)
    }

    override fun clone(): Resource<ChunkStore> = LocationMarkerResource(getMarkers())
}