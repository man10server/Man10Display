# Minecraft 1.21.8 パケット情報とサンプルコード

## 📋 概要

このドキュメントは、Minecraft 1.21.8（Paper API）で使用可能なパケット情報と、ProtocolLibを使用した実装例をまとめたものです。

## 🛠️ 技術スタック

- **Minecraft**: 1.21.8
- **Paper API**: 1.21.8-R0.1-SNAPSHOT
- **ProtocolLib**: 5.0.0
- **Java**: 21
- **Kotlin**: 2.0.0

## 📦 ProtocolLibの基本セットアップ

```kotlin
// Main.kt
class Main : JavaPlugin() {
    companion object {
        lateinit var protocolManager: ProtocolManager
    }
    
    override fun onEnable() {
        // ProtocolLibの初期化
        val protocolLibPlugin = server.pluginManager.getPlugin("ProtocolLib")
        if (protocolLibPlugin == null || !protocolLibPlugin.isEnabled) {
            server.logger.severe("ProtocolLibが見つかりません")
            server.pluginManager.disablePlugin(this)
            return
        }
        
        protocolManager = ProtocolLibrary.getProtocolManager()
    }
}
```

## 📮 パケット送信の基本パターン

```kotlin
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player

// パケットを作成
val packet = PacketContainer(PacketType.Play.Server.XXX)

// パケットにデータを書き込む
packet.integers.write(0, 123)
packet.strings.write(0, "Hello")

// プレイヤーに送信
protocolManager.sendServerPacket(player, packet)

// 複数のプレイヤーに送信
players.forEach { player ->
    protocolManager.sendServerPacket(player, packet)
}
```

## 🎯 主要なパケットタイプ一覧

### 1. チャット関連パケット

#### SystemChatPacket（システムチャット）
```kotlin
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import net.kyori.adventure.text.Component

fun sendSystemChat(player: Player, message: String) {
    val packet = PacketContainer(PacketType.Play.Server.SYSTEM_CHAT)
    
    // メッセージコンポーネント
    val component = Component.text(message)
    packet.chatComponents.write(0, component)
    
    // オーバーレイ表示（false = チャット欄に表示）
    packet.booleans.write(0, false)
    
    protocolManager.sendServerPacket(player, packet)
}
```

#### PlayerChatPacket（プレイヤーチャット）
```kotlin
fun sendPlayerChat(player: Player, sender: Player, message: String) {
    val packet = PacketContainer(PacketType.Play.Server.PLAYER_CHAT)
    
    // 送信者情報
    packet.uuids.write(0, sender.uniqueId)
    
    // メッセージ
    packet.chatComponents.write(0, Component.text(message))
    
    // 署名（署名機能が有効な場合）
    packet.booleans.write(0, false)
    
    protocolManager.sendServerPacket(player, packet)
}
```

### 2. エンティティ関連パケット

#### EntityMetadata（エンティティメタデータ）
```kotlin
import org.bukkit.entity.Entity

fun updateEntityMetadata(player: Player, entity: Entity) {
    val packet = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
    
    // エンティティID
    packet.integers.write(0, entity.entityId)
    
    // メタデータリスト（空の場合は空リスト）
    val metadata = mutableListOf<Any>()
    packet.modifier.write(0, metadata)
    
    protocolManager.sendServerPacket(player, packet)
}
```

#### SpawnEntityLiving（リビングエンティティ生成）
```kotlin
import org.bukkit.entity.LivingEntity

fun spawnLivingEntity(player: Player, entity: LivingEntity) {
    val packet = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING)
    
    // エンティティID
    packet.integers.write(0, entity.entityId)
    
    // UUID
    packet.uuids.write(0, entity.uniqueId)
    
    // エンティティタイプ
    packet.entityTypeModifier.write(0, entity.type)
    
    // 位置
    packet.doubles.write(0, entity.location.x)
    packet.doubles.write(1, entity.location.y)
    packet.doubles.write(2, entity.location.z)
    
    // 回転
    packet.bytes.write(0, (entity.location.yaw * 256.0f / 360.0f).toByte())
    packet.bytes.write(1, (entity.location.pitch * 256.0f / 360.0f).toByte())
    
    protocolManager.sendServerPacket(player, packet)
}
```

#### EntityEquipment（エンティティ装備）
```kotlin
import org.bukkit.inventory.EquipmentSlot

fun updateEntityEquipment(player: Player, entity: LivingEntity, slot: EquipmentSlot, item: ItemStack) {
    val packet = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)
    
    // エンティティID
    packet.integers.write(0, entity.entityId)
    
    // 装備スロットリスト
    val slots = mutableListOf<Pair<EquipmentSlot, ItemStack>>()
    slots.add(Pair(slot, item))
    packet.modifier.write(0, slots)
    
    protocolManager.sendServerPacket(player, packet)
}
```

### 3. プレイヤー関連パケット

#### PlayerInfo（プレイヤー情報）
```kotlin
fun sendPlayerInfo(player: Player, targetPlayer: Player) {
    val packet = PacketContainer(PacketType.Play.Server.PLAYER_INFO)
    
    // アクションタイプ（ADD_PLAYER, UPDATE_GAMEMODE, UPDATE_LATENCY, UPDATE_DISPLAY_NAME, REMOVE_PLAYER）
    packet.playerInfoActions.write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER)
    
    // プレイヤー情報リスト
    val playerInfoData = mutableListOf<Any>()
    // プレイヤー情報を構築して追加
    packet.modifier.write(0, playerInfoData)
    
    protocolManager.sendServerPacket(player, packet)
}
```

#### GameStateChange（ゲーム状態変更）
```kotlin
import com.comphenix.protocol.wrappers.WrappedGameProfile

fun sendGameStateChange(player: Player, reason: Int, value: Float) {
    val packet = PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE)
    
    // 理由（0=無効な床, 1=雨の開始, 2=雨の終了, 3=ゲームモード変更, など）
    packet.integers.write(0, reason)
    
    // 値（ゲームモード変更の場合は0-3）
    packet.float.read(0, value)
    
    protocolManager.sendServerPacket(player, packet)
}

// 使用例: クリエイティブモードに変更
fun setCreativeMode(player: Player) {
    sendGameStateChange(player, 3, 1.0f) // 3=ゲームモード変更, 1=クリエイティブ
}
```

### 4. ワールド関連パケット

#### Map（地図パケット）※既存実装参考
```kotlin
// MapPacketSender.ktを参考に実装
fun sendMapPacket(player: Player, mapId: Int, data: ByteArray) {
    val packet = PacketContainer(PacketType.Play.Server.MAP)
    
    // MapIdの書き込み
    val mapIdClass = Class.forName("net.minecraft.world.level.saveddata.maps.MapId")
    val mapIdConstructor = mapIdClass.getDeclaredConstructor(Int::class.javaPrimitiveType)
    mapIdConstructor.isAccessible = true
    val mapIdInstance = mapIdConstructor.newInstance(mapId)
    
    // MapPatchの作成
    val mapPatchClass = Class.forName("net.minecraft.world.level.saveddata.maps.MapItemSavedData\$MapPatch")
    val mapPatchConstructor = mapPatchClass.getDeclaredConstructor(
        Int::class.javaPrimitiveType, // width
        Int::class.javaPrimitiveType, // height
        Int::class.javaPrimitiveType, // x
        Int::class.javaPrimitiveType, // y
        ByteArray::class.java          // data
    )
    mapPatchConstructor.isAccessible = true
    val mapPatch = mapPatchConstructor.newInstance(128, 128, 0, 0, data)
    
    // パケットに書き込み
    packet.modifier.write(0, java.util.Optional.of(mapIdInstance))
    packet.modifier.write(1, java.util.Optional.of(mapPatch))
    
    protocolManager.sendServerPacket(player, packet)
}
```

#### ChunkData（チャンクデータ）
```kotlin
import org.bukkit.Chunk

fun sendChunkData(player: Player, chunk: Chunk) {
    val packet = PacketContainer(PacketType.Play.Server.MAP_CHUNK)
    
    // チャンク座標
    packet.integers.write(0, chunk.x)
    packet.integers.write(1, chunk.z)
    
    // チャンクデータ
    // 注意: チャンクデータの構造は複雑で、NMS（net.minecraft.server）を使用する必要があります
    // Paper APIでは直接チャンクデータを送信する機能が提供されていないため、
    // このパケットは高度な実装が必要です
    
    protocolManager.sendServerPacket(player, packet)
}
```

### 5. ブロック関連パケット

#### BlockChange（ブロック変更）
```kotlin
import org.bukkit.block.Block

fun sendBlockChange(player: Player, block: Block) {
    val packet = PacketContainer(PacketType.Play.Server.BLOCK_CHANGE)
    
    // ブロック位置
    packet.blockPositionModifier.write(0, 
        com.comphenix.protocol.wrappers.BlockPosition(
            block.x, block.y, block.z
        )
    )
    
    // ブロックタイプ
    packet.blockData.write(0, block.blockData)
    
    protocolManager.sendServerPacket(player, packet)
}
```

#### MultiBlockChange（複数ブロック変更）
```kotlin
fun sendMultiBlockChange(player: Player, blocks: List<Block>) {
    val packet = PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE)
    
    // チャンク座標
    if (blocks.isNotEmpty()) {
        val firstBlock = blocks[0]
        packet.longs.write(0, ((firstBlock.x shr 4).toLong() shl 32) or ((firstBlock.z shr 4).toLong() and 0xFFFFFFFFL))
        
        // ブロック変更リスト
        val changes = blocks.map { block ->
            // ブロック変更情報を構築
            // 実装は複雑なため、NMSを使用する必要があります
        }
        packet.modifier.write(0, changes)
    }
    
    protocolManager.sendServerPacket(player, packet)
}
```

### 6. パーティクル関連パケット

#### WorldParticles（ワールドパーティクル）
```kotlin
import org.bukkit.Particle

fun sendParticle(player: Player, particle: Particle, location: Location, count: Int) {
    val packet = PacketContainer(PacketType.Play.Server.WORLD_PARTICLES)
    
    // パーティクルタイプ
    packet.particles.write(0, particle)
    
    // 位置
    packet.doubles.write(0, location.x)
    packet.doubles.write(1, location.y)
    packet.doubles.write(2, location.z)
    
    // オフセット
    packet.float.read(0, 0.0f)
    packet.float.read(1, 0.0f)
    packet.float.read(2, 0.0f)
    
    // 速度
    packet.float.read(3, 0.0f)
    
    // カウント
    packet.integers.write(0, count)
    
    // ロング距離表示
    packet.booleans.write(0, true)
    
    protocolManager.sendServerPacket(player, packet)
}
```

### 7. タイトル関連パケット

#### Title（タイトル表示）
```kotlin
import net.kyori.adventure.title.Title

fun sendTitle(player: Player, title: String, subtitle: String = "", fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20) {
    // タイトルパケット
    val titlePacket = PacketContainer(PacketType.Play.Server.SET_TITLE_TEXT)
    titlePacket.chatComponents.write(0, Component.text(title))
    protocolManager.sendServerPacket(player, titlePacket)
    
    // サブタイトルパケット
    if (subtitle.isNotEmpty()) {
        val subtitlePacket = PacketContainer(PacketType.Play.Server.SET_SUBTITLE_TEXT)
        subtitlePacket.chatComponents.write(0, Component.text(subtitle))
        protocolManager.sendServerPacket(player, subtitlePacket)
    }
    
    // タイムパケット
    val timesPacket = PacketContainer(PacketType.Play.Server.SET_TITLES_ANIMATION)
    timesPacket.integers.write(0, fadeIn)
    timesPacket.integers.write(1, stay)
    timesPacket.integers.write(2, fadeOut)
    protocolManager.sendServerPacket(player, timesPacket)
}
```

#### ActionBar（アクションバー）
```kotlin
fun sendActionBar(player: Player, message: String) {
    val packet = PacketContainer(PacketType.Play.Server.SET_ACTION_BAR_TEXT)
    packet.chatComponents.write(0, Component.text(message))
    protocolManager.sendServerPacket(player, packet)
}
```

### 8. サウンド関連パケット

#### NamedSoundEffect（名前付きサウンドエフェクト）
```kotlin
import org.bukkit.Sound

fun sendSound(player: Player, sound: Sound, location: Location, volume: Float = 1.0f, pitch: Float = 1.0f) {
    val packet = PacketContainer(PacketType.Play.Server.NAMED_SOUND_EFFECT)
    
    // サウンド名
    packet.soundEffects.write(0, sound)
    
    // サウンドカテゴリ（MASTER, MUSIC, RECORDS, WEATHER, BLOCKS, HOSTILE, NEUTRAL, PLAYERS, AMBIENT, VOICE）
    packet.soundCategories.write(0, org.bukkit.SoundCategory.MASTER)
    
    // 位置（固定小数点）
    packet.integers.write(0, (location.x * 8).toInt())
    packet.integers.write(1, (location.y * 8).toInt())
    packet.integers.write(2, (location.z * 8).toInt())
    
    // 音量
    packet.float.read(0, volume)
    
    // ピッチ
    packet.float.read(1, pitch)
    
    protocolManager.sendServerPacket(player, packet)
}
```

### 9. スコアボード関連パケット

#### ScoreboardObjective（スコアボードオブジェクティブ）
```kotlin
fun sendScoreboardObjective(player: Player, name: String, displayName: String) {
    val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE)
    
    // オブジェクティブ名
    packet.strings.write(0, name)
    
    // アクション（0=作成, 1=削除, 2=更新）
    packet.integers.write(0, 0)
    
    // 表示名
    packet.chatComponents.write(0, Component.text(displayName))
    
    // 表示タイプ（INTEGER, HEARTS）
    packet.enumModifier.write(0, 
        com.comphenix.protocol.wrappers.EnumWrappers.ScoreboardAction.INTEGER
    )
    
    protocolManager.sendServerPacket(player, packet)
}
```

#### ScoreboardScore（スコアボードスコア）
```kotlin
fun sendScoreboardScore(player: Player, objectiveName: String, playerName: String, score: Int) {
    val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE)
    
    // エンティティ名
    packet.strings.write(0, playerName)
    
    // アクション（0=変更/設定, 1=削除）
    packet.integers.write(0, 0)
    
    // オブジェクティブ名
    packet.strings.write(1, objectiveName)
    
    // スコア値
    packet.integers.write(0, score)
    
    protocolManager.sendServerPacket(player, packet)
}
```

### 10. ボスバー関連パケット

#### Boss（ボスバー）
```kotlin
import java.util.UUID

fun sendBossBar(player: Player, uuid: UUID, title: String, health: Float) {
    val packet = PacketContainer(PacketType.Play.Server.BOSS)
    
    // UUID
    packet.uuids.write(0, uuid)
    
    // アクション（0=追加, 1=削除, 2=更新ヘルス, 3=更新タイトル, 4=更新スタイル, 5=更新フラグ）
    packet.integers.write(0, 0)
    
    // タイトル
    packet.chatComponents.write(0, Component.text(title))
    
    // ヘルス（0.0-1.0）
    packet.float.read(0, health)
    
    // カラー（PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE）
    packet.enumModifier.write(0, 
        com.comphenix.protocol.wrappers.WrappedBossBar.Color.BLUE
    )
    
    // スタイル（SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20）
    packet.enumModifier.write(1,
        com.comphenix.protocol.wrappers.WrappedBossBar.Style.PROGRESS
    )
    
    protocolManager.sendServerPacket(player, packet)
}
```

## 🔍 パケット受信（リスナー）

```kotlin
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.events.PacketListener

class PacketListenerExample : PacketListener {
    
    override fun onPacketSending(event: PacketEvent) {
        val packet = event.packet
        val player = event.player
        
        // サーバーからクライアントへのパケットをインターセプト
        when (packet.type) {
            PacketType.Play.Server.CHAT -> {
                // チャットパケットを処理
                val message = packet.chatComponents.read(0)
                // メッセージを変更したり、ログに記録したりできる
            }
            else -> {
                // その他のパケット
            }
        }
    }
    
    override fun onPacketReceiving(event: PacketEvent) {
        val packet = event.packet
        
        // クライアントからサーバーへのパケットをインターセプト
        when (packet.type) {
            PacketType.Play.Client.CHAT -> {
                // チャットメッセージを処理
                val message = packet.strings.read(0)
                // メッセージをフィルタリングしたり、検証したりできる
            }
            PacketType.Play.Client.USE_ENTITY -> {
                // エンティティ使用パケット
                val entityId = packet.integers.read(0)
                // エンティティ使用を制限したりできる
            }
            else -> {
                // その他のパケット
            }
        }
    }
    
    override fun getListening() = com.comphenix.protocol.events.ListenerPriority.NORMAL
    
    override fun getPlugin() = Main.plugin
}

// リスナーを登録
fun registerPacketListener() {
    protocolManager.addPacketListener(PacketListenerExample())
}
```

## 📝 パケットタイプ完全リスト（主要なもの）

### Play.Server（サーバー→クライアント）
- `SYSTEM_CHAT` - システムチャット
- `PLAYER_CHAT` - プレイヤーチャット
- `CHAT_COMMAND` - チャットコマンド
- `CHAT_PREVIEW` - チャットプレビュー
- `MAP` - 地図パケット
- `ENTITY_METADATA` - エンティティメタデータ
- `SPAWN_ENTITY` - エンティティ生成
- `SPAWN_ENTITY_LIVING` - リビングエンティティ生成
- `ENTITY_EQUIPMENT` - エンティティ装備
- `BLOCK_CHANGE` - ブロック変更
- `MULTI_BLOCK_CHANGE` - 複数ブロック変更
- `WORLD_PARTICLES` - ワールドパーティクル
- `SET_TITLE_TEXT` - タイトルテキスト
- `SET_SUBTITLE_TEXT` - サブタイトルテキスト
- `SET_TITLES_ANIMATION` - タイトルアニメーション
- `SET_ACTION_BAR_TEXT` - アクションバー
- `NAMED_SOUND_EFFECT` - 名前付きサウンドエフェクト
- `SCOREBOARD_OBJECTIVE` - スコアボードオブジェクティブ
- `SCOREBOARD_SCORE` - スコアボードスコア
- `BOSS` - ボスバー
- `PLAYER_INFO` - プレイヤー情報
- `GAME_STATE_CHANGE` - ゲーム状態変更
- `KEEP_ALIVE` - キープアライブ

### Play.Client（クライアント→サーバー）
- `CHAT` - チャット送信
- `CHAT_COMMAND` - チャットコマンド送信
- `USE_ENTITY` - エンティティ使用
- `PLAYER_POSITION` - プレイヤー位置
- `PLAYER_LOOK` - プレイヤー向き
- `PLAYER_POSITION_LOOK` - プレイヤー位置と向き
- `PLAYER_DIGGING` - ブロック破壊
- `USE_ITEM` - アイテム使用
- `KEEP_ALIVE` - キープアライブ応答

## ⚠️ 注意事項

1. **パケット構造の変更**: Minecraftのバージョンアップでパケット構造が変更されることがあります。ProtocolLibを使用することで、バージョン間の互換性がある程度保証されますが、完全ではありません。

2. **NMS（Net Minecraft Server）の使用**: 一部のパケット（特にチャンクデータなど）は、NMSクラスを直接使用する必要があります。これはバージョンに依存するため、注意が必要です。

3. **パフォーマンス**: 大量のパケットを送信する場合は、バッチ処理や非同期処理を検討してください。

4. **セキュリティ**: パケットをインターセプトして改変する場合は、セキュリティに注意してください。

## 📚 参考リンク

- [ProtocolLib GitHub](https://github.com/dmulloy2/ProtocolLib)
- [ProtocolLib Javadoc](https://ci.dmulloy2.net/job/ProtocolLib/javadoc/)
- [Minecraft Protocol Wiki (wiki.vg)](https://wiki.vg/Protocol)
- [Paper API Javadoc](https://papermc.io/javadocs/paper/1.21/)

## 🔄 既存実装の参考

プロジェクト内の `MapPacketSender.kt` を参考に、複雑なパケットの実装方法を確認できます。

```kotlin
// MapPacketSender.ktを参照
// - Optionalフィールドの扱い方
// - MapPatchクラスの作成方法
// - リフレクションを使用したNMSクラスの操作
// - エラーハンドリングとフォールバック処理
```

