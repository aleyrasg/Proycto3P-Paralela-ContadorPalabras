#!/bin/bash
# Script para iniciar VentanaComparativa con suficiente memoria

echo "🚀 Iniciando VentanaComparativa con memoria extendida..."
echo "   Memoria inicial: 2GB"
echo "   Memoria máxima: 8GB"
echo "   Garbage Collector: G1GC (optimizado)"
echo "   ⚠️  ARCHIVO GRANDE: Procesando 1.1GB de texto"
echo ""

# Ejecutar con 8GB de heap memory y G1GC
java -Xms2048m -Xmx8192m -XX:+UseG1GC VentanaComparativa

echo ""
echo "✅ Aplicación cerrada"
