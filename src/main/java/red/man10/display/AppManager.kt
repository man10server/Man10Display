package red.man10.display

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.*
import org.bukkit.event.server.MapInitializeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapView
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import red.man10.extention.get
import red.man10.extention.getMapId
import red.man10.extention.setMapId
import java.awt.Color
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

const val APP_PLAYER_MAP_MAX = 256


class AppPlayerData {
    var mapId: Int? = null
    var app: App? = null

    var appThread: Thread? = null

    var lastLocation: Location? = null
    var rightButtonPressed = false
    var isSneaking = false

    // var leftClick
    var rightClickDown = false
    var lastRightClickTime: Long = 0
    var lastLeftClickTime: Long = 0
    var lastFocusX: Int = -1
    var lastFocusY: Int = -1


    var penWidth: Int = 1
    var penColor: Color = Color.RED
    var hasPen: Boolean = false

    var focusingMapId: Int = -1
    var focusingImageX: Int = -1
    var focusingImageY: Int = -1
    var lastFocusingImageX: Int = -1
    var lastFocusingImageY: Int = -1


    fun stop() {
        try {
            info("AppPlayerData.stop() START")
            app?.stop()
            info("AppPlayerData.stop() app?.stop() completed")
            app = null
            info("AppPlayerData.stop() END: completed successfully")
        } catch (e: Exception) {
            error("AppPlayerData.stop() CRASH: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}

class AppManager(var plugin: JavaPlugin) : Listener {

    private val playerData = ConcurrentHashMap<UUID, AppPlayerData>()
    private var playerDataThread: Thread? = null

    var mapIds = mutableListOf<Int>()

    // 利用中のMapIdのリスト
    private fun getUsingMapIds(): List<Int> {
        val list = mutableListOf<Int>()
        playerData.forEach { (uuid, data) ->
            if (data.mapId != null)
                list.add(data.mapId!!)
        }
        return list
    }

    fun getMapId(player: Player): Int? {
        return playerData[player.uniqueId]?.mapId
    }

    fun getFreeMapId(): Int? {
        val usingIds = getUsingMapIds()
        // mapIdsから利用中のMapIdを除外したリストを作成
        val freeIds = mapIds.filter { !usingIds.contains(it) }
        if (freeIds.isEmpty()) {
            return null
        }
        return freeIds[0]
    }

    fun save(p: CommandSender? = null): Boolean {
        val file = File(Main.plugin.dataFolder, File.separator + "apps.yml")
        val config = YamlConfiguration.loadConfiguration(file)
        try {
            config.set("mapIds", mapIds)
            config.save(file)
        } catch (e: Exception) {
            error(e.message!!, p)
            return false
        }
        return true
    }

    fun isAppMapId(mapId: Int): Boolean {
        return mapIds.contains(mapId)
    }

    // inventoryの中の地図のIDをすべて書き換える
    private fun updateInventoryMap(player: Player, mapId: Int) {
        try {
            info("updateInventoryMap START: player=${player.name}, mapId=$mapId")
            val inventory = player.inventory
            info("updateInventoryMap: inventory obtained, size=${inventory.size}")
            
            for (i in 0 until inventory.size) {
                try {
                    val item = inventory.getItem(i)
                    if (item != null && item.type == Material.FILLED_MAP) {
                        info("updateInventoryMap: found FILLED_MAP at slot $i")
                        updateMapId(item, mapId)
                        info("updateInventoryMap: mapId updated for slot $i")
                    }
                } catch (e: Exception) {
                    error("updateInventoryMap: error updating slot $i: ${e.javaClass.simpleName}")
                }
            }
            info("updateInventoryMap END: completed successfully")
        } catch (e: Exception) {
            error("updateInventoryMap CRASH: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun updateMapId(item: ItemStack, mapId: Int): Boolean {
        try {
            if (item.type != Material.FILLED_MAP) {
                return false
            }
            val meta = item.itemMeta
            val pd = meta?.persistentDataContainer ?: return false
            // key指定がないものは無視
            val key = getAppKey(item) ?: return false
            item.setMapId(mapId)
            return true
        } catch (e: Exception) {
            error("updateMapId CRASH: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    fun getAppKey(item: ItemStack): String? {
        val meta = item.itemMeta
        val pd = meta?.persistentDataContainer ?: return null
        // key指定がないものは無視
        return pd.get<String?>(Main.plugin, "man10display.app.key", PersistentDataType.STRING)
    }

    fun getAppImage(item: ItemStack): String? {
        val meta = item.itemMeta
        val pd = meta?.persistentDataContainer ?: return null
        // key指定がないものは無視
        return pd.get<String?>(Main.plugin, "man10display.app.image", PersistentDataType.STRING)
    }

    fun getAppMacro(item: ItemStack): String? {
        val meta = item.itemMeta
        val pd = meta?.persistentDataContainer ?: return null
        // key指定がないものは無視
        return pd.get<String?>(Main.plugin, "man10display.app.macro", PersistentDataType.STRING)
    }

    fun load(p: CommandSender? = null): Boolean {
        val file = File(Main.plugin.dataFolder, File.separator + "apps.yml")
        val config = YamlConfiguration.loadConfiguration(file)
        deinit()
        try {
            mapIds = config.getIntegerList("mapIds").toMutableList()
        } catch (e: Exception) {
            error(e.message!!, p)
            return false
        }

        return true
    }

    fun initMapIds() {
        load(null)
        if (mapIds.isNotEmpty()) {
            return
        }

        val mapIds = mutableListOf<Int>()
        for (i in 0..APP_PLAYER_MAP_MAX) {
            mapIds.add(Display.createMapId())
        }
        this.mapIds = mapIds
        save(null)
    }

    init {
        Bukkit.getServer().pluginManager.registerEvents(this, Main.plugin)

        initMapIds()


        playerDataThread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    playerDataTask()
                    Thread.sleep(PLAYER_DATA_THREAD_INTERVAL)
                } catch (e: InterruptedException) {
                    //error(e.localizedMessage)
                    //Thread.currentThread().interrupt()
                }
            }
        }.apply(Thread::start)
    }

    fun deinit() {
        playerDataThread?.interrupt()

    }

    private fun playerDataTask() {
        for (player in Bukkit.getOnlinePlayers()) {
            val uuid = player.uniqueId
            if (!playerData.containsKey(uuid)) {
                playerData[uuid] = AppPlayerData()
            }

            val data = playerData[uuid]!!

            // 右ボタンアップを検出
            val delta = System.currentTimeMillis() - data.lastRightClickTime
            if (delta >= RIGHT_BUTTON_UP_DETECTION_INTERVAL) {
                if (data.rightButtonPressed) {
                    //info("$delta ms", player)
                    onRightButtonUp(player)
                    data.rightButtonPressed = false
                }
            }
        }

    }
    // region event handlers

    // ログインイベント
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        try {
            info("onPlayerJoin START: player=${e.player.name}")
            val player = e.player
            playerData[player.uniqueId] = AppPlayerData()
            info("onPlayerJoin: AppPlayerData created")
            var mapId = getFreeMapId()
            playerData[player.uniqueId]?.mapId = mapId
            info("${player.name} got mapId: $mapId ")

            // 3秒後にプレイヤーのインベントリの地図のIDを書き換える
            info("onPlayerJoin: scheduling task for 3 seconds later...")
            Bukkit.getScheduler().runTaskLater(Main.plugin, Runnable {
                try {
                    info("onPlayerJoin scheduled task START: player=${player.name}")
                    updateInventoryMap(player, mapId!!)
                    info("onPlayerJoin scheduled task: updateInventoryMap completed")
                    
                    var item = player.inventory.itemInMainHand
                    info("onPlayerJoin scheduled task: itemInMainHand=${item?.type}, calling startMapItemTask...")
                    startMapItemTask(player, item)
                    info("onPlayerJoin scheduled task END: completed successfully")
                } catch (e: Exception) {
                    error("onPlayerJoin scheduled task CRASH: ${e.javaClass.simpleName} - ${e.message}")
                    e.printStackTrace()
                }
            }, 20L * 3)
            info("onPlayerJoin END: scheduled task registered")
        } catch (e: Exception) {
            error("onPlayerJoin CRASH: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        try {
            val player = e.player
            info("onPlayerQuit START: player=${player.name}")
            val data = playerData[player.uniqueId]
            if (data == null) {
                info("onPlayerQuit: playerData not found for ${player.name}")
                return
            }
            info("onPlayerQuit: playerData found, stopping...")
            data.stop()
            info("onPlayerQuit: data.stop() completed")
            // プレイヤーデータを削除
            playerData.remove(player.uniqueId)
            info("onPlayerQuit END: playerData removed successfully")
        } catch (e: Exception) {
            error("onPlayerQuit CRASH: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onPlayerToggleSneak(e: PlayerToggleSneakEvent) {
        playerData[e.player.uniqueId]?.isSneaking = e.isSneaking
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val from: Location = event.from
        val to: Location = event.to
        if (from.yaw !== to.yaw || from.pitch !== to.pitch) {
            //player.sendMessage("向きが変わった")
            //    if(!playerData[player.uniqueId]?.rightButtonPressed!!)
            //         return
            //      onRightButtonEvent(player)
        }

    }

    @EventHandler
    fun onMapInitialize(event: MapInitializeEvent) {
        val mapView: MapView = event.map

        if (!mapIds.contains(mapView.id)) {
            return
        }
        info("[App]onMapInitialize ${mapView.id}")
        for (renderer in mapView.renderers) {
            mapView.removeRenderer(renderer)
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action: Action = event.action
        //info("onPlayerInteract ${action.name}",player)
        // プレイヤーが右クリック
        if (action === Action.RIGHT_CLICK_AIR || action === Action.RIGHT_CLICK_BLOCK) {
            onRightButtonEvent(player)
            // プレイヤーが左クリック
        } else if (action === Action.LEFT_CLICK_AIR || action === Action.LEFT_CLICK_BLOCK) {
            onLeftButtonEvent(player)
        }
    }


    // 近くで右クリックしたとき
    @EventHandler
    fun onPlayerInteractEntityEvent(e: PlayerInteractEntityEvent) {
        // info("onPlayerInteractEntityEvent ",e.player)
        val player = e.player

        // インタラクトしたエンティティがItemFrame（額縁）であるかチェック
        if (e.rightClicked is ItemFrame) {
            val itemFrame = e.rightClicked as ItemFrame
            val item = player.inventory.itemInMainHand
            if (item.type != Material.FILLED_MAP) {
                return
            }
            val mapId = item.getMapId() ?: return
            if (isAppMapId(mapId)) {
                e.player.sendMessage("§2§lThis item cannot be placed in the item frame.")
                e.isCancelled = true
            }
        }

        onRightButtonEvent(player)
    }


    fun startMapItemTask(player: Player, item: ItemStack) {
        try {
            info("startMapItemTask START: player=${player.name}, item=${item.type}")
            val data = playerData[player.uniqueId]
            if (data == null) {
                error("startMapItemTask: playerData not found for ${player.name}")
                return
            }
            info("startMapItemTask: playerData found, stopping...")
            data.stop()
            info("startMapItemTask: data.stop() completed")
            
            // 地図以外は無視
            if (item.type != Material.FILLED_MAP) {
                info("startMapItemTask: item is not FILLED_MAP (${item.type}), returning")
                return
            }
            info("startMapItemTask: item is FILLED_MAP, getting app key...")
            
            val key = getAppKey(item)
            if (key == null) {
                info("startMapItemTask: getAppKey returned null, returning")
                return
            }
            info("startMapItemTask: app key=$key")
            
            var mapId = data.mapId
            if (mapId == null) {
                error("startMapItemTask: cant get mapId ${player.name}")
                info("startMapItemTask: getting free mapId...")
                data.mapId = getFreeMapId()
                mapId = data.mapId
                info("startMapItemTask: got free mapId=$mapId")
            } else {
                info("startMapItemTask: using existing mapId=$mapId")
            }
            
            info("startMapItemTask: setting mapId to item...")
            item.setMapId(mapId!!)
            info("startMapItemTask: mapId set successfully")
            
            val image = getAppImage(item)
            if (image != null) {
                info("startMapItemTask: image found=$image, starting image task...")
                data.app = App(mapId, player, "image")
                info("startMapItemTask: App created, starting image task...")
                data.app!!.startImageTask(image, player)
                info("startMapItemTask: image task started")
            } else {
                info("startMapItemTask: no image found")
            }

            val macro = getAppMacro(item)
            if (macro != null) {
                info("startMapItemTask: macro found=$macro, running macro...")
                data.app = App(mapId, player, "macro")
                info("startMapItemTask: App created, running macro...")
                data.app!!.runMacro(macro)
                info("startMapItemTask: macro running")
            } else {
                info("startMapItemTask: no macro found")
            }
            
            info("startMapItemTask END: completed successfully")
        } catch (e: Exception) {
            error("startMapItemTask CRASH: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @EventHandler
    fun onItemHeld(e: PlayerItemHeldEvent) {
        try {
            info("onItemHeld START: player=${e.player.name}, newSlot=${e.newSlot}")
            val item = e.player.inventory.getItem(e.newSlot)
            if (item == null) {
                info("onItemHeld: item is null at slot ${e.newSlot}, returning")
                return
            }
            info("onItemHeld: item found, type=${item.type}, calling startMapItemTask...")
            startMapItemTask(e.player, item)
            info("onItemHeld END: completed successfully")
        } catch (e: Exception) {
            error("onItemHeld CRASH: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        // エンティティがプレイヤーであるかチェック
        if (event.entity is Player) {
            val player = event.entity as Player
            val itemEntity = event.item // 拾われたアイテムエンティティ
            // アイテムエンティティからItemStackを取得
            val itemStack: ItemStack = itemEntity.itemStack
            // mapId更新
            var mapId = getMapId(player)
            updateMapId(itemStack, mapId!!)
            info("onEntityPickupItem ${mapId}")
            // 1秒後に起動
            Bukkit.getScheduler().runTaskLater(Main.plugin, Runnable {
                var item = player.inventory.itemInMainHand
                startMapItemTask(player, item)
            }, 20L * 1)
            // startMapItemTask(player, player.inventory.itemInMainHand)
        }
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        //info("onInventoryClick",e.whoClicked)
    }

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        // info("onInventoryDrag",e.whoClicked)
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        //info("onInventoryClose",e.player)
    }

    @EventHandler
    fun onInventoryMoveItem(e: InventoryMoveItemEvent) {
        // info("onInventoryMoveItem")
    }


    @EventHandler
    fun onInventory(e: InventoryEvent) {
        //    info("onInventory",e.view.player)
    }
    // endregion

    // region Mouse Event
    fun onButtonClick(player: Player) {
        //   interactMap(player)
    }

    // 右クリックイベント
    fun onRightButtonEvent(player: Player) {
        onButtonClick(player)

        val lastClick = this.playerData[player.uniqueId]?.lastRightClickTime ?: 0
        val now = System.currentTimeMillis()

        this.playerData[player.uniqueId]?.lastRightClickTime = now

        if (this.playerData[player.uniqueId]?.rightButtonPressed == false) {
            this.playerData[player.uniqueId]?.rightButtonPressed = true
            onRightButtonDown(player)
        } else {
            onRightButtonMove(player)
        }
    }


    fun onRightButtonUp(player: Player) {
        //    info("onRightButtonUp", player)
        //    if (playerData[player.uniqueId]?.hasPen == false)
        //        return
    }


    fun onRightButtonDown(player: Player) {
        //       info("onRightButtonDown", player)
        //       if (playerData[player.uniqueId]?.hasPen == false)
        //           return

    }

    fun onRightButtonMove(player: Player) {

    }

    //  左クリックイベント
    fun onLeftButtonEvent(player: Player) {
        onButtonClick(player)

    }
    // endregion
}

