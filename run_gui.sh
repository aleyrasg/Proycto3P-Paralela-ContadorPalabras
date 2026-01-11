#!/bin/bash
# Script para iniciar VentanaComparativa con suficiente memoria

echo "🚀 Iniciando VentanaComparativa con memoria extendida..."
echo "   Memoria inicial: 1GB"
echo "   Memoria máxima: 4GB"
echo "   Garbage Collector: G1GC (optimizado)"
echo ""

# Ejecutar con 4GB de heap memory y G1GC
java -Xms1024m -Xmx4096m -XX:+UseG1GC VentanaComparativa

echo ""
echo "✅ Aplicación cerrada"
