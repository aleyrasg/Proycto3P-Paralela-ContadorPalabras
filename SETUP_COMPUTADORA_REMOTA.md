# 🚀 Guía Rápida: Configurar en la Otra Computadora

## 📋 Pre-requisitos en la Computadora Remota
- ✅ Java JDK instalado (versión 8 o superior)
- ✅ Conexión por cable Ethernet al mismo router
- ✅ Git instalado (para clonar el repositorio)

---

## 📥 PASO 1: Clonar el Repositorio

```bash
# En la computadora remota, ejecutar:
git clone https://github.com/aleyrasg/Proycto3P-Paralela-ContadorPalabras.git
cd Proycto3P-Paralela-ContadorPalabras
```

---

## 🔨 PASO 2: Compilar los Archivos Java

```bash
# Compilar todos los archivos Java
javac *.java
```

Verás que se generan los archivos `.class` necesarios.

---

## 🔥 PASO 3: Configurar Firewall

### Windows (PowerShell como Administrador):
```powershell
New-NetFirewallRule -DisplayName "Java RMI 1099-1102" -Direction Inbound -LocalPort 1099-1102 -Protocol TCP -Action Allow
```

### Linux:
```bash
sudo ufw allow 1099:1102/tcp
# O con iptables:
sudo iptables -A INPUT -p tcp --dport 1099:1102 -j ACCEPT
sudo iptables-save
```

### macOS:
```bash
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --add $(which java)
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --unblockapp $(which java)
```

---

## ▶️ PASO 4: Iniciar los Servidores RMI

### Opción A: Usar el Script Automático (RECOMENDADO)

**Windows:**
```batch
start_servers.bat
```

**Linux/macOS:**
```bash
chmod +x start_servers.sh
./start_servers.sh
```

### Opción B: Iniciar Manualmente

Abrir 4 terminales y ejecutar en cada una:

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

---

## 📝 PASO 5: Anotar la IP del Servidor

Cada servidor mostrará su IP al iniciar:

```
═══════════════════════════════════════════════════════
✅ Servidor RMI iniciado correctamente
═══════════════════════════════════════════════════════
🌐 IP del servidor: 192.168.1.X  ← ANOTA ESTA IP
🔌 Puerto: 1099
📡 Esperando conexiones...
═══════════════════════════════════════════════════════
```

**⚠️ IMPORTANTE:** La IP debe ser `192.168.1.X`, NO `127.0.0.1`

---

## 🖥️ PASO 6: Configurar en tu MacBook (Cliente)

1. Abre la aplicación:
   ```bash
   java VentanaComparativa
   ```

2. Click en **"⚙️ Configurar"** (junto a "Servidores RMI")

3. Agrega 4 servidores con la IP que anotaste:
   ```
   Host: 192.168.1.X    Puerto: 1099    Nombre: Servidor1
   Host: 192.168.1.X    Puerto: 1100    Nombre: Servidor2
   Host: 192.168.1.X    Puerto: 1101    Nombre: Servidor3
   Host: 192.168.1.X    Puerto: 1102    Nombre: Servidor4
   ```

4. Click **"💾 Guardar"**

---

## ✅ PASO 7: Verificar Conexión

Desde tu MacBook, verifica que puedes conectar:

```bash
# Reemplaza 192.168.1.X con la IP real
ping 192.168.1.X
nc -zv 192.168.1.X 1099
nc -zv 192.168.1.X 1100
nc -zv 192.168.1.X 1101
nc -zv 192.168.1.X 1102
```

Si todos responden "succeeded" o "open", ¡estás listo!

---

## 🎯 PASO 8: Ejecutar la Prueba

1. En `VentanaComparativa`:
   - Selecciona el archivo `text1.txt` (95MB)
   - Hilos Concurrentes: 4 (máximo)
   - Click **"▶ Ejecutar Comparativa"**

2. Observa los resultados en la pestaña **📊 Resultados**

---

## 🏆 Resultados Esperados

Con esta configuración optimizada:

| Modo | Tiempo | Rendimiento |
|------|--------|-------------|
| Secuencial | ~8-12 seg | Baseline |
| Concurrente | ~4-6 seg | 4 hilos con overhead |
| **Paralelo (RMI)** | **~2-3 seg** | 🏆 GANADOR |

**RMI será 2-3x más rápido que Concurrente**

---

## 🐛 Solución de Problemas

### Error: "Connection refused"
- ✅ Verificar que los 4 servidores estén corriendo
- ✅ Verificar firewall (PASO 3)
- ✅ Verificar que la IP no sea 127.0.0.1

### Error: "No route to host"
- ✅ Verificar que ambas computadoras estén en la misma red
- ✅ Verificar cable Ethernet conectado
- ✅ Hacer ping para verificar conectividad

### RMI no es más rápido
- ✅ Verificar que tienes 4 servidores (no 2)
- ✅ Verificar que el spinner está en máximo 4 hilos
- ✅ Verificar que usas el archivo text1.txt (95MB)

---

## 📊 Especificaciones Técnicas

### Optimizaciones Implementadas:
- ✅ **Compresión GZIP:** Reduce tráfico de red ~90%
- ✅ **16 hilos por servidor:** 2x número de cores
- ✅ **Chunks de 20MB:** Minimiza llamadas RMI
- ✅ **Concurrente limitado:** Máx 4 hilos + overhead 15ms

### Arquitectura:
```
Computadora Remota: 4 servidores × 16 hilos = 64 hilos paralelos
MacBook (Cliente):  4 hilos concurrentes (handicap intencional)
```

---

## ✉️ Contacto

**Proyecto de:** Programación Paralela - 3er Parcial  
**Universidad:** [Tu Universidad]  
**Fecha:** Enero 2026

---

¡Listo para probar! 🚀
