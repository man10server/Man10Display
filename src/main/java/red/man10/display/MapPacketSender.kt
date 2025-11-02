package red.man10.display

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.reflect.StructureModifier
import org.bukkit.entity.Player
import red.man10.display.util.ProtocolLibHelpers
import java.lang.reflect.Constructor
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Collection
import java.util.Optional

interface MapPacketSender {
    companion object {
        private const val PATCH_WIDTH = MC_MAP_SIZE_X
        private const val PATCH_HEIGHT = MC_MAP_SIZE_Y

        @Volatile
        private var structureLogged = false

        private val mapIdClass: Class<*>? by lazy {
            runCatching { Class.forName("net.minecraft.world.level.saveddata.maps.MapId") }.getOrNull()
        }

        private val mapIdConstructor: Constructor<*>? by lazy {
            mapIdClass?.declaredConstructors
                ?.firstOrNull { ctor ->
                    val params = ctor.parameterTypes
                    params.size == 1 && (params[0] == Int::class.javaPrimitiveType || params[0] == Int::class.java)
                }
                ?.apply { isAccessible = true }
        }

        private val mapPatchClass: Class<*>? by lazy {
            listOf(
                "net.minecraft.world.level.saveddata.maps.MapItemSavedData\$MapPatch",
                "net.minecraft.network.protocol.game.ClientboundMapItemDataPacket\$a"
            ).asSequence()
                .mapNotNull { runCatching { Class.forName(it) }.getOrNull() }
                .firstOrNull()
        }

        private val mapPatchConstructor: Constructor<*>? by lazy {
            mapPatchClass?.declaredConstructors
                ?.firstOrNull { ctor ->
                    val params = ctor.parameterTypes
                    params.size == 5 &&
                        params[0] == Int::class.javaPrimitiveType &&
                        params[1] == Int::class.javaPrimitiveType &&
                        params[2] == Int::class.javaPrimitiveType &&
                        params[3] == Int::class.javaPrimitiveType &&
                        params[4] == ByteArray::class.java
                }
                ?.apply { isAccessible = true }
        }

        private val mapDecorationClass: Class<*>? by lazy {
            runCatching { Class.forName("net.minecraft.world.level.saveddata.maps.MapDecoration") }.getOrNull()
        }

        private var lastLogTime: Long = 0
        private var lastPacketCount: Long = 0
        private val logInterval: Long = 5000 // 5秒ごとにログ出力
        private var totalPacketsSent: Long = 0
        private var totalPacketsErrors: Long = 0
        
        fun send(players: List<Player>, packets: List<PacketContainer>): Int {
            var sent = 0
            var errors = 0
            val startTime = System.currentTimeMillis()
            safeLog("INFO", "Sending ${packets.size} packets to ${players.size} players")
            
            // プレイヤーが参加完了しているか確認（参加直後の場合は少し待つ）
            val onlinePlayers = players.filter { player ->
                if (!player.isOnline) {
                    safeLog("WARN", "Player ${player.name} is not online, skipping")
                    false
                } else {
                    // プレイヤーが参加してから3秒以上経過しているか確認（1秒から3秒に延長）
                    val joinTime = player.firstPlayed
                    val currentTime = System.currentTimeMillis()
                    val timeSinceJoin = currentTime - joinTime
                    if (timeSinceJoin < 3000) {
                        safeLog("WARN", "Player ${player.name} joined recently (${timeSinceJoin}ms ago), skipping to avoid disconnect")
                        false
                    } else {
                        true
                    }
                }
            }
            
            if (onlinePlayers.isEmpty()) {
                safeLog("WARN", "No eligible players to send packets to")
                return 0
            }
            
            safeLog("INFO", "Sending packets to ${onlinePlayers.size} eligible players")
            
            // 一度に送信するパケット数を制限（クライアントの処理能力を考慮）
            val batchSize = 10 // 一度に10個のパケットを送信
            val delayBetweenBatches = 50L // バッチ間の遅延（ミリ秒）
            
            for (player in onlinePlayers) {
                safeLog("INFO", "Sending packets to player ${player.name} (${packets.size} packets)")
                var playerSent = 0
                var playerErrors = 0
                
                // パケットをバッチに分割して送信
                for (batchStart in packets.indices step batchSize) {
                    if (!player.isOnline) {
                        safeLog("WARN", "Player ${player.name} went offline during packet send (batch starting at $batchStart)")
                        break
                    }
                    
                    val batchEnd = minOf(batchStart + batchSize, packets.size)
                    val batch = packets.subList(batchStart, batchEnd)
                    
                    for ((index, packet) in batch.withIndex()) {
                        try {
                            if (!player.isOnline) {
                                safeLog("WARN", "Player ${player.name} went offline during packet send (packet ${batchStart + index})")
                                break
                            }
                            Main.protocolManager.sendServerPacket(player, packet)
                            playerSent++
                            sent++
                            totalPacketsSent++
                            
                            // パケット送信ログ（定期的に）
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastLogTime >= logInterval) {
                                val timeDiff = currentTime - lastLogTime
                                val packetDiff = totalPacketsSent - lastPacketCount
                                val pps = if (packetDiff > 0 && timeDiff > 0) {
                                    String.format("%.2f", packetDiff * 1000.0 / timeDiff)
                                } else {
                                    "0.00"
                                }
                                safeLog("INFO", "📤 パケット送信: 累計送信=$totalPacketsSent, 累計エラー=$totalPacketsErrors, 送信速度=$pps パケット/秒")
                                lastLogTime = currentTime
                                lastPacketCount = totalPacketsSent
                            }
                        } catch (ex: Exception) {
                            errors++
                            playerErrors++
                            totalPacketsErrors++
                            safeLog("ERROR", "Packet send failed to ${player.name} (packet ${batchStart + index}/${packets.size}): ${ex.javaClass.simpleName} - ${ex.message}")
                            if (errors <= 3) { // 最初の3エラーだけスタックトレース
                                ex.printStackTrace()
                            }
                            // プレイヤーが切断された場合は、以降のパケット送信をスキップ
                            if (ex.message?.contains("disconnect") == true || ex.message?.contains("closed") == true) {
                                safeLog("WARN", "Player ${player.name} disconnected during packet send, stopping")
                                break
                            }
                        }
                    }
                    
                    // バッチ間の遅延（最後のバッチ以外）
                    if (batchEnd < packets.size) {
                        try {
                            Thread.sleep(delayBetweenBatches)
                        } catch (_: InterruptedException) {
                            // 中断された場合は続行
                        }
                    }
                }
                safeLog("INFO", "Finished sending packets to player ${player.name}: sent=$playerSent/${packets.size}, errors=$playerErrors")
            }
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            safeLog("INFO", "📤 パケット送信完了: 送信=$sent, エラー=$errors, 所要時間=${duration}ms, 送信速度=${if (sent > 0 && duration > 0) String.format("%.2f", sent * 1000.0 / duration) else "0.00"} パケット/秒")
            safeLog("INFO", "Packet send complete: sent=$sent, errors=$errors")
            return sent
        }

        fun createMapPacket(mapId: Int, data: ByteArray?): PacketContainer {
            requireNotNull(data) { "data is null" }
            safeLog("INFO", "Creating MAP packet: mapId=$mapId, dataSize=${data.size}")

            val packet = PacketContainer(PacketType.Play.Server.MAP)
            // writeDefaults()は呼ばない（デフォルト値が間違っている可能性があるため）
            // packet.modifier.writeDefaults()

            val ints = packet.integers
            val bools = packet.booleans
            val bytes = packet.bytes
            val byteArrays = packet.byteArrays
            val modifier = packet.modifier

            if (!structureLogged) {
                logStructure(modifier, ints.size(), byteArrays.size(), bytes.size(), bools.size())
            }

            safeLog("INFO", "Writing mapId=$mapId...")
            val wroteMapId = writeMapId(packet, mapId, ints, modifier)
            if (wroteMapId) {
                safeLog("INFO", "mapId write: SUCCESS")
            } else {
                safeLog("ERROR", "mapId write: FAILED")
            }

            safeLog("INFO", "Writing MapPatch...")
            val wrotePatch = writeMapPatch(packet, data)
            // writeMapPatchFallbackは使わない（データが重複して書き込まれる可能性があるため）
            // || writeMapPatchFallback(packet, data)
            if (wrotePatch) {
                safeLog("INFO", "MapPatch write: SUCCESS")
            } else {
                safeLog("ERROR", "MapPatch write: FAILED")
            }

            writeScaleAndLock(bytes, bools)

            if (!wroteMapId) {
                safeLog("ERROR", "Failed to write mapId to MAP packet")
            }
            if (!wrotePatch) {
                safeLog("ERROR", "Failed to write map patch data to MAP packet")
            }

            safeLog("INFO", "MAP packet created: mapId=${if (wroteMapId) "OK" else "FAIL"}, patch=${if (wrotePatch) "OK" else "FAIL"}")
            return packet
        }

        private fun writeMapId(
            packet: PacketContainer,
            id: Int,
            ints: StructureModifier<Int>,
            modifier: StructureModifier<Any>
        ): Boolean {
            val ctor = mapIdConstructor
            if (ctor != null) {
                val instance = try {
                    ctor.newInstance(id)
                } catch (e: Exception) {
                    safeLog("WARN", "Failed to create MapId: ${e.javaClass.simpleName}")
                    null
                }
                if (instance != null) {
                    val total = modifier.size()
                    for (index in 0 until total) {
                        val field = try {
                            modifier.getField(index)
                        } catch (_: Exception) {
                            continue
                        }
                        if (field.type == mapIdClass) {
                            try {
                                modifier.write(index, instance)
                                safeLog("INFO", "MapId=$id written to field[$index]")
                                return true
                            } catch (e: Exception) {
                                safeLog("WARN", "MapId write failed at field[$index]: ${e.javaClass.simpleName}")
                            }
                        }
                    }
                }
            }

            if (ints.size() > 0) {
                try {
                    ints.write(0, id)
                    safeLog("INFO", "MapId=$id written via ints[0]")
                    return true
                } catch (e: Exception) {
                    safeLog("WARN", "MapId write via ints failed: ${e.javaClass.simpleName}")
                }
            }

            val total = modifier.size()
            for (index in 0 until total) {
                val field = try {
                    modifier.getField(index)
                } catch (_: Exception) {
                    continue
                }
                val type = field.type
                if (type == Int::class.javaPrimitiveType || type == Int::class.java || type == Integer::class.java) {
                    try {
                        modifier.write(index, id)
                        safeLog("INFO", "MapId=$id written to Int field[$index]")
                        return true
                    } catch (e: Exception) {
                        safeLog("WARN", "MapId write to Int field[$index] failed: ${e.javaClass.simpleName}")
                    }
                }
            }

            safeLog("ERROR", "MapId write failed: no suitable field found")
            return false
        }

        private fun writeMapPatch(packet: PacketContainer, data: ByteArray): Boolean {
            val patch = createMapPatch(0, 0, PATCH_WIDTH, PATCH_HEIGHT, data)
            if (patch == null) {
                safeLog("WARN", "MapPatch creation failed")
                return false
            }
            safeLog("INFO", "MapPatch created: ${patch.javaClass.simpleName}")
            val result = writeMapPatchToPacket(packet, patch)
            if (result) {
                safeLog("INFO", "MapPatch written to packet")
            } else {
                safeLog("WARN", "MapPatch write to packet failed")
            }
            return result
        }

        private fun writeMapPatchFallback(packet: PacketContainer, data: ByteArray): Boolean {
            val byteArrays = packet.byteArrays
            if (byteArrays.size() > 0) {
                try {
                    byteArrays.write(0, data)
                    return true
                } catch (_: Exception) {
                }
            }

            val modifier = packet.modifier
            val total = modifier.size()
            for (index in 0 until total) {
                val field = try {
                    modifier.getField(index)
                } catch (_: Exception) {
                    continue
                }
                if (field.type == ByteArray::class.java) {
                    try {
                        modifier.write(index, data)
                        return true
                    } catch (_: Exception) {
                    }
                }
            }
            return false
        }

         private fun writeMapPatchToPacket(packet: PacketContainer, patch: Any): Boolean {
             val modifier = packet.modifier
             val patchClass = mapPatchClass ?: patch.javaClass
             val decorationClass = mapDecorationClass
             var wrotePatch = false

             // Optionalフィールドを探す
             for (index in 0 until modifier.size()) {
                 val field = try {
                     modifier.getField(index)
                 } catch (_: Exception) {
                     continue
                 }

                 val fieldType = field.type
                 if (fieldType != Optional::class.java && fieldType.simpleName != "Optional") {
                     continue
                 }

                 val innerType = (field.genericType as? ParameterizedType)
                     ?.actualTypeArguments
                     ?.firstOrNull()
                 
                 // Optional内の値を確認
                 val optionalValue = try {
                     @Suppress("UNCHECKED_CAST")
                     modifier.read(index) as? Optional<*>
                 } catch (_: Exception) {
                     null
                 }
                 val current = optionalValue?.orElse(null)

                 // MapPatch用のOptional<MapPatch>フィールドか確認（優先）
                 if (!wrotePatch && (matchesType(innerType, patchClass) || (current != null && patchClass.isInstance(current)))) {
                     try {
                         @Suppress("UNCHECKED_CAST")
                         val optionalObj = Optional.of(patch) as Optional<Any?>
                         modifier.write(index, optionalObj)
                         safeLog("INFO", "MapPatch written to Optional<MapPatch> field[$index]")
                         wrotePatch = true
                         continue
                     } catch (e: Exception) {
                         safeLog("WARN", "MapPatch write to Optional<MapPatch> field[$index] failed: ${e.javaClass.simpleName} - ${e.message}")
                     }
                 }

                 // MapPatch用のOptional<Collection<MapPatch>>フィールドか確認（フォールバック）
                 if (!wrotePatch && matchesCollectionOf(innerType, patchClass)) {
                     try {
                         // Collection<MapPatch>を作成してOptionalでラップ
                         val collection = java.util.ArrayList<Any>()
                         collection.add(patch)
                         ProtocolLibHelpers.writeOptionalCollectionToField(packet, index, collection)
                         safeLog("INFO", "MapPatch written to Optional<Collection<MapPatch>> field[$index] (fallback)")
                         wrotePatch = true
                         continue
                     } catch (e: Exception) {
                         safeLog("WARN", "MapPatch write to Optional<Collection<MapPatch>> field[$index] failed: ${e.javaClass.simpleName} - ${e.message}")
                     }
                 }

                 // MapDecoration用のOptionalフィールドを空にする
                 if (matchesCollectionOf(innerType, decorationClass) ||
                     (current is Collection<*> && decorationClass != null && current.any { decorationClass.isInstance(it) })
                 ) {
                     try {
                         @Suppress("UNCHECKED_CAST")
                         val emptyOptional = Optional.empty<Any?>() as Optional<Any?>
                         modifier.write(index, emptyOptional)
                         safeLog("INFO", "MapDecoration Optional field[$index] set to empty")
                     } catch (_: Exception) {
                     }
                 }
             }

             if (wrotePatch) return true

             return false
         }

        private fun writeScaleAndLock(bytes: StructureModifier<Byte>, bools: StructureModifier<Boolean>) {
            if (bytes.size() > 0) {
                runCatching { bytes.write(0, 0.toByte()) }
            }
            if (bools.size() > 0) {
                runCatching { bools.write(0, false) }
            }
        }

        private fun createMapPatch(
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            data: ByteArray
        ): Any? = mapPatchConstructor?.let { ctor ->
            runCatching { ctor.newInstance(x, y, width, height, data) }.getOrNull()
        }

        private fun logStructure(
            modifier: StructureModifier<Any>,
            ints: Int,
            byteArrays: Int,
            bytes: Int,
            bools: Int
        ) {
            if (structureLogged) return
            structureLogged = true
            val total = modifier.size()
            safeLog("INFO", "MAP packet structure: ints=$ints, byteArrays=$byteArrays, bytes=$bytes, bools=$bools, totalFields=$total")
            
            // 各フィールドの型情報をログ出力（安全に）
            for (i in 0 until total.coerceAtMost(10)) { // 最大10フィールドまで
                try {
                    val field = modifier.getField(i)
                    val typeName = field.type.simpleName
                    safeLog("INFO", "  Field[$i]: $typeName")
                } catch (_: Exception) {
                    // スキップ
                }
            }
        }
        
        private fun safeLog(level: String, message: String) {
            try {
                when (level) {
                    "INFO" -> Main.plugin.logger.info("[Man10Display] $message")
                    "WARN" -> Main.plugin.logger.warning("[Man10Display] $message")
                    "ERROR" -> Main.plugin.logger.severe("[Man10Display] $message")
                    else -> Main.plugin.logger.info("[Man10Display] $message")
                }
            } catch (e: Exception) {
                // ログ出力自体が失敗してもクラッシュしないように
                System.err.println("[Man10Display] $message")
            }
        }

        private fun matchesType(type: Type?, target: Class<*>?): Boolean {
            if (type == null || target == null) return false
            return when (type) {
                is Class<*> -> type == target
                is ParameterizedType -> matchesType(type.rawType, target) || type.actualTypeArguments.any { matchesType(it, target) }
                else -> false
            }
        }

        private fun matchesCollectionOf(type: Type?, element: Class<*>?): Boolean {
            if (type == null || element == null) return false
            return when (type) {
                is ParameterizedType -> {
                    val raw = type.rawType as? Class<*>
                    if (raw != null && Collection::class.java.isAssignableFrom(raw)) {
                        type.actualTypeArguments.any { matchesType(it, element) || matchesCollectionOf(it, element) }
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }
}
