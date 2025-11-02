package red.man10.display.examples

import com.comphenix.protocol.PacketType

/**
 * ProtocolLibで使用可能なパケットタイプ一覧（Minecraft 1.21.8）
 * 
 * このファイルは、ProtocolLibで使用可能なすべてのパケットタイプを
 * 参照するためのリファレンスです。
 * 
 * 使用方法:
 * val packet = PacketContainer(PacketType.Play.Server.XXX)
 */
object PacketTypeReference {
    
    // ==========================================
    // Play.Server - サーバー→クライアントパケット
    // ==========================================
    
    /**
     * チャット関連
     */
    object Chat {
        val SYSTEM_CHAT = PacketType.Play.Server.SYSTEM_CHAT          // システムチャット
        // val PLAYER_CHAT = PacketType.Play.Server.PLAYER_CHAT          // プレイヤーチャット（存在しない可能性）
        // val CHAT_COMMAND = PacketType.Play.Server.CHAT_COMMAND        // チャットコマンド（存在しない可能性）
        val CHAT_PREVIEW = PacketType.Play.Server.CHAT_PREVIEW        // チャットプレビュー
    }
    
    /**
     * タイトル関連
     */
    object Title {
        val SET_TITLE_TEXT = PacketType.Play.Server.SET_TITLE_TEXT           // タイトルテキスト
        val SET_SUBTITLE_TEXT = PacketType.Play.Server.SET_SUBTITLE_TEXT       // サブタイトルテキスト
        val SET_TITLES_ANIMATION = PacketType.Play.Server.SET_TITLES_ANIMATION // タイトルアニメーション
        val SET_ACTION_BAR_TEXT = PacketType.Play.Server.SET_ACTION_BAR_TEXT   // アクションバー
    }
    
    /**
     * エンティティ関連
     */
    object Entity {
        val ENTITY_METADATA = PacketType.Play.Server.ENTITY_METADATA           // エンティティメタデータ
        val SPAWN_ENTITY = PacketType.Play.Server.SPAWN_ENTITY                 // エンティティ生成
        val SPAWN_ENTITY_LIVING = PacketType.Play.Server.SPAWN_ENTITY_LIVING   // リビングエンティティ生成
        val SPAWN_ENTITY_PAINTING = PacketType.Play.Server.SPAWN_ENTITY_PAINTING // 絵画生成
        val ENTITY_EQUIPMENT = PacketType.Play.Server.ENTITY_EQUIPMENT         // エンティティ装備
        val ENTITY_VELOCITY = PacketType.Play.Server.ENTITY_VELOCITY           // エンティティ速度
        val ENTITY_TELEPORT = PacketType.Play.Server.ENTITY_TELEPORT           // エンティティテレポート
        val ENTITY_HEAD_ROTATION = PacketType.Play.Server.ENTITY_HEAD_ROTATION // エンティティ頭部回転
        val ENTITY_STATUS = PacketType.Play.Server.ENTITY_STATUS                // エンティティステータス
        val ATTACH_ENTITY = PacketType.Play.Server.ATTACH_ENTITY                // エンティティアタッチ
        val ENTITY_DESTROY = PacketType.Play.Server.ENTITY_DESTROY             // エンティティ破壊
    }
    
    /**
     * ブロック関連
     */
    object Block {
        val BLOCK_CHANGE = PacketType.Play.Server.BLOCK_CHANGE                 // ブロック変更
        val MULTI_BLOCK_CHANGE = PacketType.Play.Server.MULTI_BLOCK_CHANGE      // 複数ブロック変更
        val BLOCK_ACTION = PacketType.Play.Server.BLOCK_ACTION                  // ブロックアクション
        val BLOCK_BREAK_ANIMATION = PacketType.Play.Server.BLOCK_BREAK_ANIMATION // ブロック破壊アニメーション
    }
    
    /**
     * ワールド関連
     */
    object World {
        val MAP = PacketType.Play.Server.MAP                                   // 地図パケット
        val MAP_CHUNK = PacketType.Play.Server.MAP_CHUNK                       // チャンクデータ
        val UNLOAD_CHUNK = PacketType.Play.Server.UNLOAD_CHUNK                 // チャンクアンロード
        val WORLD_PARTICLES = PacketType.Play.Server.WORLD_PARTICLES            // ワールドパーティクル
        val EXPLOSION = PacketType.Play.Server.EXPLOSION                        // 爆発
        val UPDATE_TIME = PacketType.Play.Server.UPDATE_TIME                    // 時間更新
        val GAME_STATE_CHANGE = PacketType.Play.Server.GAME_STATE_CHANGE       // ゲーム状態変更
        // val THUNDERBOLT = PacketType.Play.Server.THUNDERBOLT                   // 雷（存在しない可能性）
    }
    
    /**
     * プレイヤー関連
     */
    object Player {
        val PLAYER_INFO = PacketType.Play.Server.PLAYER_INFO                   // プレイヤー情報
        val PLAYER_INFO_REMOVE = PacketType.Play.Server.PLAYER_INFO_REMOVE      // プレイヤー情報削除
        val NAMED_ENTITY_SPAWN = PacketType.Play.Server.NAMED_ENTITY_SPAWN     // 名前付きエンティティ生成
        val ABILITIES = PacketType.Play.Server.ABILITIES                        // プレイヤー能力
        val HELD_ITEM_SLOT = PacketType.Play.Server.HELD_ITEM_SLOT             // 手持ちアイテムスロット
        val PLAYER_LIST_HEADER_FOOTER = PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER // プレイヤーリストヘッダー/フッター
    }
    
    /**
     * サウンド関連
     */
    object Sound {
        val NAMED_SOUND_EFFECT = PacketType.Play.Server.NAMED_SOUND_EFFECT     // 名前付きサウンドエフェクト
        val STOP_SOUND = PacketType.Play.Server.STOP_SOUND                     // サウンド停止
        // val SOUND_EFFECT = PacketType.Play.Server.SOUND_EFFECT                 // サウンドエフェクト（存在しない可能性）
    }
    
    /**
     * スコアボード関連
     */
    object Scoreboard {
        val SCOREBOARD_OBJECTIVE = PacketType.Play.Server.SCOREBOARD_OBJECTIVE // スコアボードオブジェクティブ
        val SCOREBOARD_SCORE = PacketType.Play.Server.SCOREBOARD_SCORE         // スコアボードスコア
        val SCOREBOARD_DISPLAY_OBJECTIVE = PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE // スコアボード表示
        // val TEAM = PacketType.Play.Server.TEAM                                  // チーム（存在しない可能性）
    }
    
    /**
     * ボスバー関連
     */
    object BossBar {
        val BOSS = PacketType.Play.Server.BOSS                                 // ボスバー
    }
    
    /**
     * ウィンドウ関連
     */
    object Window {
        val OPEN_WINDOW = PacketType.Play.Server.OPEN_WINDOW                    // ウィンドウを開く
        val CLOSE_WINDOW = PacketType.Play.Server.CLOSE_WINDOW                  // ウィンドウを閉じる
        val SET_SLOT = PacketType.Play.Server.SET_SLOT                          // スロット設定
        val WINDOW_ITEMS = PacketType.Play.Server.WINDOW_ITEMS                  // ウィンドウアイテム
        val WINDOW_DATA = PacketType.Play.Server.WINDOW_DATA                    // ウィンドウデータ
        val CRAFT_PROGRESS_BAR = PacketType.Play.Server.CRAFT_PROGRESS_BAR      // クラフト進捗バー
    }
    
    /**
     * その他
     */
    object Other {
        val KEEP_ALIVE = PacketType.Play.Server.KEEP_ALIVE                     // キープアライブ
        val RESOURCE_PACK_SEND = PacketType.Play.Server.RESOURCE_PACK_SEND     // リソースパック送信
        val CUSTOM_PAYLOAD = PacketType.Play.Server.CUSTOM_PAYLOAD              // カスタムペイロード
        val RESPAWN = PacketType.Play.Server.RESPAWN                            // リスポーン
        val STATISTIC = PacketType.Play.Server.STATISTIC                       // 統計
        val UPDATE_HEALTH = PacketType.Play.Server.UPDATE_HEALTH                // ヘルス更新
        val EXPERIENCE = PacketType.Play.Server.EXPERIENCE                     // 経験値
        val COMBAT_EVENT = PacketType.Play.Server.COMBAT_EVENT                  // コンバットイベント
        val CAMERA = PacketType.Play.Server.CAMERA                              // カメラ
        val WORLD_BORDER = PacketType.Play.Server.WORLD_BORDER                 // ワールドボーダー
        val TITLE = PacketType.Play.Server.TITLE                                // タイトル（旧形式）
        val TAB_COMPLETE = PacketType.Play.Server.TAB_COMPLETE                  // タブ補完
        // val DECLARE_COMMANDS = PacketType.Play.Server.DECLARE_COMMANDS          // コマンド宣言（存在しない可能性）
    }
    
    // ==========================================
    // Play.Client - クライアント→サーバーパケット
    // ==========================================
    
    /**
     * チャット関連
     */
    object ClientChat {
        val CHAT = PacketType.Play.Client.CHAT                                  // チャット送信
        val CHAT_COMMAND = PacketType.Play.Client.CHAT_COMMAND                  // チャットコマンド送信
        val CHAT_PREVIEW = PacketType.Play.Client.CHAT_PREVIEW                  // チャットプレビュー
    }
    
    /**
     * プレイヤー動作関連
     */
    object ClientPlayer {
        // これらのパケットタイプは存在しない可能性があります
        // val PLAYER_POSITION = PacketType.Play.Client.PLAYER_POSITION           // プレイヤー位置
        // val PLAYER_LOOK = PacketType.Play.Client.PLAYER_LOOK                    // プレイヤー向き
        // val PLAYER_POSITION_LOOK = PacketType.Play.Client.PLAYER_POSITION_LOOK // プレイヤー位置と向き
        // val PLAYER_DIGGING = PacketType.Play.Client.PLAYER_DIGGING            // ブロック破壊
        // val PLAYER_ABILITIES = PacketType.Play.Client.PLAYER_ABILITIES          // プレイヤー能力
        // val PLAYER_ACTION = PacketType.Play.Client.PLAYER_ACTION                // プレイヤーアクション
        // val PLAYER_COMMAND = PacketType.Play.Client.PLAYER_COMMAND               // プレイヤーコマンド
    }
    
    /**
     * エンティティ操作関連
     */
    object ClientEntity {
        val USE_ENTITY = PacketType.Play.Client.USE_ENTITY                      // エンティティ使用
        val ENTITY_ACTION = PacketType.Play.Client.ENTITY_ACTION                // エンティティアクション
    }
    
    /**
     * アイテム関連
     */
    object ClientItem {
        val USE_ITEM = PacketType.Play.Client.USE_ITEM                         // アイテム使用
        val SET_CREATIVE_SLOT = PacketType.Play.Client.SET_CREATIVE_SLOT       // クリエイティブスロット設定
        val HELD_ITEM_SLOT = PacketType.Play.Client.HELD_ITEM_SLOT             // 手持ちアイテムスロット
    }
    
    /**
     * ウィンドウ関連
     */
    object ClientWindow {
        val CLOSE_WINDOW = PacketType.Play.Client.CLOSE_WINDOW                 // ウィンドウを閉じる
        // val CLICK_WINDOW = PacketType.Play.Client.CLICK_WINDOW                  // ウィンドウクリック（存在しない可能性）
        // val TRADE_SELECT = PacketType.Play.Client.TRADE_SELECT                   // トレード選択（存在しない可能性）
    }
    
    /**
     * その他
     */
    object ClientOther {
        val KEEP_ALIVE = PacketType.Play.Client.KEEP_ALIVE                     // キープアライブ応答
        val CUSTOM_PAYLOAD = PacketType.Play.Client.CUSTOM_PAYLOAD              // カスタムペイロード
        val RESOURCE_PACK_STATUS = PacketType.Play.Client.RESOURCE_PACK_STATUS  // リソースパックステータス
        val TAB_COMPLETE = PacketType.Play.Client.TAB_COMPLETE                  // タブ補完
        val CLIENT_COMMAND = PacketType.Play.Client.CLIENT_COMMAND              // クライアントコマンド
    }
    
    // ==========================================
    // ステータスパケット
    // ==========================================
    
    object Status {
        object Server {
            val SERVER_INFO = PacketType.Status.Server.SERVER_INFO
            val PONG = PacketType.Status.Server.PONG
        }

        object Client {
            val START = PacketType.Status.Client.START
            val PING = PacketType.Status.Client.PING
        }
    }
    
    // ==========================================
    // ログインパケット
    // ==========================================
    
    object Login {
        object Server {
            val DISCONNECT = PacketType.Login.Server.DISCONNECT
            val ENCRYPTION_BEGIN = PacketType.Login.Server.ENCRYPTION_BEGIN
            val SUCCESS = PacketType.Login.Server.SUCCESS
            val SET_COMPRESSION = PacketType.Login.Server.SET_COMPRESSION
            val CUSTOM_PAYLOAD = PacketType.Login.Server.CUSTOM_PAYLOAD
        }

        object Client {
            val START = PacketType.Login.Client.START
            val ENCRYPTION_BEGIN = PacketType.Login.Client.ENCRYPTION_BEGIN
            val CUSTOM_PAYLOAD = PacketType.Login.Client.CUSTOM_PAYLOAD
        }
    }
    
    // ==========================================
    // ハンドシェイクパケット
    // ==========================================
    
    object Handshake {
        object Client {
            val SET_PROTOCOL = PacketType.Handshake.Client.SET_PROTOCOL
        }
    }
}
