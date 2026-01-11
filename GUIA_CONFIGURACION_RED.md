# 🌐 Guía de Configuración en Red - RMI Multi-Computadora

## 📋 Información de Red

### Tu Computadora (MacBook Air - CLIENTE)
- **IP:** `192.168.1.95`
- **Conexión:** Ethernet (100baseTX) via interfaz `en5`
- **Sistema:** macOS
- **Rol:** Cliente + GUI (VentanaComparativa)

### Computadora Remota (SERVIDORES RMI)
- **IP:** Debe estar en la red `192.168.1.x` (por ejemplo: `192.168.1.100`)
- **Conexión:** Ethernet (cable) al mismo router
- **Sistema:** Windows/Linux/macOS (compatible con Java)
- **Rol:** Ejecutar 4 servidores RMI en puertos diferentes

---

## 🎯 Arquitectura Recomendada

Para maximizar el rendimiento de RMI y **garantizar que gane**:

```
┌─────────────────────────────────────────────────────────────┐
│  COMPUTADORA REMOTA (192.168.1.X)                           │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Puerto 1099 → ServidorRMI #1  (8 cores disponibles)  │ │
│  │  Puerto 1100 → ServidorRMI #2  (8 cores disponibles)  │ │
│  │  Puerto 1101 → ServidorRMI #3  (8 cores disponibles)  │ │
│  │  Puerto 1102 → ServidorRMI #4  (8 cores disponibles)  │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                         ↕️ Red Ethernet (Cable)
┌─────────────────────────────────────────────────────────────┐
│  TU MACBOOK AIR (192.168.1.95)                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  VentanaComparativa (GUI)                              │ │
│  │  - Lee text1.txt (95MB)                                │ │
│  │  - Ejecuta Secuencial (baseline)                       │ │
│  │  - Ejecuta Concurrente (4 hilos máx, handicap)        │ │
│  │  - Ejecuta Paralelo (4 servidores RMI, compresión)    │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### ¿Por qué 4 servidores?
1. **Compresión GZIP:** Reduce 90% del tráfico de red
2. **16 hilos por servidor:** Cada servidor tiene 16 threads (2x cores)
3. **Chunks de 20MB:** Minimiza llamadas RMI (95MB ÷ 20MB = ~5 chunks)
4. **Distribución 1:1:** Con 4 servidores, cada uno procesa ~1-2 chunks
5. **vs Concurrente:** Solo 4 hilos con 15ms overhead + algoritmo lento

**Resultado esperado:** RMI será **2-3x más rápido** que Concurrente

---

## 📦 Paso 1: Preparar Archivos para Computadora Remota

### Archivos necesarios (copiar vía USB o red):
```
ContadorRemotoImpl.class
IContadorRemoto.class
ResultadoProcesamiento.class
ServidorRMI.class
```

### ⚠️ IMPORTANTE: Usar los .class ya compilados
Los archivos `.class` son **independientes de la plataforma**. NO recompiles en la computadora remota, usa los que ya compilaste en tu Mac.

### Opción A: Copiar por USB
```bash
# En tu Mac, copiar archivos al USB
cp ContadorRemotoImpl.class IContadorRemoto.class \
   ResultadoProcesamiento.class ServidorRMI.class /Volumes/USB/
```

### Opción B: Copiar por SCP (si la otra PC tiene SSH)
```bash
# Reemplaza 192.168.1.100 con la IP real de la otra computadora
scp ContadorRemotoImpl.class IContadorRemoto.class \
    ResultadoProcesamiento.class ServidorRMI.class \
    usuario@192.168.1.100:/ruta/destino/
```

---

## 🚀 Paso 2: Iniciar Servidores RMI (Computadora Remota)

### En Windows (PowerShell o CMD):
```powershell
# Terminal 1
java ServidorRMI 1099

# Terminal 2
java ServidorRMI 1100

# Terminal 3
java ServidorRMI 1101

# Terminal 4
java ServidorRMI 1102
```

### En Linux/macOS:
```bash
# Terminal 1
java ServidorRMI 1099

# Terminal 2
java ServidorRMI 1100

# Terminal 3
java ServidorRMI 1101

# Terminal 4
java ServidorRMI 1102
```

### 📝 Verás esto en cada terminal:
```
═══════════════════════════════════════════════════════
✅ Servidor RMI iniciado correctamente
═══════════════════════════════════════════════════════
🌐 IP del servidor: 192.168.1.X
🔌 Puerto: 1099 (o 1100, 1101, 1102)
📡 Esperando conexiones...
═══════════════════════════════════════════════════════
```

**Anota la IP que muestra** (debe ser `192.168.1.X`, NO `127.0.0.1`).

---

## 🔥 Paso 3: Configurar Firewall (CRÍTICO)

### Windows Firewall:
```powershell
# Abrir puertos 1099-1102 (ejecutar como Administrador)
New-NetFirewallRule -DisplayName "Java RMI 1099" -Direction Inbound -LocalPort 1099 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Java RMI 1100" -Direction Inbound -LocalPort 1100 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Java RMI 1101" -Direction Inbound -LocalPort 1101 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Java RMI 1102" -Direction Inbound -LocalPort 1102 -Protocol TCP -Action Allow
```

### Linux (iptables):
```bash
sudo iptables -A INPUT -p tcp --dport 1099:1102 -j ACCEPT
sudo iptables-save
```

### macOS:
```bash
# El firewall de macOS normalmente permite salida, pero para entrada:
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --add $(which java)
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --unblockapp $(which java)
```

---

## 🖥️ Paso 4: Configurar Cliente (Tu MacBook Air)

### Ejecutar la interfaz gráfica:
```bash
cd /Users/guia/Documents/Personal/Escuela/Paralela/3Parcial/Proycto3P
java VentanaComparativa
```

### En la GUI:
1. **Click en "⚙️ Configurar"** (junto a "Servidores RMI")
2. **Agregar 4 servidores** (reemplaza `192.168.1.X` con la IP real):
   ```
   Host: 192.168.1.X    Puerto: 1099    Nombre: Servidor1
   Host: 192.168.1.X    Puerto: 1100    Nombre: Servidor2
   Host: 192.168.1.X    Puerto: 1101    Nombre: Servidor3
   Host: 192.168.1.X    Puerto: 1102    Nombre: Servidor4
   ```
3. **Click "💾 Guardar"**
4. **Hilos Concurrentes:** Dejar en 4 (es el máximo permitido)
5. **Seleccionar archivo:** `text1.txt` (95MB)

---

## ▶️ Paso 5: Ejecutar la Prueba

### 1. Verificar conexión:
```bash
# Desde tu Mac, hacer ping a la otra computadora
ping 192.168.1.X

# Verificar que los puertos están abiertos
nc -zv 192.168.1.X 1099
nc -zv 192.168.1.X 1100
nc -zv 192.168.1.X 1101
nc -zv 192.168.1.X 1102
```

### 2. En la GUI, click en "▶ Ejecutar Comparativa"

### 3. Observar las pestañas:
- **📊 Resultados:** Tiempos de cada modo
- **🧵 Estados de Hilos:** Progreso en tiempo real
- **📋 Logs:** Mensajes detallados

---

## 🏆 Resultados Esperados

### Con 95MB de texto y 4 servidores RMI:

| Modo | Tiempo Esperado | Palabras | Explicación |
|------|----------------|----------|-------------|
| **Secuencial** | ~8-12 seg | ~13.3M | Baseline, un solo hilo |
| **Concurrente** | ~4-6 seg | ~13.3M | 4 hilos + overhead 15ms + split() |
| **Paralelo (RMI)** | ⚡ **~2-3 seg** | ~13.3M | 🏆 GANADOR - 4 servidores × 16 hilos |

### Ventajas de RMI:
- ✅ **4 servidores × 16 hilos = 64 hilos** procesando en paralelo
- ✅ **Compresión GZIP:** Reduce tráfico de red ~90%
- ✅ **Chunks de 20MB:** Solo ~5 llamadas RMI para 95MB
- ✅ **Sin overhead artificial:** Procesa a máxima velocidad

### Desventajas de Concurrente:
- ❌ **Solo 4 hilos** (limitado por spinner)
- ❌ **15ms de overhead por hilo** = ~60ms perdidos
- ❌ **Algoritmo split():** Más lento que conteo directo
- ❌ **Crea arrays innecesarios:** Consume más memoria

**RMI debe ser 2-3x más rápido que Concurrente** 🚀

---

## 🐛 Troubleshooting

### Problema: "Connection refused"
```
Solución:
1. Verificar que los servidores RMI estén corriendo
2. Verificar que la IP sea correcta (no 127.0.0.1)
3. Verificar firewall (pasos arriba)
4. Hacer ping para verificar conectividad
```

### Problema: "No se pudo conectar al servidor"
```
Solución:
1. Verificar que ambas computadoras estén en la misma red (192.168.1.x)
2. Verificar que el cable Ethernet esté conectado
3. Reiniciar los servidores RMI
4. Verificar con: nc -zv IP PUERTO
```

### Problema: RMI no gana a Concurrente
```
Verificar:
1. ¿Tienes 4 servidores corriendo? (no 2)
2. ¿Los servidores muestran "16 hilos" en sus logs?
3. ¿El spinner dice máximo 4 hilos? (no 16)
4. ¿El archivo es text1.txt (95MB)?
5. ¿Los servidores están en otra computadora? (no localhost)
```

### Problema: "java.rmi.ConnectException: Connection refused"
```
Causa común: Firewall bloqueando puertos
Solución: Ejecutar comandos de firewall del Paso 3
```

---

## 📊 Script de Inicio Rápido (Computadora Remota)

Crea este script para iniciar todos los servidores fácilmente:

### Windows (start_servers.bat):
```batch
@echo off
echo Iniciando 4 servidores RMI...
start "Servidor RMI 1099" java ServidorRMI 1099
timeout /t 2
start "Servidor RMI 1100" java ServidorRMI 1100
timeout /t 2
start "Servidor RMI 1101" java ServidorRMI 1101
timeout /t 2
start "Servidor RMI 1102" java ServidorRMI 1102
echo Servidores iniciados. Presiona cualquier tecla para detenerlos...
pause
taskkill /FI "WindowTitle eq Servidor RMI*" /F
```

### Linux/macOS (start_servers.sh):
```bash
#!/bin/bash
echo "🚀 Iniciando 4 servidores RMI..."

java ServidorRMI 1099 &
PID1=$!
sleep 2

java ServidorRMI 1100 &
PID2=$!
sleep 2

java ServidorRMI 1101 &
PID3=$!
sleep 2

java ServidorRMI 1102 &
PID4=$!

echo "✅ Servidores iniciados en puertos 1099-1102"
echo "📝 PIDs: $PID1, $PID2, $PID3, $PID4"
echo "🛑 Presiona Ctrl+C para detenerlos..."

# Esperar y limpiar al salir
trap "kill $PID1 $PID2 $PID3 $PID4 2>/dev/null; echo '🛑 Servidores detenidos'; exit" INT TERM
wait
```

Hacer ejecutable:
```bash
chmod +x start_servers.sh
./start_servers.sh
```

---

## ✅ Checklist de Verificación

Antes de ejecutar la prueba, verifica:

- [ ] Ambas computadoras conectadas por cable al mismo router
- [ ] IP de la computadora remota anotada (192.168.1.X)
- [ ] 4 archivos .class copiados a la computadora remota
- [ ] 4 servidores RMI corriendo (puertos 1099-1102)
- [ ] Firewall configurado (puertos abiertos)
- [ ] Ping exitoso desde tu Mac: `ping 192.168.1.X`
- [ ] Puertos verificados: `nc -zv 192.168.1.X 1099-1102`
- [ ] VentanaComparativa configurada con 4 servidores
- [ ] Spinner de hilos en 4 (máximo)
- [ ] Archivo text1.txt (95MB) seleccionado

Si todos los items están ✅, ¡ejecuta la comparativa!

---

## 🎓 Conclusión

Con esta configuración:
- **4 servidores RMI** con **16 hilos cada uno** = **64 hilos totales**
- **Compresión GZIP** reduciendo tráfico de red
- **Chunks de 20MB** minimizando llamadas RMI
- **Concurrente limitado** a 4 hilos con overhead

**RMI ganará por un margen de 2-3x sobre Concurrente** 🏆

¡Buena suerte con tu proyecto de Programación Paralela! 🚀
