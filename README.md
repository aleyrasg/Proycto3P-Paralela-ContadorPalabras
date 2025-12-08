# 🚀 Sistema de Procesamiento Paralelo Distribuido con RMI

Sistema optimizado de procesamiento paralelo que distribuye el conteo de palabras en archivos de texto entre múltiples servidores RMI.

## ✨ Características Principales

### 🎯 Versión Optimizada (VentanaParalelaOptimizada)

- ✅ **Escalabilidad Dinámica**: Soporta N servidores configurables
- ✅ **Interfaz Avanzada**: Dashboard con métricas en tiempo real
- ✅ **Manejo Robusto de Errores**: Reintentos automáticos y failover
- ✅ **CompletableFuture**: Procesamiento asíncrono moderno
- ✅ **Monitoreo en Tiempo Real**: Progreso, estadísticas y velocidad
- ✅ **Configuración Dinámica**: Agregar/eliminar servidores desde GUI
- ✅ **Timeout Inteligente**: 30 segundos por servidor
- ✅ **Logs Detallados**: Timestamps y eventos completos

### 📊 Interfaz Mejorada

- **Barra de Progreso**: Visualización en tiempo real
- **Tabla de Servidores**: Estado, palabras procesadas, tiempo, velocidad
- **Panel de Configuración**: Gestión dinámica de servidores
- **Estadísticas**: Total de palabras, tiempo, velocidad promedio
- **Log con Timestamps**: Seguimiento detallado de eventos

## 🚀 Ejecución Rápida

### 1. Compilar
```bash
cd Proycto3P
javac *.java
```

### 2. Iniciar Servidores
**Terminal 1:**
```bash
java ServidorRMI 1099
```

**Terminal 2:**
```bash
java ServidorRMI 1100
```

### 3. Ejecutar Cliente Optimizado
```bash
java VentanaParalelaOptimizada
```

## 📖 Uso de la Interfaz

1. **Seleccionar Archivo**: Click en "📁 Seleccionar Archivo"
2. **Configurar Servidores** (opcional): Click en "⚙️ Configurar Servidores"
   - Agregar nuevos servidores
   - Eliminar servidores existentes
3. **Procesar**: Click en "⚡ Procesar Paralelo"
4. **Monitorear**: Ver progreso en tiempo real en tabla y log

## 🔧 Configuración de Servidores

### Agregar Servidor
1. Click en "⚙️ Configurar Servidores"
2. Click en "➕ Agregar"
3. Ingresar:
   - Host (ej: localhost, 192.168.1.10)
   - Puerto (ej: 1099)
   - Nombre (ej: Servidor-3)

### Servidores por Defecto
- Servidor-1: localhost:1099
- Servidor-2: localhost:1100

## 🏗️ Arquitectura

```
┌─────────────────────────────────┐
│  VentanaParalelaOptimizada      │
│  (Cliente con GUI Avanzada)     │
└────────────┬────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
┌───▼────┐      ┌────▼────┐
│Server 1│ ...  │Server N │
│  RMI   │      │   RMI   │
└────────┘      └─────────┘
```

## 📦 Componentes

### Clases Principales
- **VentanaParalelaOptimizada**: Interfaz gráfica avanzada
- **ClienteRMIOptimizado**: Cliente con reintentos y timeout
- **ConfiguracionServidor**: Configuración de servidores
- **ResultadoProcesamiento**: Encapsulación de resultados
- **ServidorRMI**: Servidor RMI (puerto configurable)
- **IContadorRemoto**: Interfaz remota
- **ContadorRemotoImpl**: Implementación del servicio

### Mejoras Implementadas

#### 1. Escalabilidad
- Soporte para N servidores (no limitado a 2)
- Balanceo automático de carga
- Configuración dinámica sin reiniciar

#### 2. Rendimiento
- CompletableFuture para procesamiento asíncrono
- Procesamiento paralelo real
- División inteligente del trabajo

#### 3. Confiabilidad
- Reintentos automáticos (3 intentos)
- Timeout de 30 segundos
- Manejo robusto de errores
- Logs detallados

#### 4. Experiencia de Usuario
- Interfaz moderna y profesional
- Progreso en tiempo real
- Estadísticas detalladas
- Configuración visual

## 📊 Métricas Mostradas

- **Total de Palabras**: Suma de todas las particiones
- **Tiempo Total**: Duración del procesamiento paralelo
- **Servidores Exitosos**: Cantidad de servidores que completaron
- **Velocidad Promedio**: Palabras procesadas por segundo
- **Por Servidor**:
  - Estado (Inactivo/Procesando/Completado/Error)
  - Palabras procesadas
  - Tiempo de procesamiento
  - Velocidad individual

## 🔄 Comparación de Versiones

| Característica | Versión Original | Versión Optimizada |
|----------------|------------------|-------------------|
| Servidores | 2 fijos | N configurables |
| Interfaz | Básica | Avanzada con métricas |
| Progreso | No | Barra en tiempo real |
| Reintentos | No | 3 intentos automáticos |
| Timeout | No | 30 segundos |
| Configuración | Hardcoded | Dinámica desde GUI |
| Threads | Thread básico | CompletableFuture |
| Estadísticas | Mínimas | Completas |

## 🎯 Casos de Uso

1. **Procesamiento de Logs**: Analizar archivos de log grandes
2. **Análisis de Texto**: Contar palabras en documentos extensos
3. **Big Data**: Procesar datasets distribuidos
4. **Benchmarking**: Comparar rendimiento de servidores

## 🛠️ Requisitos

- Java 11 o superior
- Múltiples terminales para servidores
- Archivos de texto para procesar

## 📝 Ejemplo de Salida

```
[10:30:45] 🚀 Iniciando procesamiento paralelo...
[10:30:45] 📄 Archivo: text1.txt
[10:30:45] 🖥️  Servidores activos: 2
[10:30:45] 📊 Total de líneas: 1000
[10:30:45] 📤 Enviando 500 líneas a Servidor-1
[10:30:45] 📤 Enviando 500 líneas a Servidor-2
[10:30:46] ✅ Servidor-1 completado: 2543 palabras en 1234 ms
[10:30:46] ✅ Servidor-2 completado: 2487 palabras en 1198 ms
[10:30:46] ==================================================
[10:30:46] ✅ PROCESAMIENTO COMPLETADO
[10:30:46] 📊 Total de palabras: 5030
[10:30:46] ⏱️  Tiempo total: 1250 ms
[10:30:46] 🖥️  Servidores exitosos: 2/2
[10:30:46] ⚡ Velocidad promedio: 4024 palabras/seg
[10:30:46] ==================================================
```

## 🚀 Próximas Mejoras Posibles

- [ ] Persistencia de configuración de servidores
- [ ] Gráficas de rendimiento en tiempo real
- [ ] Exportar resultados a CSV/JSON
- [ ] Soporte para múltiples archivos simultáneos
- [ ] Autenticación y seguridad RMI
- [ ] Monitoreo de recursos del servidor
- [ ] Balanceo de carga adaptativo

## 📄 Licencia

Proyecto educativo - Programación Paralela
