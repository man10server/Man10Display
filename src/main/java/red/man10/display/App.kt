package red.man10.display

import com.comphenix.protocol.events.PacketContainer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.map.MapPalette
import red.man10.extention.drawImageFully
import java.awt.image.BufferedImage

class App(val mapId: Int, val player: Player, val key: String) : Display() {

    //  var globalMapId: Int = -1
    var localMapId: Int = -1

    init {
        this.width = 1
        this.height = 1
        super.init()
    }


    companion object;

    override fun getTargetPlayers(): List<Player> {
        return listOf(player)
    }


    override fun getMessagePlayers(): List<Player> {
        val players = mutableListOf<Player>()
        this.playersCount = Bukkit.getOnlinePlayers().size
        for (p in Bukkit.getOnlinePlayers()) {
            if (this.message_distance > 0.0) {
                if (player.location.world != p.world)
                    continue
                if (p.location.distance(player.location) > message_distance)
                    continue
            }
            if (p.isOnline) {
                players.add(p)
            }
        }
        return players
    }

    override fun getSoundPlayers(): List<Player> {
        val players = mutableListOf<Player>()
        this.playersCount = Bukkit.getOnlinePlayers().size
        for (p in Bukkit.getOnlinePlayers()) {
            if (this.message_distance > 0.0) {
                if (player.location.world != p.world)
                    continue
                if (p.location.distance(player.location) > sound_distance)
                    continue
            }
            if (p.isOnline) {
                players.add(p)
            }
        }
        return players
    }


    override fun createPacketCache(image: BufferedImage, key: String, send: Boolean) {
        try {
            info("App.createPacketCache START: mapId=$mapId, key=$key, send=$send")
            val packets = mutableListOf<PacketContainer>()
            info("App.createPacketCache: converting image to bytes...")
            val bytes = MapPalette.imageToBytes(image)
            info("App.createPacketCache: image converted, bytes.size=${bytes.size}")
            
            info("App.createPacketCache: creating MAP packet...")
            val packet = MapPacketSender.createMapPacket(mapId, bytes)
            info("App.createPacketCache: MAP packet created successfully")
            packets.add(packet)

            packetCache[key] = packets
            info("App.createPacketCache: packet cache stored")

            if (send) {
                info("App.createPacketCache: sending map cache...")
                sendMapCache(getTargetPlayers(), key)
                info("App.createPacketCache: map cache sent")
            }
            info("App.createPacketCache END: completed successfully")
        } catch (e: Exception) {
            error("App.createPacketCache CRASH: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private var thread: Thread? = null
    private var threadExit = false
    fun startImageTask(imagePath: String, player: Player) {
        try {
            info("App.startImageTask START: imagePath=$imagePath, player=${player.name}")
            this.thread = Thread(Runnable {
                try {
                    info("App.startImageTask: thread started")
                    while (!this.threadExit) {
                        threadExit = true
                        info("App.startImageTask: checking cache for $imagePath...")

                        val cache = packetCache[imagePath]
                        if (cache != null) {
                            info("App.startImageTask: cache found, sending...")
                            sendMapCache(getTargetPlayers(), imagePath)
                            info("App.startImageTask: cache sent")
                            return@Runnable
                        }

                        info("App.startImageTask: cache not found, loading image...")
                        val image = ImageLoader.get(imagePath)
                        if (image != null) {
                            info("App.startImageTask: image loaded, drawing...")
                            this.currentImage?.drawImageFully(image)
                            info("App.startImageTask: image drawn, creating packet cache...")
                            this.createPacketCache(this.currentImage!!, imagePath, true)
                            info("App.startImageTask: packet cache created")
                        } else {
                            info("App.startImageTask: image not found, sending blank")
                            this.sendMapCache(getTargetPlayers(), "blank")
                        }
                    }
                    info("App.startImageTask: thread exit")
                } catch (e: Exception) {
                    error("App.startImageTask THREAD CRASH: ${e.javaClass.simpleName} - ${e.message}")
                    e.printStackTrace()
                }
            })
            info("App.startImageTask: starting thread...")
            this.thread!!.start()
            info("App.startImageTask END: thread started successfully")
        } catch (e: Exception) {
            error("App.startImageTask CRASH: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override fun deinit() {
        try {
            info("App.deinit() START")
            this.macroEngine.stop()
            info("App.deinit() macroEngine.stop() completed")
            this.clearCache()
            info("App.deinit() clearCache() completed")
            info("App.deinit() END: completed successfully")
        } catch (e: Exception) {
            error("App.deinit() CRASH: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}