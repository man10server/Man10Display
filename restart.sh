#!/usr/bin/env bash
set -euo pipefail

# Man10Display サーバー再起動スクリプト
# Kubernetes環境のサーバーを再起動します

STATEFULSET_NAME="mcserver-test-staging"

echo "🔄 サーバーを再起動中..."
echo "StatefulSet: $STATEFULSET_NAME"
echo ""

kubectl rollout restart statefulset "$STATEFULSET_NAME"

echo ""
echo "✅ 再起動コマンドを実行しました"
echo "💡 再起動の進行状況を確認: kubectl rollout status statefulset $STATEFULSET_NAME"

