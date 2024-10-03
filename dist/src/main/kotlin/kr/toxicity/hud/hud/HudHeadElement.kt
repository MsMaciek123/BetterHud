package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.layout.HeadLayout
import kr.toxicity.hud.manager.PlayerHeadManager
import kr.toxicity.hud.renderer.HeadRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.NAME_SPACE_ENCODED
import kr.toxicity.hud.util.encodeKey
import kr.toxicity.hud.util.parseChar
import net.kyori.adventure.text.Component

class HudHeadElement(parent: HudImpl, private val head: HeadLayout, gui: GuiLocation, pixel: ImageLocation) {

    private val renderer = run {
        val final = head.location + pixel
        val shader = HudShader(
            gui,
            head.layer,
            head.outline
        )
        HeadRenderer(
            parent.getOrCreateSpace(-1),
            parent.getOrCreateSpace(-(head.head.pixel * 8 + 1)),
            (0..7).map { i ->
                val char = (++parent.imageChar).parseChar()
                val encode = "pixel_${head.head.pixel}".encodeKey()
                val fileName = "$NAME_SPACE_ENCODED:$encode.png"
                val ascent = final.y + i * head.head.pixel
                val height = head.head.pixel
                val shaderGroup = ShaderGroup(shader, fileName, 1.0, ascent)
                PlayerHeadManager.getHead(shaderGroup) ?: run {
                    parent.jsonArray?.let { array ->
                        HudImpl.createBit(shader, ascent) { y ->
                            array.add(JsonObject().apply {
                                addProperty("type", "bitmap")
                                addProperty("file", fileName)
                                addProperty("ascent", y)
                                addProperty("height", height)
                                add("chars", JsonArray().apply {
                                    add(char)
                                })
                            })
                        }
                    }
                    PlayerHeadManager.setHead(shaderGroup, char)
                    char
                }
            },
            parent.imageKey,
            head.head.pixel * 8,
            final.x,
            head.align,
            head.follow,
            head.cancelIfFollowerNotExists,
            head.conditions.and(head.head.conditions)
        ).getHead(UpdateEvent.EMPTY)
    }

    fun getHead(hudPlayer: HudPlayer) = renderer(hudPlayer)
}