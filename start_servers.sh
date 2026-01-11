#!/bin/bash
# =====================================================
#  Script de Inicio de Servidores RMI - Linux/macOS
#  Proyecto: Contador de Palabras Distribuido
# =====================================================

# Colores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo ""
echo "======================================================"
echo "  🚀 INICIANDO 4 SERVIDORES RMI"
echo "======================================================"
echo ""

# Verificar que los archivos .class existen
if [ ! -f "ServidorRMI.class" ]; then
    echo -e "${RED}❌ ERROR: No se encuentra ServidorRMI.class${NC}"
    echo "Por favor, copia los archivos .class desde tu Mac"
    exit 1
fi

if [ ! -f "ContadorRemotoImpl.class" ]; then
    echo -e "${RED}❌ ERROR: No se encuentra ContadorRemotoImpl.class${NC}"
    echo "Por favor, copia los archivos .class desde tu Mac"
    exit 1
fi

# Array para almacenar los PIDs
declare -a PIDS

# Función para limpiar al salir
cleanup() {
    echo ""
    echo -e "${YELLOW}🛑 Deteniendo servidores...${NC}"
    for pid in "${PIDS[@]}"; do
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null
        fi
    done
    echo -e "${GREEN}✅ Servidores detenidos${NC}"
    exit 0
}

# Capturar Ctrl+C
trap cleanup INT TERM

# Iniciar servidores
echo -e "${BLUE}[1/4]${NC} Iniciando Servidor RMI en puerto 1099..."
java ServidorRMI 1099 &
PIDS[0]=$!
sleep 3

echo -e "${BLUE}[2/4]${NC} Iniciando Servidor RMI en puerto 1100..."
java ServidorRMI 1100 &
PIDS[1]=$!
sleep 3

echo -e "${BLUE}[3/4]${NC} Iniciando Servidor RMI en puerto 1101..."
java ServidorRMI 1101 &
PIDS[2]=$!
sleep 3

echo -e "${BLUE}[4/4]${NC} Iniciando Servidor RMI en puerto 1102..."
java ServidorRMI 1102 &
PIDS[3]=$!
sleep 3

echo ""
echo "======================================================"
echo -e "  ${GREEN}✅ 4 SERVIDORES RMI INICIADOS CORRECTAMENTE${NC}"
echo "======================================================"
echo "  📡 Puertos: 1099, 1100, 1101, 1102"
echo "  🧵 Procesadores: 16 hilos por servidor (2x cores)"
echo "  🌐 Listo para recibir conexiones"
echo "======================================================"
echo ""
echo -e "${YELLOW}📝 PIDs de los procesos:${NC}"
for i in "${!PIDS[@]}"; do
    port=$((1099 + i))
    echo "  - Puerto $port: PID ${PIDS[$i]}"
done
echo ""
echo "======================================================"
echo -e "${YELLOW}⚠️  IMPORTANTE:${NC}"
echo "  - No cierres esta terminal"
echo "  - Los servidores deben permanecer activos"
echo "  - Para detener: Presiona Ctrl+C"
echo "======================================================"
echo ""

# Esperar indefinidamente (hasta Ctrl+C)
wait
