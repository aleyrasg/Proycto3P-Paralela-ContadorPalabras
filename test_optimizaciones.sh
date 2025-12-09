#!/bin/bash
# Script para probar las optimizaciones de RMI

echo "🚀 Prueba de optimizaciones RMI vs Concurrente"
echo "=============================================="
echo ""

# Verificar que los archivos estén compilados
if [ ! -f "VentanaComparativa.class" ]; then
    echo "⚠️  Compilando archivos..."
    javac *.java
    if [ $? -ne 0 ]; then
        echo "❌ Error en compilación"
        exit 1
    fi
    echo "✅ Compilación exitosa"
    echo ""
fi

# Verificar número de cores disponibles
cores=$(sysctl -n hw.ncpu 2>/dev/null || nproc 2>/dev/null || echo "desconocido")
echo "💻 Cores disponibles en esta máquina: $cores"
echo ""

# Verificar tamaño del archivo de prueba
if [ -f "text1.txt" ]; then
    size=$(wc -c < text1.txt)
    lines=$(wc -l < text1.txt)
    echo "📄 Archivo de prueba: text1.txt"
    echo "   Tamaño: $(numfmt --to=iec $size 2>/dev/null || echo "$size bytes")"
    echo "   Líneas: $lines"
else
    echo "⚠️  No se encontró text1.txt"
fi
echo ""

echo "📋 INSTRUCCIONES:"
echo "================"
echo ""
echo "1️⃣  Inicia los servidores RMI (en terminales separadas):"
echo ""
echo "   Terminal 1:"
echo "   $ java ServidorRMI 1099"
echo ""
echo "   Terminal 2:"
echo "   $ java ServidorRMI 1100"
echo ""
echo "   ℹ️  Deberías ver: '🚀 Servidor optimizado con X hilos paralelos'"
echo ""
echo "2️⃣  Ejecuta la interfaz comparativa:"
echo ""
echo "   $ java VentanaComparativa"
echo ""
echo "3️⃣  En la interfaz:"
echo "   - Selecciona text1.txt"
echo "   - Configura 4 hilos concurrentes (máximo)"
echo "   - Click en '🚀 Ejecutar Comparativa Completa'"
echo ""
echo "4️⃣  Observa los resultados:"
echo "   - Paralelo (RMI) debería ser 2-3x más rápido que Concurrente"
echo "   - Verifica el Speedup y Eficiencia en la tabla"
echo ""
echo "=============================================="
echo ""
echo "💡 TIPS para maximizar victoria de RMI:"
echo ""
echo "   ✅ Usar archivos grandes (>100KB)"
echo "   ✅ Limitar hilos concurrentes a 4-6"
echo "   ✅ Correr servidores en máquinas diferentes si es posible"
echo "   ✅ Verificar que ambos servidores estén corriendo"
echo ""
echo "📖 Más detalles en: OPTIMIZACIONES_RMI.md"
echo ""
