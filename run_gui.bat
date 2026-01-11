@echo off
REM Script para iniciar VentanaComparativa con suficiente memoria

echo.
echo ======================================================
echo   INICIANDO VENTANA COMPARATIVA
echo ======================================================
echo   Memoria inicial: 1GB
echo   Memoria maxima: 4GB
echo   Garbage Collector: G1GC (optimizado)
echo ======================================================
echo.

REM Ejecutar con 4GB de heap memory y G1GC
java -Xms1024m -Xmx4096m -XX:+UseG1GC VentanaComparativa

echo.
echo Aplicacion cerrada.
pause
