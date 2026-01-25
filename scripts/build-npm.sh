#!/bin/bash
set -e

echo "🔨 Building Fat JAR..."
./gradlew build

echo "📦 Preparing NPM package..."
cp build/libs/docutilians.jar npm-package/lib/

echo "✅ Done! Ready to publish."
echo "   cd npm-package && npm publish"