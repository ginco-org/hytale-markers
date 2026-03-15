package gg.ginco.markers.ext

import com.hypixel.hytale.math.vector.Vector3d

/** Returns a pretty representation of a Vector3d. */
fun Vector3d.toPrettyString(): String {
    /** Clamps the decimal numbers on a double to the first two. */
    fun Double.round(): Double = kotlin.math.round(this * 100) / 100

    return "${x.round()}, ${y.round()}, ${z.round()}"
}