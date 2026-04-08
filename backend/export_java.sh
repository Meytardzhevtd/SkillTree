#!/bin/bash

OUTPUT_FILE="context_for_ai.txt"

> "$OUTPUT_FILE"

add_files() {
    local dir=$1
    if [ -d "$dir" ]; then
        find "$dir" -name "*.java" -type f | sort | while read -r file; do
            echo "file name: $file" >> "$OUTPUT_FILE"
            echo >> "$OUTPUT_FILE"
            cat "$file" >> "$OUTPUT_FILE"
            
            echo >> "$OUTPUT_FILE"
            echo "-------------------------------------------------" >> "$OUTPUT_FILE"
            echo >> "$OUTPUT_FILE"
        done
    else
        echo "   ⚠️ Папка $dir не найдена, пропускаю."
    fi
}

add_files "./src/main/java"

# Если нужны тесты, раскомментируй следующую строку:
# add_files "./src/test/java"

LINES=$(wc -l < "$OUTPUT_FILE")
SIZE=$(du -h "$OUTPUT_FILE" | cut -f1)

echo "✅ Готово!"
echo "📄 Файл: $OUTPUT_FILE"
echo "📏 Строк: $LINES"
echo "💾 Размер: $SIZE"