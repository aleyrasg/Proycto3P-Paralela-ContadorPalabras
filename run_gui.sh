#!/bin/bash
# Script para iniciar VentanaComparativa con suficiente memoria

echo "🚀 Iniciando VentanaComparativa con memoria extendida..."
echo "   Memoria inicial: 512MB"
echo "   Memoria máxima: 2GB"
echo ""

# Ejecutar con 2GB de heap memory
java -Xms512m -Xmx2048m VentanaComparativa

echo ""
echo "✅ Aplicación cerrada"
