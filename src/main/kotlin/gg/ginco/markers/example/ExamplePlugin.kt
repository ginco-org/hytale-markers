package gg.ginco.markers.example

import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import gg.ginco.markers.LocationMarkerSystemsRegistrar

class ExamplePlugin(init: JavaPluginInit) : JavaPlugin(init) {

    lateinit var markerRegistrar: LocationMarkerSystemsRegistrar

    override fun setup() {
        markerRegistrar = LocationMarkerSystemsRegistrar(this, true)
    }

}