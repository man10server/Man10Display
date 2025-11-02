package red.man10.display

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.function.Consumer
import javax.imageio.ImageIO

open class VideoCaptureServer(port: Int) : Thread(), AutoCloseable {
    @Volatile
    var running = true
    private var socket: DatagramSocket? = null
    private var frameConsumer: Consumer<BufferedImage>? = null
    private var portNo = port
    var frameReceivedCount: Long = 0
    var frameReceivedBytes: Long = 0
    var frameErrorCount: Long = 0
    private var lastLogTime: Long = 0
    private var lastFrameLogTime: Long = 0
    private var lastFrameCount: Long = 0
    private val logInterval: Long = 5000 // 5秒ごとにログ出力
    
    fun resetStats() {
        frameReceivedCount = 0
        frameReceivedBytes = 0
        frameErrorCount = 0
        lastLogTime = System.currentTimeMillis()
        lastFrameLogTime = System.currentTimeMillis()
        lastFrameCount = 0
    }

    override fun close() {
        info("closing VideoCaptureServer port:$portNo")
        running = false
        try {
            if (socket != null) {
                if (socket?.isConnected == true) {
                    socket?.disconnect()
                }
                socket?.close()
                socket = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        frameConsumer = null
        join()
    }

    fun onFrame(consumer: Consumer<BufferedImage>) {
        frameConsumer = consumer
    }

    fun deinit() {
        close()
    }

    override fun run() {
        info("VideoCaptureServer.run() 開始: ポート=$portNo, スレッド名=${Thread.currentThread().name}")
        try {
            val buffer = ByteArray(1000 * 1000)
            info("VideoCaptureServer: DatagramSocket作成開始...")
            socket = DatagramSocket(null)
            socket?.reuseAddress = true
            info("VideoCaptureServer: ポート $portNo にバインド開始...")
            socket?.bind(InetSocketAddress(portNo))
            info("📹 ストリーム受信開始: ポート $portNo で待機中...")
            info("📡 UDPソケットバインド成功: ポート=$portNo, ローカルアドレス=${socket?.localAddress}:${socket?.localPort}")
            lastLogTime = System.currentTimeMillis()
            lastFrameLogTime = System.currentTimeMillis()
            lastFrameCount = 0
            
            // 受信タイムアウトを設定（30秒）
            socket?.soTimeout = 30000
            info("VideoCaptureServer: タイムアウト設定完了 (30秒), パケット受信待機開始...")

            val packet = DatagramPacket(buffer, buffer.size)
            var firstPacketReceived = false
            val output = ByteArrayOutputStream()
            var soi = 0 // start of image / SOI
            var eoi = 0 // end of image / EOI
            while (running) {
                if (!running) break
                var data: ByteArray? = null
                var length: Int = 0
                try {
                    socket!!.receive(packet)
                    data = packet.data
                    length = packet.length
                    
                    // 最初のパケット受信ログ
                    if (!firstPacketReceived) {
                        info("🎉 最初のパケット受信成功: ポート=$portNo, サイズ=${length}bytes, 送信元=${packet.address}:${packet.port}")
                        firstPacketReceived = true
                    }
                    
                    frameReceivedBytes += length.toLong()
                } catch (e: java.net.SocketTimeoutException) {
                    // タイムアウト時のログ（30秒ごと）
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastLogTime >= 30000) {
                        warning("⏱️ パケット受信タイムアウト: ポート=$portNo, 累計フレーム=$frameReceivedCount, 累計バイト=${frameReceivedBytes / 1024}KB")
                        info("💡 ヒント: ストリーム送信が開始されているか確認してください。FFmpegがポート $portNo に送信しているか確認してください。")
                        lastLogTime = currentTime
                    }
                    continue
                }
                
                // パケットデータの処理
                if (data != null && length > 0) {
                    for (i in packet.offset until length) {
                        val b = data[i]
                        when (b) {
                            0xFF.toByte() -> {
                                if (soi % 2 == 0) soi++ // find next byte
                                if (eoi == 0) eoi++
                            }

                            0xD8.toByte() -> {
                                if (soi % 2 == 1) {
                                    soi++               // first SOI found
                                }
                                if (soi == 4) {
                                    // found another SOI, probably incomplete frame.
                                    // discard previous data, restart with this SOI
                                    output.reset()
                                    output.write(0xFF)
                                    soi = 2
                                }
                            }

                            0xD9.toByte() -> if (eoi == 1) eoi++ // EOI found
                            else -> {
                                // wrong byte, reset
                                if (soi == 1) soi = 0
                                if (eoi == 1) eoi = 0
                                if (soi == 3) soi--
                            }
                        }
                        output.write(b.toInt())
                        if (eoi == 2) { // image is complete
                            try {
                                val stream = ByteArrayInputStream(output.toByteArray())
                                val bufferedImage = ImageIO.read(stream)
                                if (bufferedImage == null) {
                                    frameErrorCount++
                                    if (frameErrorCount % 10 == 0L || frameErrorCount == 1L) {
                                        error("❌ 画像解析失敗: ポート=$portNo, エラー数=$frameErrorCount, 画像データが無効です（JPEGデータが不完全な可能性があります）")
                                    }
                                    // reset
                                    output.reset()
                                    soi = 0
                                    eoi = 0
                                    continue
                                }
                                
                                // bufferedImageはnullでないことを確認済み
                                bufferedImage.let {
                                    frameConsumer?.accept(it)
                                }
                                frameReceivedCount++
                                
                                // フレーム受信完了ログ（定期的に）
                                val currentTime = System.currentTimeMillis()
                                if (frameReceivedCount % 100 == 0L || currentTime - lastFrameLogTime >= logInterval) {
                                    val timeDiff = currentTime - lastFrameLogTime
                                    val frameDiff = frameReceivedCount - lastFrameCount
                                    val fps = if (frameDiff > 0 && timeDiff > 0) {
                                        String.format("%.2f", frameDiff * 1000.0 / timeDiff)
                                    } else {
                                        "0.00"
                                    }
                                    // bufferedImageはnullでないことを確認済みなので、安全にアクセス可能
                                    info("✅ フレーム受信完了: ポート=$portNo, フレーム#=$frameReceivedCount, 解像度=${bufferedImage.width}x${bufferedImage.height}, 推定FPS=$fps")
                                    lastFrameLogTime = currentTime
                                    lastFrameCount = frameReceivedCount
                                }
                                
                                // reset
                                output.reset()
                                soi = 0
                                eoi = 0
                            } catch (e: IOException) {
                                frameErrorCount++
                                if (frameErrorCount % 10 == 0L || frameErrorCount == 1L) {
                                    error("❌ フレーム解析エラー: ポート=$portNo, エラー数=$frameErrorCount, エラー=${e.message}")
                                }
                                // reset on error
                                output.reset()
                                soi = 0
                                eoi = 0
                            }
                        }
                    }
                }
            }
            info("server stopped port:$portNo")
        } catch (e: IOException) {
            error("❌ VideoCaptureServer エラー: ポート=$portNo, エラー=${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            error("❌ VideoCaptureServer 予期しないエラー: ポート=$portNo, エラー=${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
        }
    }

}

