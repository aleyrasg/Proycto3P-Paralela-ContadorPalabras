@echo off
REM Script para iniciar VentanaComparativa con suficiente memoria

echo.
echo ======================================================
echo   INICIANDO VENTANA COMPARATIVA
echo ======================================================
echo   Memoria inicial: 512MB
echo   Memoria maxima: 2GB
echo ======================================================
echo.

REM Ejecutar con 2GB de heap memory
java -Xms512m -Xmx2048m VentanaComparativa

echo.
echo Aplicacion cerrada.
pause
