# Minecraft 1.21.8 パケット情報調査結果サマリー

## 📋 調査概要

Minecraft 1.21.8（Paper API）におけるパケット情報を徹底的に調査し、ProtocolLibを使用した実装例を作成しました。

## 📁 作成ファイル一覧

### 1. MINECRAFT_PACKET_EXAMPLES.md
   - パケット情報の詳細ドキュメント
   - 各パケットタイプの説明とサンプルコード
   - 主要なパケットの実装例

### 2. PacketExamples.kt
   - 実用的なパケット送信メソッドの実装クラス
   - チャット、タイトル、サウンド、パーティクル、ボスバーなど
   - すぐに使用できる実装済みメソッド

### 3. PacketTestCommand.kt
   - パケット送信をテストするためのコマンドクラス
   - `/packettest`コマンドで各種パケットをテスト可能

### 4. PacketTypeReference.kt
   - ProtocolLibで使用可能な全パケットタイプのリファレンス
   - パケットタイプを分類して整理

## 🎯 主要な発見事項

### ProtocolLibの使用
- ProtocolLib 5.0.0を使用してパケットを送信
- `PacketContainer`クラスでパケットを作成
- `ProtocolManager.sendServerPacket()`で送信

### パケットの構造
- 各パケットは複数のフィールドを持つ
- `integers`, `strings`, `doubles`, `booleans`などの型別アクセサを使用
- 一部のパケットはNMS（Net Minecraft Server）クラスを直接使用する必要がある

### 既存実装の参考
- `MapPacketSender.kt`でMAPパケットの実装例を確認
- Optionalフィールドの扱い方
- リフレクションを使用したNMSクラスの操作
- エラーハンドリングとフォールバック処理

## 📚 主要なパケットタイプ

### サーバー→クライアント（Play.Server）
- **チャット**: SYSTEM_CHAT, PLAYER_CHAT
- **タイトル**: SET_TITLE_TEXT, SET_SUBTITLE_TEXT, SET_ACTION_BAR_TEXT
- **エンティティ**: ENTITY_METADATA, SPAWN_ENTITY_LIVING, ENTITY_EQUIPMENT
- **ブロック**: BLOCK_CHANGE, MULTI_BLOCK_CHANGE
- **ワールド**: MAP, WORLD_PARTICLES, EXPLOSION
- **プレイヤー**: PLAYER_INFO, ABILITIES
- **サウンド**: NAMED_SOUND_EFFECT, STOP_SOUND
- **スコアボード**: SCOREBOARD_OBJECTIVE, SCOREBOARD_SCORE
- **ボスバー**: BOSS
- **ウィンドウ**: OPEN_WINDOW, CLOSE_WINDOW, SET_SLOT

### クライアント→サーバー（Play.Client）
- **チャット**: CHAT, CHAT_COMMAND
- **プレイヤー**: PLAYER_POSITION, PLAYER_LOOK, PLAYER_DIGGING
- **エンティティ**: USE_ENTITY
- **アイテム**: USE_ITEM, SET_CREATIVE_SLOT
- **ウィンドウ**: CLOSE_WINDOW, CLICK_WINDOW

## 💡 使用例

### 基本的なパケット送信
```kotlin
import red.man10.display.examples.PacketExamples

// タイトルを表示
PacketExamples.sendTitle(player, "タイトル", "サブタイトル")

// アクションバーを表示
PacketExamples.sendActionBar(player, "メッセージ")

// サウンドを再生
PacketExamples.sendSound(player, Sound.ENTITY_PLAYER_LEVELUP, player.location)

// パーティクルを表示
PacketExamples.sendParticle(player, Particle.HEART, player.location, 10)
```

### パケットリスナーの実装
```kotlin
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.events.PacketListener

class MyPacketListener : PacketListener {
    override fun onPacketSending(event: PacketEvent) {
        // サーバー→クライアントパケットをインターセプト
    }
    
    override fun onPacketReceiving(event: PacketEvent) {
        // クライアント→サーバーパケットをインターセプト
    }
    
    override fun getListening() = ListenerPriority.NORMAL
    override fun getPlugin() = Main.plugin
}

// 登録
Main.protocolManager.addPacketListener(MyPacketListener())
```

## ⚠️ 注意事項

1. **バージョン互換性**: Minecraftのバージョンアップでパケット構造が変更される可能性があります
2. **NMSクラス**: 一部のパケット（チャンクデータなど）はNMSクラスを直接使用する必要があります
3. **パフォーマンス**: 大量のパケットを送信する場合は、バッチ処理や非同期処理を検討してください
4. **セキュリティ**: パケットをインターセプトして改変する場合は、セキュリティに注意してください

## 🔗 参考リンク

- [ProtocolLib GitHub](https://github.com/dmulloy2/ProtocolLib)
- [ProtocolLib Javadoc](https://ci.dmulloy2.net/job/ProtocolLib/javadoc/)
- [Minecraft Protocol Wiki (wiki.vg)](https://wiki.vg/Protocol)
- [Paper API Javadoc](https://papermc.io/javadocs/paper/1.21/)

## 📝 今後の拡張

以下のパケットタイプの実装例を追加できます：
- エンティティのアニメーション
- インベントリ操作
- エフェクト
- カスタムペイロード
- その他の高度なパケット操作

## ✨ まとめ

Minecraft 1.21.8におけるパケット情報の調査が完了し、実用的なサンプルコードを作成しました。これらのコードを参考に、独自のパケット処理機能を実装できます。

