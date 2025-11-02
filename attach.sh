#!/usr/bin/env bash
set -euo pipefail

# Man10Display サーバーアタッチスクリプト
# Kubernetes環境のサーバーにアタッチします

POD_NAME="mcserver-test-staging-0"

echo "🔌 サーバーにアタッチ中..."
echo "Pod: $POD_NAME"
echo "💡 Ctrl+C で切断できます"
echo ""

kubectl attach -it "$POD_NAME"



