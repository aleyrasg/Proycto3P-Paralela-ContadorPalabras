# 🌐 Guía de Configuración RMI en Red

## 📋 Requisitos Previos

1. Todos los dispositivos deben estar en la **misma red** (WiFi o LAN)
2. Java instalado en todos los dispositivos
3. Firewall configurado para permitir conexiones RMI

---

## 🖥️ Configuración por Dispositivo

### Dispositivo 1: Servidor RMI (Computadora A)

#### Paso 1: Obtener tu IP
**macOS/Linux:**
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

**Windows:**
```cmd
ipconfig
```

Ejemplo de salida: `192.168.1.100`

#### Paso 2: Configurar Firewall

**macOS:**
```bash
# Permitir Java en el firewall
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --add /usr/bin/java
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --unblockapp /usr/bin/java
```

**Windows:**
```
1. Panel de Control → Firewall de Windows
2. Configuración avanzada → Reglas de entrada
3. Nueva regla → Puerto → TCP → 1099 (y otros puertos que uses)
4. Permitir la conexión
```

**Linux:**
```bash
sudo ufw allow 1099/tcp
sudo ufw allow 1100/tcp
```

#### Paso 3: Iniciar Servidor
```bash
cd Proycto3P
java ServidorRMI 1099
```

Verás algo como:
```
═══════════════════════════════════════════════════════
✅ Servidor RMI iniciado correctamente
═══════════════════════════════════════════════════════
🌐 IP del servidor: 192.168.1.100
🔌 Puerto: 1099
📡 Esperando conexiones...
═══════════════════════════════════════════════════════

💡 Para conectar desde otro dispositivo:
   Host: 192.168.1.100
   Puerto: 1099
═══════════════════════════════════════════════════════
```

**¡IMPORTANTE!** Anota la IP que aparece (ej: 192.168.1.100)

---

### Dispositivo 2: Servidor RMI (Computadora B)

#### Paso 1: Obtener IP
Ejemplo: `192.168.1.101`

#### Paso 2: Configurar Firewall (igual que Dispositivo 1)

#### Paso 3: Copiar archivos necesarios
Copia estos archivos desde tu computadora principal:
- `IContadorRemoto.class`
- `ContadorRemotoImpl.class`
- `ServidorRMI.class`

O compila desde el código fuente:
```bash
javac IContadorRemoto.java ContadorRemotoImpl.java ServidorRMI.java
```

#### Paso 4: Iniciar Servidor
```bash
java ServidorRMI 1099
```

---

### Dispositivo 3: Cliente (Tu computadora principal)

#### Paso 1: Configurar servidores en la interfaz

1. Ejecuta el cliente:
```bash
java VentanaComparativa
```

2. Click en "⚙️ Configurar Servidores"

3. Elimina los servidores localhost

4. Agrega los servidores reales:

**Servidor 1:**
- Host: `192.168.1.100` (IP de Computadora A)
- Puerto: `1099`
- Nombre: `Servidor-A`

**Servidor 2:**
- Host: `192.168.1.101` (IP de Computadora B)
- Puerto: `1099`
- Nombre: `Servidor-B`

5. Click en "✅ Cerrar"

#### Paso 2: Probar conexión

1. Selecciona un archivo de texto
2. Click en "🚀 Ejecutar Comparativa Completa"
3. Observa el log para verificar conexiones exitosas

---

## 🔍 Verificar Conectividad

### Desde el Cliente, prueba la conexión:

**macOS/Linux:**
```bash
# Verificar que el servidor está escuchando
nc -zv 192.168.1.100 1099
```

**Windows:**
```cmd
# Verificar que el servidor está escuchando
telnet 192.168.1.100 1099
```

Si la conexión es exitosa, verás: `Connection to 192.168.1.100 port 1099 [tcp/*] succeeded!`

---

## 🐛 Solución de Problemas

### Error: "Connection refused"

**Causa:** El servidor no está corriendo o el firewall bloquea la conexión

**Solución:**
1. Verifica que el servidor esté corriendo
2. Verifica la IP correcta
3. Desactiva temporalmente el firewall para probar:
   - macOS: `sudo /usr/libexec/ApplicationFirewall/socketfilterfw --setglobalstate off`
   - Windows: Panel de Control → Firewall → Desactivar
   - Linux: `sudo ufw disable`

### Error: "No route to host"

**Causa:** Los dispositivos no están en la misma red

**Solución:**
1. Conecta todos los dispositivos a la misma WiFi
2. Verifica con `ping`:
   ```bash
   ping 192.168.1.100
   ```

### Error: "Connection timeout"

**Causa:** Firewall bloqueando o IP incorrecta

**Solución:**
1. Verifica la IP del servidor
2. Configura el firewall correctamente
3. Usa la IP local (192.168.x.x), no la IP pública

---

## 📊 Ejemplo de Configuración Completa

### Red Local:
```
Router WiFi (192.168.1.1)
    │
    ├─── Computadora A (192.168.1.100) → Servidor RMI puerto 1099
    │
    ├─── Computadora B (192.168.1.101) → Servidor RMI puerto 1099
    │
    └─── Computadora C (192.168.1.102) → Cliente (VentanaComparativa)
```

### Configuración en el Cliente:
```
Servidor-A: 192.168.1.100:1099
Servidor-B: 192.168.1.101:1099
```

---

## 🎯 Comandos Rápidos

### Iniciar Servidor (en cada computadora servidora):
```bash
cd Proycto3P
java ServidorRMI 1099
```

### Iniciar Cliente (en tu computadora principal):
```bash
cd Proycto3P
java VentanaComparativa
```

---

## 💡 Consejos

1. **Usa IPs estáticas** o anota las IPs dinámicas cada vez
2. **Mantén los servidores corriendo** mientras ejecutas pruebas
3. **Verifica el log del servidor** para ver las conexiones entrantes
4. **Usa archivos pequeños** (<10MB) para las primeras pruebas
5. **Prueba primero con 2 dispositivos** antes de agregar más

---

## 🔐 Seguridad

⚠️ **IMPORTANTE:** RMI sin seguridad adicional NO debe usarse en redes públicas.

Para producción, considera:
- Usar SSL/TLS
- Implementar autenticación
- Usar VPN para conexiones remotas
- Configurar políticas de seguridad Java

---

## 📞 Verificación Final

Antes de ejecutar la comparativa completa:

✅ Todos los servidores muestran su IP y puerto  
✅ El cliente puede hacer ping a todas las IPs  
✅ Los firewalls permiten el puerto 1099  
✅ Todos están en la misma red WiFi/LAN  
✅ Los servidores muestran "Esperando conexiones..."  

Si todo está ✅, ¡estás listo para procesar en paralelo distribuido! 🚀
