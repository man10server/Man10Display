package red.man10.display

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import org.bukkit.command.CommandSender
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import red.man10.display.commands.Man10DisplayCommand
import red.man10.display.itemframe.IFPListener
import red.man10.display.itemframe.ItemFrameListener


class Main : JavaPlugin(), Listener {
    companion object {
        val version = "2023/7/22"
        var commandSender: CommandSender? = null
        val prefix = "[Man10Display] "
        lateinit var plugin: JavaPlugin
        lateinit var displayManager: DisplayManager
        lateinit var appManager: AppManager
        lateinit var imageManager: ImageManager
        lateinit var protocolManager: ProtocolManager
        lateinit var commandRouter: Man10DisplayCommand
        lateinit var settings: ConfigData
    }

    override fun onEnable() {
        plugin = this
        settings = ConfigData()
        settings.load(this, config)
        imageManager = ImageManager(settings.imagePath)
        
        // ProtocolLibが利用可能か確認（依存関係で保証されているが、念のためチェック）
        val protocolLibPlugin = server.pluginManager.getPlugin("ProtocolLib")
        if (protocolLibPlugin == null || !protocolLibPlugin.isEnabled) {
            server.logger.severe("${prefix}ProtocolLibが見つかりませんまたは無効です。プラグインを無効化します。")
            server.pluginManager.disablePlugin(this)
            return
        }
        
        // 次のtick以降でProtocolLib初期化を試みる（最大50tick）
        object : BukkitRunnable() {
            var attempts = 0
            override fun run() {
                try {
                    val pm = ProtocolLibrary.getProtocolManager()
                    protocolManager = pm

                    // 以降の初期化
                    displayManager = DisplayManager()

                    commandRouter = Man10DisplayCommand()
                    getCommand("mdisplay")!!.setExecutor(commandRouter)
                    getCommand("mdisplay")!!.tabCompleter = commandRouter
                    getCommand("md")!!.setExecutor(commandRouter)
                    getCommand("md")!!.tabCompleter = commandRouter

                    // 額縁保護用のイベント
                    server.pluginManager.registerEvents(ItemFrameListener(), this@Main)
                    if (server.pluginManager.getPlugin("ItemFrameProtector") != null) {
                        server.pluginManager.registerEvents(IFPListener(), this@Main)
                    }

                    appManager = AppManager(this@Main)

                    info("Man10 Display Plugin Enabled")
                    cancel()
                } catch (e: Exception) {
                    attempts++
                    if (attempts >= 200) { // 最大約10秒待機
                        server.logger.severe("${prefix}ProtocolLibのProtocolManagerが初期化できませんでした（タイムアウト）。プラグインを無効化します。")
                        server.pluginManager.disablePlugin(this@Main)
                        cancel()
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L)
    }

    override fun onDisable() {
        // 初期化されている場合のみクリーンアップ
        try {
            displayManager.deinit()
        } catch (e: UninitializedPropertyAccessException) {
            // 初期化されていない場合は何もしない
        }
        info("Disabled Man10 Display Plugin")
    }
}
