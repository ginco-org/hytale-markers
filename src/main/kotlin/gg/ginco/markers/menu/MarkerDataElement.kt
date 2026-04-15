package gg.ginco.markers.menu

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import dev.jonrapp.hytaleReactiveUi.elements.Element
import dev.jonrapp.hytaleReactiveUi.events.EventBinding
import dev.jonrapp.hytaleReactiveUi.utils.UiUtils
import gg.ginco.markers.resource.LocationMarker

/** A UI element that represents a key and value data pair in a [LocationMarker].*/
class MarkerDataElement(
    val data: Pair<String?, String?>,
    private val index: Int,
    private val parentMenu: MarkerEditorMenu
) : Element<MarkerEditorMenu>(parentMenu) {

    val localSelector: String
        get() = UiUtils.getArraySelector("#$elementSelectorId", index)

    override fun onCreate(
        root: String,
        commands: UICommandBuilder,
        events: UIEventBuilder
    ) {
        commands.append(root, "Markers/MarkerDataElement.ui")

        data.first?.let { commands.set("$localSelector #MarkerDataKey.Value", it) }
        data.second?.let { commands.set("$localSelector #MarkerDataValue.Value", it) }

        bindEvent(
            CustomUIEventBindingType.Activating,
            "$localSelector #Delete",
            events,
            EventBinding.action("delete-${index}").also { parentMenu.addEventData(it) }.onEvent {
                parentMenu.removeDataPair(index, it)
            })
    }
}