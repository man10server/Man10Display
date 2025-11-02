#!/bin/bash
# macOS用: OBS Virtual CameraからMinecraftサーバーにストリーム送信
# 解像度: 512x384 (4:3 = 4x3マップ), フレームレート: 10fps, ポート: 12346
# マイクラマップサイズ: 128x128ピクセル
# 4マップ(幅) × 128 = 512px, 3マップ(高さ) × 128 = 384px

SERVER_IP="${1:-man10.local}"
VIDEO_DEVICE_INDEX="${2:-0}"

echo "📹 OBS Virtual Cameraからストリーム送信開始"
echo "サーバー: $SERVER_IP:31000"
echo "デバイスインデックス: $VIDEO_DEVICE_INDEX"
echo "解像度: 512x384 (4:3 = 4x3マップ)"
echo "マイクラマップサイズ: 128x128ピクセル"
echo "フレームレート: 10fps (出力)"
echo ""

# デバイスがサポートする形式を確認（エラー時のみ表示）
echo "デバイス形式を確認中..."
ffmpeg -f avfoundation -video_device_index "$VIDEO_DEVICE_INDEX" -i ":0" 2>&1 | grep -i "supported modes" || true
echo ""

# ストリーム送信（フレームレート指定なし - デバイスのデフォルトを使用）
ffmpeg -y \
  -f avfoundation \
  -video_device_index "$VIDEO_DEVICE_INDEX" \
  -i ":0" \
  -vf scale=512:384 \
  -bufsize 5000k \
  -maxrate 4000k \
  -b:v 3000k \
  -f rawvideo \
  -c:v mjpeg \
  -qscale:v 5 \
  -r 10 \
  udp://"$SERVER_IP":31001

# エラーが出た場合、60fpsを明示的に指定して再試行
if [ $? -ne 0 ]; then
  echo ""
  echo "⚠️  デフォルト設定で失敗。60fpsを明示的に指定して再試行..."
  echo ""
  
  ffmpeg -y \
    -f avfoundation \
    -framerate 60 \
    -video_device_index "$VIDEO_DEVICE_INDEX" \
    -video_size 1920x1080 \
    -i ":0" \
    -vf scale=512:384 \
    -bufsize 5000k \
    -maxrate 4000k \
    -b:v 3000k \
    -f rawvideo \
    -c:v mjpeg \
    -qscale:v 5 \
    -r 10 \
    udp://"$SERVER_IP":31001
fi

