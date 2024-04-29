package kr.toxicity.hud.player.head

import net.skinsrestorer.api.SkinsRestorerProvider
import org.bukkit.entity.Player

class SkinsRestorerSkinProvider: PlayerSkinProvider {
    override fun provide(player: Player): String? {
        return SkinsRestorerProvider.get().playerStorage.getSkinOfPlayer(player.uniqueId).map {
            it.value
        }.orElse(null)
    }
}