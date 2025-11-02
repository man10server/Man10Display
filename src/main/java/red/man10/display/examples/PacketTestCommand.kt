package red.man10.display.examples

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.Location
import red.man10.display.examples.PacketExamples
// import java.util.UUID  // ボスバー用（現在未使用）

/**
 * パケット送信のテストコマンド
 * 
 * 使用方法:
 * /packettest <type> [args...]
 * 
 * タイプ:
 * - title: タイトルを表示
 * - actionbar: アクションバーを表示
 * - sound: サウンドを再生
 * - particle: パーティクルを表示
 * - bossbar: ボスバーを表示（現在無効）
 * - chat: システムチャットを送信
 * - gamemode: ゲームモード変更パケットを送信
 */
class PacketTestCommand : CommandExecutor {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます")
            return true
        }
        
        if (args.isEmpty()) {
            sender.sendMessage("使用方法: /packettest <type> [args...]")
            sender.sendMessage("タイプ: title, actionbar, sound, particle, chat, gamemode")
            return true
        }
        
        val type = args[0].lowercase()
        
        when (type) {
            "title" -> {
                val title = if (args.size > 1) args[1] else "タイトル"
                val subtitle = if (args.size > 2) args.slice(2 until args.size).joinToString(" ") else "サブタイトル"
                PacketExamples.sendTitle(sender, title, subtitle)
                sender.sendMessage("タイトルを表示しました: $title - $subtitle")
            }
            
            "actionbar" -> {
                val message = if (args.size > 1) args.slice(1 until args.size).joinToString(" ") else "アクションバー"
                PacketExamples.sendActionBar(sender, message)
                sender.sendMessage("アクションバーを表示しました: $message")
            }
            
            "sound" -> {
                val soundName = if (args.size > 1) args[1].uppercase() else "ENTITY_PLAYER_LEVELUP"
                val sound = try {
                    // Sound.valueOf()はdeprecatedだが、1.21.8ではまだ動作する
                    @Suppress("DEPRECATION")
                    Sound.valueOf(soundName)
                } catch (e: IllegalArgumentException) {
                    sender.sendMessage("無効なサウンド名: $soundName")
                    return true
                }
                PacketExamples.sendSound(sender, sound, sender.location)
                sender.sendMessage("サウンドを再生しました: $soundName")
            }
            
            "particle" -> {
                val particleName = if (args.size > 1) args[1].uppercase() else "HEART"
                val particle = try {
                    Particle.valueOf(particleName)
                } catch (e: IllegalArgumentException) {
                    sender.sendMessage("無効なパーティクル名: $particleName")
                    return true
                }
                val count = if (args.size > 2) args[2].toIntOrNull() ?: 10 else 10
                PacketExamples.sendParticle(sender, particle, sender.location, count)
                sender.sendMessage("パーティクルを表示しました: $particleName (数: $count)")
            }
            
            // "bossbar" -> {
            //     // ボスバーは複雑な実装が必要なため、現在は無効化されています
            //     sender.sendMessage("ボスバーは現在サポートされていません")
            // }
            
            "chat" -> {
                val message = if (args.size > 1) args.slice(1 until args.size).joinToString(" ") else "テストメッセージ"
                val overlay = args.contains("overlay")
                PacketExamples.sendSystemChat(sender, message, overlay)
                sender.sendMessage("システムチャットを送信しました: $message (overlay=$overlay)")
            }
            
            "gamemode" -> {
                val gamemode = if (args.size > 1) {
                    when (args[1].lowercase()) {
                        "survival", "0", "s" -> 0
                        "creative", "1", "c" -> 1
                        "adventure", "2", "a" -> 2
                        "spectator", "3", "sp" -> 3
                        else -> {
                            sender.sendMessage("無効なゲームモード: ${args[1]}")
                            return true
                        }
                    }
                } else {
                    1 // デフォルトはクリエイティブ
                }
                PacketExamples.setGamemode(sender, gamemode)
                val gamemodeName = when (gamemode) {
                    0 -> "サバイバル"
                    1 -> "クリエイティブ"
                    2 -> "アドベンチャー"
                    3 -> "スペクテイター"
                    else -> "不明"
                }
                sender.sendMessage("ゲームモード変更パケットを送信しました: $gamemodeName")
            }
            
            else -> {
                sender.sendMessage("無効なタイプ: $type")
                sender.sendMessage("タイプ: title, actionbar, sound, particle, chat, gamemode")
            }
        }
        
        return true
    }
}

