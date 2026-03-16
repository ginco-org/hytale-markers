package gg.ginco.markers.menu

import com.hypixel.hytale.component.ResourceType
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.protocol.packets.interface_.Page
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
import dev.jonrapp.hytaleReactiveUi.bindings.UIBindable
import dev.jonrapp.hytaleReactiveUi.bindings.UIBinding
import dev.jonrapp.hytaleReactiveUi.elements.Element
import dev.jonrapp.hytaleReactiveUi.events.EventBinding
import dev.jonrapp.hytaleReactiveUi.utils.UiUtils
import gg.ginco.markers.ext.toPrettyString
import gg.ginco.markers.resource.LocationMarker
import gg.ginco.markers.resource.LocationMarkerResource

/** A UI element that represents a [LocationMarker]. It displays its type and id with buttons to teleport and edit it. */
class MarkerElement(
    private val marker: LocationMarker,
    private val index: Int,
    private val markerResourceType: ResourceType<ChunkStore, LocationMarkerResource>,
    parentMenu: MarkerListMenu
) : Element<MarkerListMenu>(parentMenu) {

    @UIBinding(selector = "#MarkerType.TextSpans")
    private val markerTypeLabel: UIBindable<String>? = null

    @UIBinding(selector = "#MarkerId.Value")
    private val markerIdField: UIBindable<String>? = null

    @UIBinding(selector = "#MarkerLocation.TextSpans")
    private val markerLocationLabel: UIBindable<String>? = null

    override fun onCreate(
        root: String,
        commands: UICommandBuilder,
        events: UIEventBuilder
    ) {
        commands.append(root, "Markers/MarkerElement.ui")

        markerTypeLabel?.set(marker.markerType)
        commands.set("${UiUtils.getArraySelector("#$elementSelectorId", index)} #MarkerId.Value", marker.markerId!!)
        // For some reason this does not work: markerIdField?.set(marker.markerId!!)

        markerLocationLabel?.set(marker.location?.position?.toPrettyString())

        bindEvent(
            CustomUIEventBindingType.Activating,
            "${UiUtils.getArraySelector("#$elementSelectorId", index)} #Edit",
            events,
            EventBinding.action("edit-${index}").onEvent {
                val player = it.store.getComponent(it.ref, Player.getComponentType()) ?: return@onEvent
                val playerRef = it.store.getComponent(it.ref, PlayerRef.getComponentType()) ?: return@onEvent

                player.pageManager.openCustomPage(it.ref, it.store, MarkerEditorMenu(playerRef, marker))
            })

        bindEvent(
            CustomUIEventBindingType.Activating,
            "${UiUtils.getArraySelector("#$elementSelectorId", index)} #Remove",
            events,
            EventBinding.action("remove-${index}").onEvent {
                val player = it.store.getComponent(it.ref, Player.getComponentType()) ?: return@onEvent
                val world = player.world ?: return@onEvent

                val markerId = marker.markerId ?: return@onEvent

                world.chunkStore.store.getResource(markerResourceType).removeMarker(markerId)
                player.pageManager.setPage(it.ref, it.store, Page.None)

                player.sendMessage(Message.translation("ginco.general.marker.removed").apply {
                    param("markerId", markerId)
                })
            })

        bindEvent(
            CustomUIEventBindingType.Activating,
            "${UiUtils.getArraySelector("#$elementSelectorId", index)} #Teleport",
            events,
            EventBinding.action("teleport-${index}").onEvent {
                val player = it.store.getComponent(it.ref, Player.getComponentType()) ?: return@onEvent
                val world = player.world ?: return@onEvent
                val markerLocation = marker.location

                world.execute {
                    if (markerLocation == null) {
                        player.sendMessage(Message.translation("ginco.general.marker.no_location"))
                        return@execute
                    }

                    player.pageManager.setPage(it.ref, it.store, Page.None)

                    val teleport: Teleport = Teleport.createForPlayer(world, markerLocation)
                    it.store.addComponent(it.ref, Teleport.getComponentType(), teleport)
                }
            })
    }
}