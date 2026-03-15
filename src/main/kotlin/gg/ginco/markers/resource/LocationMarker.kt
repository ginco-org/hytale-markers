package gg.ginco.markers.resource

import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import com.hypixel.hytale.math.vector.Location
import com.hypixel.hytale.math.vector.Transform
import gg.ginco.jellyparty.codec.annotations.SerialWithCodec
import gg.ginco.jellyparty.codec.annotations.SerializableObject
import java.util.*

/**
 * Contains all data related to a location marker, to be used and referenced in code.
 * - [location]: Location where the marker was placed.
 * - [markerId]: Randomized UUID by default
 * - [markerType]: Used to identify what type of marker this is.
 * - [data]: Any extra contextual data for this marker:
 *     For example: Neighbor spaces or team that owns this location.
 */
@SerializableObject(extraImports = ["com.hypixel.hytale.codec.codecs.map.MapCodec", "com.hypixel.hytale.math.vector.Transform"])
class LocationMarker() {

    var markerId: String? = null
    var markerType: String? = null

    @SerialWithCodec("Transform.CODEC")
    var location: Transform? = null

    @SerialWithCodec("MapCodec.STRING_HASH_MAP_CODEC")
    var data: Map<String, String>? = null

    /** Location marker from a [location]. */
    constructor(
        location: Location,
        markerType: String,
        data: Map<String, String> = emptyMap(),
        markerId: String = UUID.randomUUID().toString(),
    ) : this() {
        this.data = data
        this.location = location.toTransform()
        this.markerId = markerId
        this.markerType = markerType
    }

    constructor(
        transform: Transform,
        markerType: String,
        data: Map<String, String> = emptyMap(),
        markerId: String = UUID.randomUUID().toString(),
    ) : this() {
        this.data = data
        this.location = transform
        this.markerId = markerId
        this.markerType = markerType
    }

    fun clone() : LocationMarker = LocationMarker().apply {
        location = this@LocationMarker.location
        markerType = this@LocationMarker.markerType
        markerId = this@LocationMarker.markerId
        data = this@LocationMarker.data?.toMap()
    }

    companion object {
        @JvmField
        val ARRAY_CODEC = ArrayCodec(LocationMarkerCodec) { arrayOfNulls<LocationMarker>(it) }
    }
}