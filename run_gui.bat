@echo off
REM Script para iniciar VentanaComparativa con suficiente memoria

echo.
echo ======================================================
echo   INICIANDO VENTANA COMPARATIVA
echo ======================================================
echo   Memoria inicial: 2GB
echo   Memoria maxima: 8GB
echo   Garbage Collector: G1GC (optimizado)
echo   ADVERTENCIA: Archivo grande (1.1GB)
echo ======================================================
echo.

REM Ejecutar con 8GB de heap memory y G1GC
java -Xms2048m -Xmx8192m -XX:+UseG1GC VentanaComparativa

echo.
echo Aplicacion cerrada.
pause
