package kr.toxicity.hud.compatibility

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import org.bukkit.configuration.ConfigurationSection

interface Compatibility {
    val listeners: Map<String, (ConfigurationSection) -> HudListener>
    val numbers: Map<String, HudPlaceholder<Number>>
    val strings: Map<String, HudPlaceholder<String>>
    val booleans: Map<String, HudPlaceholder<Boolean>>
}