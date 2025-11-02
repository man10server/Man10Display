#!/usr/bin/env bash
set -euo pipefail

# Man10Display クリーンアップスクリプト
# ビルド成果物とキャッシュを削除します

echo "🧹 クリーンアップ開始..."
echo ""

# Gradleクリーン実行
if [[ -f "gradlew" ]]; then
  echo "📦 Gradleクリーン実行中..."
  ./gradlew clean
  echo "✅ Gradleクリーン完了"
else
  echo "⚠️  gradlewが見つかりません。Gradleクリーンをスキップします"
fi

# ビルドディレクトリを削除
if [[ -d "build" ]]; then
  echo "🗑️  buildディレクトリを削除中..."
  rm -rf build
  echo "✅ buildディレクトリ削除完了"
fi

# .gradleディレクトリを削除（オプション）
if [[ -d ".gradle" ]]; then
  echo "🗑️  .gradleディレクトリを削除中..."
  rm -rf .gradle
  echo "✅ .gradleディレクトリ削除完了"
fi

echo ""
echo "🎉 クリーンアップ完了！"

