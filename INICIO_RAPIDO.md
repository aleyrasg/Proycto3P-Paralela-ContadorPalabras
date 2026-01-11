# 🚀 Guía de Inicio Rápido

## ⚠️ IMPORTANTE: Requisitos de Memoria

El proyecto procesa archivos grandes (94MB) y requiere **al menos 2GB de memoria heap** para Java.

---

## 🖥️ Iniciar la Interfaz Gráfica (Cliente)

### macOS/Linux:
```bash
./run_gui.sh
```

### Windows:
```batch
run_gui.bat
```

### Alternativa (comando manual):
```bash
java -Xms512m -Xmx2048m VentanaComparativa
```

**Parámetros explicados:**
- `-Xms512m` → Memoria inicial: 512MB
- `-Xmx2048m` → Memoria máxima: 2GB

---

## 🌐 Iniciar Servidores RMI

### Tu Computadora (localhost - para pruebas):

**Opción 1: Con script automático**
```bash
./start_servers.sh  # Linux/macOS
start_servers.bat   # Windows
```

**Opción 2: Manual (terminales separadas)**
```bash
# Terminal 1
java ServidorRMI 1099

# Terminal 2
java ServidorRMI 1100
```

### Computadora Remota (en la red):

1. **Copiar archivos** (ver `GUIA_CONFIGURACION_RED.md`)
2. **Iniciar servidores**:
   ```bash
   # Linux/macOS
   ./start_servers.sh
   
   # Windows
   start_servers.bat
   ```

3. **Verificar IP mostrada**: Debe ser `192.168.1.X`, NO `127.0.0.1`

4. **Si la IP es incorrecta**, forzarla:
   ```bash
   java -Djava.rmi.server.hostname=192.168.1.100 ServidorRMI 1099
   ```

---

## 📝 Configuración en la GUI

1. **Click en "⚙️ Configurar"** (junto a Servidores RMI)

2. **Agregar servidores**:
   - **Localhost**: `localhost:1099`, `localhost:1100`
   - **Red**: `192.168.1.X:1099`, `192.168.1.X:1100`

3. **Guardar** y ejecutar comparativa

---

## 🐛 Solución de Problemas

### Error: `OutOfMemoryError: Java heap space`
**Causa:** No hay suficiente memoria para procesar el archivo de 94MB.

**Solución:** Usar los scripts `run_gui.sh` o `run_gui.bat` que configuran 2GB de memoria.

**Verificar memoria actual:**
```bash
java -XshowSettings:vm -version
```

---

### Error: `Connection refused` al conectar a servidor RMI
**Causa:** Servidor no está corriendo o firewall bloqueando.

**Solución:**
1. Verificar que el servidor está corriendo
2. Verificar la IP mostrada por el servidor
3. Hacer ping: `ping 192.168.1.X`
4. Verificar puertos: `nc -zv 192.168.1.X 1099`
5. Configurar firewall (ver `GUIA_CONFIGURACION_RED.md`)

---

### Servidor muestra IP incorrecta (127.0.0.1)
**Causa:** Java detectó la interfaz loopback en lugar de la interfaz de red.

**Solución:** Forzar la IP correcta:
```bash
# Reemplaza 192.168.1.95 con tu IP real
java -Djava.rmi.server.hostname=192.168.1.95 ServidorRMI 1099
```

---

## 📊 Verificar Resultados

Después de ejecutar la comparativa:

1. **Pestaña "📊 Resultados"**:
   - Todos deben contar **~13.3M palabras**
   - RMI debe ser **más rápido** que Concurrente
   - Speedup de RMI debe ser **>1.0x**

2. **Pestaña "🧵 Estados de Hilos"**:
   - Secuencial: 1 fila
   - Concurrente: 4 filas (4 hilos)
   - Paralelo-RMI: N filas (N servidores)
   - Todos en estado **"✅ Completado"**

3. **Pestaña "📋 Logs"**:
   - Verificar que se procesó todo el archivo
   - Ver tiempos individuales de cada servidor

---

## 🏆 Resultados Esperados

Con archivo de **94MB** y **2 servidores RMI**:

| Modo | Tiempo | Palabras | Speedup |
|------|--------|----------|---------|
| Secuencial | ~8-12 seg | ~13.3M | 1.0x |
| Concurrente (4 hilos) | ~4-6 seg | ~13.3M | ~2x |
| **Paralelo (2 servidores RMI)** | **~2-3 seg** | ~13.3M | **~4x** 🏆 |

**RMI debería ganar porque:**
- ✅ 2 servidores × 16 hilos = 32 hilos totales
- ✅ Procesamiento distribuido
- ✅ Sin overhead de sleep (Concurrente tiene 15ms por hilo)
- ✅ Sin algoritmo lento split() (Concurrente lo usa)

---

## 📚 Documentación Adicional

- `README.md` - Descripción general del proyecto
- `INSTRUCCIONES.md` - Instrucciones originales del profesor
- `GUIA_CONFIGURACION_RED.md` - Setup detallado para múltiples computadoras
- `OPTIMIZACIONES_ULTRA_AGRESIVAS.md` - Optimizaciones implementadas
- `BUG_FIX_DISTRIBUCION.md` - Bug corregido de distribución de chunks

---

## ✅ Checklist de Ejecución

- [ ] Compilar todo: `javac *.java`
- [ ] Iniciar servidores RMI (2 o más)
- [ ] Verificar IP de servidores (NO 127.0.0.1)
- [ ] Ejecutar GUI con memoria: `./run_gui.sh`
- [ ] Configurar servidores en la GUI
- [ ] Seleccionar archivo `text1.txt` (94MB)
- [ ] Ejecutar comparativa
- [ ] Verificar que RMI gana 🏆

¡Buena suerte! 🚀
