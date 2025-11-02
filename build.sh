#!/usr/bin/env bash
set -euo pipefail

# Man10Display ビルドスクリプト
# Gradle を使用してプロジェクトをビルドします

echo "🔨 Man10Display ビルド開始"
echo ""

# Gradle Wrapper の確認と生成
setup_gradle_wrapper() {
  if [[ ! -f "gradlew" ]]; then
    echo "📦 Gradle Wrapper が見つかりません。生成中..."
    
    if ! command -v gradle >/dev/null 2>&1; then
      echo "❌ エラー: gradle コマンドが見つかりません"
      echo "💡 Gradle をインストールするか、gradlew ファイルを手動で生成してください"
      echo "   インストール: sudo apt install gradle"
      exit 1
    fi
    
    gradle wrapper
    echo "✅ Gradle Wrapper 生成完了"
  else
    echo "✅ Gradle Wrapper 確認済み"
  fi
}

# 実行権限の付与
chmod_gradlew() {
  if [[ -f "gradlew" ]] && [[ ! -x "gradlew" ]]; then
    echo "🔧 gradlew に実行権限を付与中..."
    chmod +x gradlew
  fi
}

# 定数設定
POD_NAME="mcserver-test-staging-0"
PLUGIN_NAME="Man10Display"
DEPLOY_TARGET="/btrfs/ssd-main/mcserver/test-staging/plugins"

# ビルド実行
build_project() {
  echo ""
  echo "🏗️  ビルド実行中..."
  echo ""
  
  ./gradlew clean build
  
  if [[ $? -eq 0 ]]; then
    echo ""
    echo "✅ ビルド成功！"
    echo ""
    echo "📦 ビルド成果物:"
    
    # JAR ファイルの場所を表示
    if [[ -d "build/libs" ]]; then
      ls -lh build/libs/*.jar 2>/dev/null | awk '{print "   " $9 " (" $5 ")"}' || true
    fi
  else
    echo ""
    echo "❌ ビルド失敗"
    exit 1
  fi
}

# JARファイルをサーバーディレクトリにコピー
deploy_jar() {
  echo ""
  echo "📤 JARファイルをデプロイ中..."
  
  # JARファイルを検索
  shopt -s nullglob
  jars=(build/libs/*.jar)
  
  if [[ ${#jars[@]} -eq 0 ]]; then
    echo "⚠️  JARファイルが見つかりません"
    return 1
  fi
  
  # 最新のJARファイルを取得
  JAR_FILE=$(ls -1t "${jars[@]}" | head -n1)
  JAR_NAME=$(basename "$JAR_FILE")
  
  # ディレクトリが存在するか確認
  if [[ ! -d "$DEPLOY_TARGET" ]]; then
    echo "⚠️  デプロイ先ディレクトリが見つかりません: $DEPLOY_TARGET"
    echo "💡 手動でコピーしてください: cp $JAR_FILE <デプロイ先>"
    return 1
  fi
  
  # JARファイルをコピー
  cp "$JAR_FILE" "${DEPLOY_TARGET}/${JAR_NAME}"
  
  if [[ $? -eq 0 ]]; then
    echo "✅ デプロイ完了: $JAR_FILE -> ${DEPLOY_TARGET}/${JAR_NAME}"
    echo "💡 プラグインをリロード: kubectl exec ${POD_NAME} -- rcon-cli plugman reload ${PLUGIN_NAME}"
  else
    echo "❌ デプロイ失敗"
    return 1
  fi
}

# RCONラッパー
rcon() {
  kubectl exec "${POD_NAME}" -- rcon-cli "$@"
}

# PlugManXでリロード or ロード
reload_or_load() {
  echo ""
  echo "♻️  プラグインを再読み込み中... (${PLUGIN_NAME})"

  set +e
  RELOAD_OUT=$(rcon plugman reload "${PLUGIN_NAME}" 2>&1)
  RELOAD_CODE=$?
  set -e

  echo "-- plugman reload 出力 --"
  echo "$RELOAD_OUT"
  echo "---------------------------"

  if [[ $RELOAD_CODE -eq 0 ]] && ! echo "$RELOAD_OUT" | grep -qiE "(not found|does not exist|isn't loaded|is not loaded|Unknown command)"; then
    echo "✅ reload 成功"
    return 0
  fi

  echo "🔁 reloadに失敗または未ロードと判断。loadを試行します..."
  set +e
  LOAD_OUT=$(rcon plugman load "${JAR_NAME}" 2>&1)
  LOAD_CODE=$?
  set -e

  echo "-- plugman load 出力 --"
  echo "$LOAD_OUT"
  echo "-------------------------"

  if [[ $LOAD_CODE -eq 0 ]] && ! echo "$LOAD_OUT" | grep -qiE "(not found|does not exist|Unknown command)"; then
    echo "✅ load 成功"
    return 0
  fi

  echo "❌ reload/load ともに失敗しました。手動でご確認ください。"
  return 1
}

# メイン実行
main() {
  setup_gradle_wrapper
  chmod_gradlew
  build_project
  deploy_jar
  reload_or_load || true
  
  echo ""
  echo "🎉 完了！"
}

# スクリプト実行
main "$@"
