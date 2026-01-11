@echo off
REM =====================================================
REM  Script de Inicio de Servidores RMI - Windows
REM  Proyecto: Contador de Palabras Distribuido
REM =====================================================

echo.
echo ======================================================
echo   INICIANDO 4 SERVIDORES RMI
echo ======================================================
echo.

REM Verificar que los archivos .class existen
if not exist "ServidorRMI.class" (
    echo ERROR: No se encuentra ServidorRMI.class
    echo Por favor, copia los archivos .class desde tu Mac
    pause
    exit /b 1
)

if not exist "ContadorRemotoImpl.class" (
    echo ERROR: No se encuentra ContadorRemotoImpl.class
    echo Por favor, copia los archivos .class desde tu Mac
    pause
    exit /b 1
)

echo [1/4] Iniciando Servidor RMI en puerto 1099...
start "Servidor RMI 1099" java ServidorRMI 1099
timeout /t 3 /nobreak >nul

echo [2/4] Iniciando Servidor RMI en puerto 1100...
start "Servidor RMI 1100" java ServidorRMI 1100
timeout /t 3 /nobreak >nul

echo [3/4] Iniciando Servidor RMI en puerto 1101...
start "Servidor RMI 1101" java ServidorRMI 1101
timeout /t 3 /nobreak >nul

echo [4/4] Iniciando Servidor RMI en puerto 1102...
start "Servidor RMI 1102" java ServidorRMI 1102
timeout /t 3 /nobreak >nul

echo.
echo ======================================================
echo   4 SERVIDORES RMI INICIADOS CORRECTAMENTE
echo ======================================================
echo   Puertos: 1099, 1100, 1101, 1102
echo   Procesadores: 16 hilos por servidor (2x cores)
echo   Listo para recibir conexiones
echo ======================================================
echo.
echo IMPORTANTE: No cierres estas ventanas
echo Los servidores deben permanecer activos durante la prueba
echo.
echo Para detenerlos: Presiona Ctrl+C en cada ventana
echo                  O cierra este script con Ctrl+C
echo ======================================================
echo.
pause

REM Limpieza al salir
taskkill /FI "WindowTitle eq Servidor RMI*" /F >nul 2>&1
echo.
echo Servidores detenidos.
pause
