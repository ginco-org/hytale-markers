package gg.ginco.markers.menu

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.math.vector.Transform
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.protocol.packets.interface_.Page
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import dev.jonrapp.hytaleReactiveUi.events.EventBinding
import dev.jonrapp.hytaleReactiveUi.events.EventRouter
import dev.jonrapp.hytaleReactiveUi.pages.ReactiveUiPage
import gg.ginco.markers.LocationMarkerSystemsRegistrar
import gg.ginco.markers.ext.toPrettyString
import gg.ginco.markers.resource.LocationMarker
import java.util.*

/** Lists all markers in the world, sorted by distance. */
class MarkerEditorMenu(
    player: PlayerRef,
    private val marker: LocationMarker?,
    private val markerRegistrar: LocationMarkerSystemsRegistrar,
) : ReactiveUiPage(player, CustomPageLifetime.CanDismissOrCloseThroughInteraction) {

    val markerClone = marker?.clone() ?: LocationMarker().apply {
        location = player.transform.clone().apply {
            rotation = player.headRotation.clone()
        }
        markerId = UUID.randomUUID().toString()
    }

    private var perfectLocationMarked: Boolean = false
    private val dataElements: MutableList<Pair<String?, String?>> =
        markerClone.data?.toList()?.toMutableList() ?: mutableListOf()

    override fun build(
        ref: Ref<EntityStore>,
        commands: UICommandBuilder,
        events: UIEventBuilder,
        store: Store<EntityStore>
    ) {
        commands.append("Markers/MarkerEditor.ui")

        commands.set("#MarkerPerfectLocation #CheckBox.Value", perfectLocationMarked)

        markerClone.markerId?.let { commands.set("#MarkerId.Value", it) }
        markerClone.markerType?.let { commands.set("#MarkerType.Value", it) }

        markerClone.location?.let {
            commands.set("#MarkerLocation.Value", it.position.toPrettyString())
        }

        dataElements.forEachIndexed { index, pair ->
            val element = MarkerDataElement(pair, index, this)
            element.create("#MarkerData", index, commands, events)
        }

        bindEvent(
            CustomUIEventBindingType.Activating, "#Cancel",
            events,
            EventBinding.action("back")
                .onEvent {
                    val player = it.store.getComponent(it.ref, Player.getComponentType()) ?: return@onEvent
                    player.pageManager.openCustomPage(it.ref, it.store, MarkerListMenu(playerRef, markerRegistrar))
                })

        bindEvent(
            CustomUIEventBindingType.Activating, "#AddData", events,
            EventBinding.action("add-data").applyEventData().onEvent {
                saveCloneFields(it, false)

                // If we have any null keys, don't add another one. User must define a key for every pair first.
                if (dataElements.none { it.first.isNullOrBlank() }) {
                    dataElements += Pair(null, null)
                    rebuild()
                } else {
                    val player = it.store.getComponent(it.ref, Player.getComponentType()) ?: return@onEvent
                    player.sendMessage(Message.translation("ginco.general.marker.complete_pair"))
                    sendUpdate()
                }
            })

        bindEvent(
            CustomUIEventBindingType.Activating,
            "#Save",
            events,
            EventBinding.action("save").applyEventData().onEvent { eventContext ->
                val player =
                    eventContext.store.getComponent(eventContext.ref, Player.getComponentType()) ?: return@onEvent

                if (saveCloneFields(eventContext, true)) {
                    val inputMarkerId = eventContext.getParameter<String>("@MarkerId")

                    val world = requireNotNull(player.world)
                    { "Could not save marker fields and data, player is not in a world!" }

                    // If the id has been edited, remove the original marker from the list and add the copy
                    // with the new id.
                    marker?.markerId?.let { id ->
                        if (inputMarkerId != id) world.chunkStore.store.getResource(markerRegistrar.markerResourceType)
                            .removeMarker(id)
                    }

                    world.chunkStore.store.getResource(markerRegistrar.markerResourceType).addMarker(markerClone)
                    player.sendMessage(Message.translation("ginco.general.marker.saved"))

                    player.pageManager.openCustomPage(
                        eventContext.ref,
                        eventContext.store,
                        MarkerListMenu(playerRef, markerRegistrar)
                    )
                }
            }
        )
    }

    fun addEventData(eventBinding: EventBinding): EventBinding {
        return eventBinding.apply {
            withEventData("@MarkerId", Codec.STRING, "#MarkerId.Value")
            withEventData("@MarkerType", Codec.STRING, "#MarkerType.Value")
            withEventData("@MarkerPerfectLocation", Codec.BOOLEAN, "#MarkerPerfectLocation #CheckBox.Value")

            dataElements.forEachIndexed { index, _ ->
                withEventData(
                    "@MarkerData-$index-Key",
                    Codec.STRING,
                    "#MarkerData #MarkerDataElement$index #MarkerDataKey.Value"
                )
                withEventData(
                    "@MarkerData-$index-Value",
                    Codec.STRING,
                    "#MarkerData #MarkerDataElement$index #MarkerDataValue.Value"
                )
            }
        }
    }

    private fun EventBinding.applyEventData(): EventBinding {
        return addEventData(this)
    }

    /** Returns whether the checked fields are correct. */
    fun saveCloneFields(eventContext: EventRouter.EventContext, checkFields: Boolean = false): Boolean {
        val player =
            eventContext.store.getComponent(eventContext.ref, Player.getComponentType()) ?: return false

        val inputMarkerId = eventContext.getParameter<String>("@MarkerId")
        val inputMarkerType = eventContext.getParameter<String>("@MarkerType")

        markerClone.markerId = inputMarkerId.takeIf { !it.isNullOrBlank() } ?: UUID.randomUUID().toString()
        markerClone.markerType = inputMarkerType

        if (checkFields && inputMarkerType.isNullOrBlank()) {
            player.sendMessage(Message.translation("ginco.general.marker.empty_fields"))
            player.pageManager.setPage(eventContext.ref, eventContext.store, Page.None)

            return false
        }

        val perfectLocation = eventContext.getParameter<Boolean>("@MarkerPerfectLocation")
        if (perfectLocation ?: false) markerClone.location?.let {
            markerClone.location = it.perfectLocation()
        }

        perfectLocationMarked = perfectLocation == true

        extractData(eventContext).let {
            markerClone.data = it

            dataElements.clear()
            dataElements.addAll(it.toList())
        }

        return true
    }

    /** Extracts all the custom data fields from the form into a key value string map. */
    private fun extractData(eventContext: EventRouter.EventContext): Map<String, String> {
        val dataMap = dataElements.mapIndexedNotNull { index, _ ->
            val key = eventContext.getParameter<String>("@MarkerData-$index-Key")
            val value = eventContext.getParameter<String>("@MarkerData-$index-Value")

            if (key != null && value != null) key to value
            else null
        }.toMap()
        return dataMap
    }

    fun removeDataPair(index: Int, eventContext: EventRouter.EventContext) {
        saveCloneFields(eventContext, false)
        dataElements.removeAt(index)
        rebuild()
    }

    /** Horizontally centers the transform to the block below. */
    private fun Transform.perfectLocation(): Transform = clone().apply {
        position = position.clone().apply {
            x = kotlin.math.floor(x) + 0.5
            z = kotlin.math.floor(z) + 0.5
        }
    }

    override fun getRootContentSelector(): String = "#Content"
}