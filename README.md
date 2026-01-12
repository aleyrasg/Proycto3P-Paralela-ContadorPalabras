# 🚀 Comparativa de Procesamiento: Secuencial vs Concurrente vs Paralelo (RMI)

Sistema de comparación de rendimiento para el conteo de palabras en archivos de texto grandes, utilizando tres modos de procesamiento: Secuencial, Concurrente (multi-hilo local) y Paralelo (RMI distribuido).

## 📋 Descripción del Proyecto

Este proyecto demuestra las diferencias de rendimiento entre:

1. **Secuencial**: Un solo hilo procesando todo el archivo
2. **Concurrente**: Múltiples hilos locales (ExecutorService)
3. **Paralelo (RMI)**: Procesamiento distribuido en múltiples servidores remotos

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│  TU COMPUTADORA (Cliente)                                   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  VentanaComparativa (GUI)                              │ │
│  │  - Ejecuta Secuencial (1 hilo)                         │ │
│  │  - Ejecuta Concurrente (N hilos locales)               │ │
│  │  - Ejecuta Paralelo (envía a servidores RMI)           │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                         ↕️ Red (RMI)
┌─────────────────────────────────────────────────────────────┐
│  COMPUTADORA REMOTA (Servidores RMI)                        │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌───────────┐ │
│  │ Servidor 1 │ │ Servidor 2 │ │ Servidor 3 │ │ Servidor N│ │
│  │ Puerto 1099│ │ Puerto 1100│ │ Puerto 1101│ │ Puerto N  │ │
│  └────────────┘ └────────────┘ └────────────┘ └───────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 📦 Archivos del Proyecto

| Archivo | Descripción |
|---------|-------------|
| `VentanaComparativa.java` | GUI principal con la comparativa |
| `ProcesadorSecuencial.java` | Procesamiento en un solo hilo |
| `ProcesadorConcurrente.java` | Procesamiento multi-hilo local |
| `ClienteRMIOptimizado.java` | Cliente RMI con compresión y timeout |
| `ServidorRMI.java` | Servidor RMI (ejecutar en máquina remota) |
| `ContadorRemotoImpl.java` | Implementación del servicio RMI |
| `IContadorRemoto.java` | Interfaz remota RMI |
| `ConfiguracionServidor.java` | Configuración de servidores |
| `ResultadoProcesamiento.java` | Encapsulación de resultados |
| `run_gui.sh` | Script para ejecutar GUI (macOS/Linux) |
| `run_gui.bat` | Script para ejecutar GUI (Windows) |
| `start_servers.sh` | Script para iniciar servidores (macOS/Linux) |
| `start_servers.bat` | Script para iniciar servidores (Windows) |

## 🚀 Ejecución Rápida

### 1. Compilar
```bash
javac *.java
```

### 2. Iniciar Servidores RMI (en otra computadora o terminales separadas)
```bash
# Terminal 1
java ServidorRMI 1099

# Terminal 2
java ServidorRMI 1100
```

### 3. Ejecutar la GUI (⚠️ IMPORTANTE: Usar el script)
```bash
# macOS/Linux
./run_gui.sh

# Windows
run_gui.bat
```

> ⚠️ **NO** ejecutar `java VentanaComparativa` directamente. El archivo de texto es grande y necesita 8GB de memoria heap.

## 📊 Métricas de Rendimiento

### Speedup
- **Speedup > 1**: El modo es más rápido que secuencial ✅
- **Speedup = 1**: Igual que secuencial
- **Speedup < 1**: Más lento que secuencial ❌

### Eficiencia
- `Eficiencia = Speedup / Número de hilos (o servidores)`
- **100%** = Escalabilidad perfecta (ideal)
- **<100%** = Overhead por paralelización

## 🔧 Configuración de Servidores RMI

### En la GUI:
1. Click en **"⚙️ Configurar"**
2. Agregar servidores con:
   - **Host**: IP del servidor (ej: `192.168.1.100`)
   - **Puerto**: Puerto RMI (ej: `1099`)
   - **Nombre**: Identificador (ej: `Servidor-1`)
3. Click **"💾 Guardar"**

### Configuración por defecto:
- `localhost:1099` - Servidor-1
- `localhost:1100` - Servidor-2

## 🌐 Configuración en Red

### Para ejecutar servidores en otra computadora:

1. **Copiar archivos necesarios**:
   ```
   ServidorRMI.class
   ContadorRemotoImpl.class
   IContadorRemoto.class
   ResultadoProcesamiento.class
   ```

2. **Iniciar servidores** en la computadora remota:
   ```bash
   java ServidorRMI 1099
   java ServidorRMI 1100
   ```

3. **Verificar IP** mostrada por el servidor (debe ser `192.168.x.x`, NO `127.0.0.1`)

4. **Configurar firewall** para permitir puertos 1099-1102

5. **En la GUI**, configurar servidores con la IP de la computadora remota

## 📈 Resultados Esperados

Con un archivo de texto grande (~1GB):

| Modo | Descripción | Speedup Esperado |
|------|-------------|------------------|
| **Secuencial** | 1 hilo | 1x (baseline) |
| **Concurrente (4 hilos)** | 4 hilos locales | ~2-4x |
| **Paralelo (4 servidores RMI)** | 4 servidores remotos | ~2-4x |

> El speedup real depende del hardware, red, y tamaño del archivo.

## 🐛 Solución de Problemas

### Error: `OutOfMemoryError: Java heap space`
**Causa**: No hay suficiente memoria para procesar archivos grandes.
**Solución**: Usar `./run_gui.sh` en lugar de `java VentanaComparativa`

### Error: `Connection refused`
**Causa**: El servidor RMI no está corriendo o firewall bloquea.
**Solución**:
1. Verificar que el servidor está corriendo
2. Verificar la IP (no debe ser 127.0.0.1)
3. Abrir puertos en firewall

### Servidor muestra IP incorrecta
**Solución**: Forzar la IP correcta:
```bash
java -Djava.rmi.server.hostname=192.168.1.X ServidorRMI 1099
```

## 👥 Autores

Proyecto para la materia de Programación Paralela - 3er Parcial

## 📄 Licencia

Proyecto académico - Universidad
