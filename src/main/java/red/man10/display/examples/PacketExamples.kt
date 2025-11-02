package red.man10.display.examples

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import red.man10.display.Main

/**
 * Minecraft 1.21.8 パケット送信サンプルクラス
 * 
 * ProtocolLibを使用してパケットを送信する実装例です。
 * 各メソッドは独立して使用できます。
 * 
 * 注意: 一部のパケットは複雑な構造を持つため、実装が簡略化されています。
 * 実際の使用では、適切なデータ型とフィールドインデックスを確認してください。
 */
object PacketExamples {
    
    // ==========================================
    // チャット関連パケット
    // ==========================================
    
    /**
     * システムチャットを送信
     * @param player 送信先プレイヤー
     * @param message メッセージ
     * @param overlay オーバーレイ表示（true=アクションバー、false=チャット欄）
     */
    fun sendSystemChat(player: Player, message: String, overlay: Boolean = false) {
        val packet = PacketContainer(PacketType.Play.Server.SYSTEM_CHAT)
        
        // メッセージコンポーネント（JSON文字列として送信）
        val component = WrappedChatComponent.fromText(message)
        packet.chatComponents.write(0, component)
        
        // オーバーレイ表示設定
        packet.booleans.write(0, overlay)
        
        Main.protocolManager.sendServerPacket(player, packet)
    }
    
    /**
     * タイトルを表示
     * @param player 送信先プレイヤー
     * @param title タイトルテキスト
     * @param subtitle サブタイトルテキスト（省略可）
     * @param fadeIn フェードイン時間（tick）
     * @param stay 表示時間（tick）
     * @param fadeOut フェードアウト時間（tick）
     */
    fun sendTitle(
        player: Player,
        title: String,
        subtitle: String = "",
        fadeIn: Int = 10,
        stay: Int = 70,
        fadeOut: Int = 20
    ) {
        // タイトルテキスト
        val titlePacket = PacketContainer(PacketType.Play.Server.SET_TITLE_TEXT)
        titlePacket.chatComponents.write(0, WrappedChatComponent.fromText(title))
        Main.protocolManager.sendServerPacket(player, titlePacket)
        
        // サブタイトルテキスト
        if (subtitle.isNotEmpty()) {
            val subtitlePacket = PacketContainer(PacketType.Play.Server.SET_SUBTITLE_TEXT)
            subtitlePacket.chatComponents.write(0, WrappedChatComponent.fromText(subtitle))
            Main.protocolManager.sendServerPacket(player, subtitlePacket)
        }
        
        // タイミング設定
        val timesPacket = PacketContainer(PacketType.Play.Server.SET_TITLES_ANIMATION)
        timesPacket.integers.write(0, fadeIn)
        timesPacket.integers.write(1, stay)
        timesPacket.integers.write(2, fadeOut)
        Main.protocolManager.sendServerPacket(player, timesPacket)
    }
    
    /**
     * アクションバーを表示
     * @param player 送信先プレイヤー
     * @param message メッセージ
     */
    fun sendActionBar(player: Player, message: String) {
        val packet = PacketContainer(PacketType.Play.Server.SET_ACTION_BAR_TEXT)
        packet.chatComponents.write(0, WrappedChatComponent.fromText(message))
        Main.protocolManager.sendServerPacket(player, packet)
    }
    
    // ==========================================
    // サウンド関連パケット
    // ==========================================
    
    /**
     * サウンドを再生
     * @param player 送信先プレイヤー
     * @param sound サウンドタイプ
     * @param location 再生位置
     * @param volume 音量（0.0-1.0）
     * @param pitch ピッチ（0.0-2.0）
     */
    fun sendSound(
        player: Player,
        sound: Sound,
        location: Location,
        volume: Float = 1.0f,
        pitch: Float = 1.0f
    ) {
        val packet = PacketContainer(PacketType.Play.Server.NAMED_SOUND_EFFECT)
        
        // サウンド名
        packet.soundEffects.write(0, sound)
        
        // サウンドカテゴリ
        packet.soundCategories.write(0, EnumWrappers.SoundCategory.MASTER)
        
        // 位置（固定小数点、8倍）
        packet.integers.write(0, (location.x * 8).toInt())
        packet.integers.write(1, (location.y * 8).toInt())
        packet.integers.write(2, (location.z * 8).toInt())
        
        // 音量とピッチ（floatアクセサを使用）
        packet.float.write(0, volume)
        packet.float.write(1, pitch)
        
        Main.protocolManager.sendServerPacket(player, packet)
    }
    
    // ==========================================
    // ブロック関連パケット
    // ==========================================
    
    /**
     * ブロック変更パケットを送信
     * @param player 送信先プレイヤー
     * @param location ブロック位置
     * @param blockData ブロックデータ
     */
    fun sendBlockChange(
        player: Player,
        location: Location,
        blockData: org.bukkit.block.data.BlockData
    ) {
        val packet = PacketContainer(PacketType.Play.Server.BLOCK_CHANGE)
        
        // ブロック位置
        packet.blockPositionModifier.write(0,
            com.comphenix.protocol.wrappers.BlockPosition(
                location.blockX,
                location.blockY,
                location.blockZ
            )
        )
        
        // ブロックデータ（WrappedBlockDataに変換）
        val wrappedBlockData = com.comphenix.protocol.wrappers.WrappedBlockData.createData(blockData)
        packet.blockData.write(0, wrappedBlockData)
        
        Main.protocolManager.sendServerPacket(player, packet)
    }
    
    // ==========================================
    // パーティクル関連パケット
    // ==========================================
    
    /**
     * パーティクルを表示
     * @param player 送信先プレイヤー
     * @param particle パーティクルタイプ
     * @param location 位置
     * @param count パーティクル数
     * @param offsetX X方向オフセット
     * @param offsetY Y方向オフセット
     * @param offsetZ Z方向オフセット
     * @param extra 追加データ
     * @param longDistance ロング距離表示
     */
    fun sendParticle(
        player: Player,
        particle: org.bukkit.Particle,
        location: Location,
        count: Int = 1,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0,
        offsetZ: Double = 0.0,
        extra: Double = 0.0,
        longDistance: Boolean = false
    ) {
        val packet = PacketContainer(PacketType.Play.Server.WORLD_PARTICLES)
        
        // パーティクルタイプ（EnumWrappers.Particleに変換）
        val wrappedParticle = EnumWrappers.Particle.valueOf(particle.name)
        packet.particles.write(0, wrappedParticle)
        
        // 位置
        packet.doubles.write(0, location.x)
        packet.doubles.write(1, location.y)
        packet.doubles.write(2, location.z)
        
        // オフセット
        packet.float.write(0, offsetX.toFloat())
        packet.float.write(1, offsetY.toFloat())
        packet.float.write(2, offsetZ.toFloat())
        
        // 速度
        packet.float.write(3, extra.toFloat())
        
        // カウント
        packet.integers.write(0, count)
        
        // ロング距離表示
        packet.booleans.write(0, longDistance)
        
        Main.protocolManager.sendServerPacket(player, packet)
    }
    
    // ==========================================
    // エンティティ関連パケット
    // ==========================================
    
    /**
     * エンティティメタデータを更新
     * @param player 送信先プレイヤー
     * @param entityId エンティティID
     * @param metadata メタデータリスト（空の場合は更新なし）
     */
    fun updateEntityMetadata(player: Player, entityId: Int, metadata: List<Any> = emptyList()) {
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
        
        // エンティティID
        packet.integers.write(0, entityId)
        
        // メタデータリスト
        packet.modifier.write(0, metadata)
        
        Main.protocolManager.sendServerPacket(player, packet)
    }
    
    // ==========================================
    // ゲーム状態関連パケット
    // ==========================================
    
    /**
     * ゲーム状態を変更
     * @param player 送信先プレイヤー
     * @param reason 理由（0=無効な床, 1=雨の開始, 2=雨の終了, 3=ゲームモード変更, など）
     * @param value 値（ゲームモード変更の場合は0-3）
     */
    fun sendGameStateChange(player: Player, reason: Int, value: Float) {
        val packet = PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE)
        
        // 理由
        packet.integers.write(0, reason)
        
        // 値
        packet.float.write(0, value)
        
        Main.protocolManager.sendServerPacket(player, packet)
    }
    
    /**
     * ゲームモードを変更（ヘルパーメソッド）
     * @param player 送信先プレイヤー
     * @param gamemode ゲームモード（0=サバイバル, 1=クリエイティブ, 2=アドベンチャー, 3=スペクテイター）
     */
    fun setGamemode(player: Player, gamemode: Int) {
        // 理由3=ゲームモード変更
        sendGameStateChange(player, 3, gamemode.toFloat())
    }
    
    // ==========================================
    // 一括送信ヘルパー
    // ==========================================
    
    /**
     * 複数のプレイヤーに同じパケットを送信
     * @param players 送信先プレイヤーリスト
     * @param packet 送信するパケット
     */
    fun sendToPlayers(players: List<Player>, packet: PacketContainer) {
        players.forEach { player ->
            if (player.isOnline) {
                try {
                    Main.protocolManager.sendServerPacket(player, packet)
                } catch (e: Exception) {
                    Main.plugin.logger.warning("パケット送信失敗: ${player.name} - ${e.message}")
                }
            }
        }
    }
    
    /**
     * 全プレイヤーにパケットを送信
     * @param packet 送信するパケット
     */
    fun sendToAllPlayers(packet: PacketContainer) {
        sendToPlayers(Main.plugin.server.onlinePlayers.toList(), packet)
    }
}

