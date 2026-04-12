#!/bin/bash

OUTPUT_FILE="context_for_ai.txt"

> "$OUTPUT_FILE"

add_files() {
    local dir=$1
    if [ -d "$dir" ]; then
        find "$dir" -type f \( -name "*.tsx" -o -name "*.ts" \) | sort | while read -r file; do
            if [[ "$file" == *"node_modules"* ]] || [[ "$file" == *"dist"* ]] || [[ "$file" == *"build"* ]]; then
                continue
            fi
            
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

add_files "./src"



LINES=$(wc -l < "$OUTPUT_FILE")
SIZE=$(du -h "$OUTPUT_FILE" | cut -f1)

echo "✅ Готово!"
echo "📄 Файл: $OUTPUT_FILE"
echo "📏 Строк: $LINES"
echo "💾 Размер: $SIZE"