# Man10Display Plugin

Minecraftサーバー用ディスプレイシステムプラグイン - アイテムフレームを使用した大規模ディスプレイ表示システム

## 📋 概要

Man10Displayは、Minecraftサーバー上でアイテムフレームを組み合わせて大規模なディスプレイを作成し、画像・動画・テキストなどを表示できるプラグインです。

### 主な機能

- 🖼️ **画像表示**: URLから画像を読み込んで表示
- 📺 **動画ストリーミング**: OBSなどからのストリーム表示
- 🎨 **描画機能**: ペンアイテムで直接描画
- 📸 **写真表示**: 写真アイテムを作成して表示
- 🔧 **マクロ機能**: 複雑な表示パターンを自動実行
- ⚙️ **フィルター機能**: 画像処理フィルター（ぼかし、明るさ調整など）
- 🎮 **インタラクティブ**: クリックでコマンド実行

## 🛠️ 技術仕様

- **Java**: 21
- **Kotlin**: 2.0.0
- **Gradle**: 9.0.0
- **Paper API**: 1.21.8-R0.1-SNAPSHOT
- **Kotlin Coroutines**: 1.9.0
- **Vault**: 1.7
- **依存プラグイン**: ProtocolLib (必須), ItemFrameProtector (推奨)

## 🚀 インストール

1. ProtocolLibプラグインをサーバーにインストール
2. Man10DisplayのJARファイルを`plugins/`ディレクトリに配置
3. サーバーを再起動または`/reload`

## 📖 コマンド一覧

### 基本コマンド

| コマンド | エイリアス | 説明 | 権限 |
|---------|----------|------|------|
| `/mdisplay` | `/md` | メインコマンド | `red.man10.display.op` |

### ディスプレイ管理

#### 作成・削除

- **`/md create [名前] [幅] [高さ] [ポート]`**
  - 新しいディスプレイを作成
  - 幅・高さ: 1-24 (アイテムフレームの数)
  - ポート: 0-65535 (0でストリーム無効、OBS等からストリーム受信)

- **`/md delete [名前]`**
  - ディスプレイを削除

- **`/md save [名前]`**
  - ディスプレイの状態を保存

#### 情報・リスト

- **`/md list`**
  - 全てのディスプレイ一覧を表示

- **`/md info [名前]`**
  - ディスプレイの詳細情報を表示

- **`/md stats [名前]`**
  - ディスプレイの統計情報を表示

- **`/md map [名前]`**
  - ディスプレイに使用されているマップアイテムを取得

#### 操作

- **`/md place [名前]`**
  - 見ている方向にアイテムフレームを自動配置してディスプレイを作成

- **`/md place_growing [名前]`**
  - 成長型ディスプレイを作成（段階的に拡大）

- **`/md remove`**
  - 見ている方向のディスプレイを削除

- **`/md tp [名前]`**
  - ディスプレイにテレポート

- **`/md clear [名前]`**
  - ディスプレイをクリア（白画面）

- **`/md reset [名前]`**
  - ディスプレイをリセット

- **`/md refresh [名前]`**
  - ディスプレイをリフレッシュ

### 描画・表示

- **`/md set [名前] [パラメータ] [値]`**
  - ディスプレイのパラメータを設定
  - パラメータ例: `image`, `filter`, `brightness`, `contrast` など

- **`/md image [名前] [画像URL]`**
  - 画像を読み込んで表示

### マクロ・アプリ

- **`/md run [名前] [マクロ名]`**
  - マクロを実行

- **`/md stop [名前]`**
  - ディスプレイの実行を停止

- **`/md stopall`**
  - 全ディスプレイを停止

### 特殊アイテム作成

- **`/md wand`**
  - アイテムフレーム選択用のワンドを取得

- **`/md create_pen [幅(1-30)] [色コード(#000000)]`**
  - 手に持っているアイテムをペンに変換
  - ペンでアイテムフレームをクリックして描画

- **`/md create_photo [画像URL]`**
  - 写真アイテムを作成

- **`/md create_app [マクロ名]`**
  - アプリアイテムを作成（クリックでマクロ実行）

- **`/md create_ticket [タイプ] [値]`**
  - チケットアイテムを作成
  - タイプ: `start`, `end`, `player`, `command`, `op_command`, `data`, `macro`, `key`, `image`

### その他

- **`/md reload`**
  - 設定ファイルをリロード

## 📝 使用例

### 基本的な使い方

1. **ディスプレイを作成**
   ```
   /md create mydisplay 10 5 12345
   ```
   - 名前: `mydisplay`
   - サイズ: 10×5 アイテムフレーム
   - ポート: 12345 (OBSからストリーム受信)

2. **現在位置に配置**
   ```
   /md place mydisplay
   ```
   見ている方向に自動でアイテムフレームが配置されます。

3. **画像を表示**
   ```
   /md image mydisplay https://example.com/image.png
   ```

4. **OBSからストリームを送信**
   - OBSの「ブラウザソース」または「画像ソース」で以下を設定
   - URL: `http://サーバーIP:12345`

### ペンで描画

1. **ペンを作成**
   ```
   /md create_pen 3 #FF0000
   ```
   - 幅: 3ブロック
   - 色: 赤 (#FF0000)

2. **ペンを持ってアイテムフレームをクリック**
   - 右クリックで描画
   - 左クリックで消去

### マクロの使用

1. **マクロを実行**
   ```
   /md run mydisplay mymacro
   ```

2. **実行を停止**
   ```
   /md stop mydisplay
   ```

## ⚙️ 設定

設定ファイルは `plugins/Man10Display/config.yml` に生成されます。

主な設定項目：
- `imagePath`: 画像ファイルの保存パス
- その他の詳細設定

## 🎨 フィルター機能

画像表示時に適用できるフィルター：

- **明るさ調整** (`brightness`)
- **コントラスト調整** (`contrast`)
- **ぼかし** (`blur`)
- **グレースケール** (`grayscale`)
- **セピア** (`sepia`)
- **ディザリング** (`dithering`)
- **シャープ** (`sharpen`)
- **その他多数**

## 🔧 開発者向け情報

### ビルド

```bash
./build.sh
```

ビルド完了後、JARファイルは自動的にサーバーディレクトリにコピーされます。

### 開発環境

#### Kubernetes環境での操作

**サーバー情報**
- **Pod名**: `mcserver-test-staging-0`
- **ディレクトリ**: `/btrfs/ssd-main/mcserver/test-staging/...`

**ログ確認**
```bash
kubectl logs mcserver-test-staging-0
```

**RCONコマンド実行**
```bash
kubectl exec mcserver-test-staging-0 -- rcon-cli <コマンド>
```

**サーバーにアタッチ**
```bash
kubectl attach -it mcserver-test-staging-0
```

**JARファイルの配置先**
ビルド完了後、JARファイルは以下のディレクトリに自動的にコピーされます：
```
/btrfs/ssd-main/mcserver/test-staging/plugins/
```

### Git操作

**コミット前の確認**
変更をコミットする前に、以下を確認してください：

```bash
# 変更内容を確認
git status

# 差分を確認
git diff

# ビルドが通ることを確認
./build.sh
```

### 開発フロー

1. コード編集
2. 変更内容を確認（`git diff`）
3. `./build.sh` でビルド（自動デプロイ付き）
4. `./reload.sh` でリロード
5. 動作確認
6. Gitにコミット（`git add .` → `git commit -m "メッセージ"`）

### 便利なスクリプト

- `./build.sh` - ビルドと自動デプロイ
- `./reload.sh` - プラグインリロード（PlugManX使用）
- `./attach.sh` - サーバーにアタッチ
- `./restart.sh` - サーバー再起動
- `./clean.sh` - ビルド成果物とキャッシュのクリーンアップ

## 📚 権限一覧

| 権限 | 説明 |
|------|------|
| `red.man10.display.*` | 全権限 |
| `red.man10.display.op` | 基本操作権限 |
| `red.man10.display.create` | ディスプレイ作成 |
| `red.man10.display.delete` | ディスプレイ削除 |
| `red.man10.display.place` | アイテムフレーム配置 |
| `red.man10.display.remove` | アイテムフレーム削除 |
| `red.man10.display.reload` | リロード |
| `red.man10.display.wand` | ワンド取得 |
| `red.man10.display.set` | パラメータ設定 |
| `red.man10.display.teleport` | テレポート |
| `red.man10.display.run` | マクロ実行 |
| `red.man10.display.stop` | 停止 |
| `red.man10.display.clear` | クリア |
| `red.man10.display.create_pen` | ペン作成 |
| `red.man10.display.create_photo` | 写真作成 |
| `red.man10.display.create_app` | アプリ作成 |
| `red.man10.display.create_ticket` | チケット作成 |

## 🌐 外部連携

### OBS連携

#### FFmpegを使用したストリーミング

FFmpegを使用してOBS Virtual CameraからMinecraftサーバーにストリームを送信できます。

**Windows版:**
```bash
ffmpeg -y -f dshow -i video="OBS Virtual Camera" \
  -vf scale=2048:1152 \
  -bufsize 5000k -maxrate 4000k -b:v 3000k \
  -f rawvideo -c:v mjpeg -qscale:v 5 -r 10 \
  udp://127.0.0.1:12345
```

**macOS版:**
```bash
# 利用可能なカメラデバイスを確認
ffmpeg -f avfoundation -list_devices true -i ""

# OBS Virtual Cameraからストリーム送信（推奨：フレームレート指定なし）
# エラーが出た場合は、デバイスがサポートするフレームレートを使用
ffmpeg -y -f avfoundation -video_device_index 0 -i ":0" \
  -vf scale=2048:1152 \
  -bufsize 5000k -maxrate 4000k -b:v 3000k \
  -f rawvideo -c:v mjpeg -qscale:v 5 -r 10 \
  udp://サーバーIP:12345

# エラー「framerate is not supported」が出た場合の対処法
# エラーメッセージに表示されたサポート形式を使用（例：60fps）
ffmpeg -y -f avfoundation -framerate 60 -video_device_index 0 -video_size 1920x1080 \
  -i ":0" \
  -vf scale=2048:1152 \
  -bufsize 5000k -maxrate 4000k -b:v 3000k \
  -f rawvideo -c:v mjpeg -qscale:v 5 -r 10 \
  udp://サーバーIP:12345
```

**トラブルシューティング:**
- **エラー: "framerate is not supported"**
  - エラーメッセージに表示されたサポート形式（例：`1920x1080@60fps`）を使用
  - `-framerate`と`-video_size`をエラーメッセージの値に合わせる
  - または`-framerate`と`-video_size`を省略してデフォルト値を使用

- **出力フレームレートについて**
  - `-r 10`は最終的な送信レート（10fps）で、入力はデバイスのレート（例：60fps）を受け取ってダウンサンプリング

**Linux版:**
```bash
# v4l2を使用
ffmpeg -y -f v4l2 -input_format mjpeg -framerate 30 \
  -video_size 1920x1080 -i /dev/video0 \
  -vf scale=2048:1152 \
  -bufsize 5000k -maxrate 4000k -b:v 3000k \
  -f rawvideo -c:v mjpeg -qscale:v 5 -r 10 \
  udp://サーバーIP:12345
```

**パラメータ説明:**
- `scale=2048:1152`: 出力解像度（ディスプレイサイズに合わせて調整）
- `-r 10`: フレームレート（10fps）
- `-b:v 3000k`: ビットレート（3000kbps）
- `udp://IP:ポート`: サーバーIPとディスプレイ作成時に指定したポート番号

**利用可能なスクリプト:**
`obs/`ディレクトリに様々な解像度・フレームレートのサンプルスクリプトがあります。

#### OBS Studioのブラウザソースを使用

1. OBSで「ブラウザソース」を追加
2. URL: `http://サーバーIP:ポート番号`
3. 幅・高さを設定

### その他のストリーミングツール

- 同じポート番号でHTTPサーバーとして動作
- 画像やHTMLコンテンツを表示可能

## ⚠️ 注意事項

- ProtocolLibが必須です（プラグインが無効化されます）
- 大量のアイテムフレームを使用するため、サーバーのパフォーマンスに注意
- 画像読み込みはインターネット接続が必要
- ポート番号は他の用途と重複しないよう注意

## 🔗 リンク

- **Website**: https://man10.red
- **Author**: takatronix

## 📄 ライセンス

このプラグインのライセンス情報については、リポジトリを参照してください。
