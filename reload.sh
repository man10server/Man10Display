#!/usr/bin/env bash
set -euo pipefail

# Man10Display プラグインリロードスクリプト
# PlugManXを使用してプラグインをリロードします

POD_NAME="mcserver-test-staging-0"
PLUGIN_NAME="Man10Display"

echo "🔄 プラグインをリロード中..."
echo "Pod: $POD_NAME"
echo "Plugin: $PLUGIN_NAME"
echo ""

# PlugManXでリロードを試行
if kubectl exec "$POD_NAME" -- rcon-cli "plugman reload $PLUGIN_NAME" 2>/dev/null; then
    echo ""
    echo "✅ プラグインリロード成功"
else
    echo ""
    echo "⚠️  リロードに失敗しました。無効化してから再度読み込みを試みます..."
    
    # 無効化
    kubectl exec "$POD_NAME" -- rcon-cli "plugman disable $PLUGIN_NAME" 2>/dev/null || true
    sleep 1
    
    # 再度有効化
    if kubectl exec "$POD_NAME" -- rcon-cli "plugman enable $PLUGIN_NAME" 2>/dev/null; then
        echo "✅ プラグイン有効化成功"
    else
        echo "❌ プラグイン有効化失敗"
        exit 1
    fi
fi

echo ""
echo "💡 ログを確認: kubectl logs $POD_NAME --tail=50"

