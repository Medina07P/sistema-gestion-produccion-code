# Manual de Usuario — Sistema de Gestión de Recolectores

Este documento explica cómo usar la aplicación paso a paso. No se requieren conocimientos técnicos.

---

## Tabla de contenidos

1. [Introducción](#1-introducción)
2. [Cómo iniciar la aplicación](#2-cómo-iniciar-la-aplicación)
3. [Pantalla principal](#3-pantalla-principal)
4. [Gestión de Lotes](#4-gestión-de-lotes)
5. [Gestión de Recolectores](#5-gestión-de-recolectores)
6. [Registro de Pesajes](#6-registro-de-pesajes)
7. [Cálculo de Pagos y Liquidación](#7-cálculo-de-pagos-y-liquidación)
8. [Reportes](#8-reportes)
9. [Resolución de problemas comunes](#9-resolución-de-problemas-comunes)

---

## 1. Introducción

El **Sistema de Gestión de Recolectores** es una aplicación para administrar el trabajo diario en la cosecha agrícola. Con ella se puede:

- Organizar a los recolectores por **lotes** (sectores de cultivo).
- Registrar los **kilos recolectados** cada día por cada trabajador.
- Calcular y guardar la **liquidación de pagos** al final de un período.
- Consultar **reportes** de producción por recolector, lote, día o por liquidaciones guardadas.

**Requisito mínimo del equipo**: Java 17 instalado. El servidor de base de datos MySQL debe estar en funcionamiento antes de abrir la aplicación.

---

## 2. Cómo iniciar la aplicación

### Abrir la aplicación

Haga doble clic sobre el archivo `sistema-recolectores-1.0-SNAPSHOT.jar`, o desde la terminal:

```
java -jar sistema-recolectores-1.0-SNAPSHOT.jar
```

### Pantalla de inicio de sesión

Al abrir la aplicación aparece la ventana de login:

1. Ingrese su **nombre de usuario** y **contraseña**.
2. Haga clic en **Iniciar sesión**.

> **Credenciales por defecto**: usuario `admin`, contraseña `Admin1234`.

**Intentos fallidos**: Si ingresa una contraseña incorrecta 3 veces seguidas, el botón de inicio de sesión se bloqueará durante **30 segundos**. Espere y vuelva a intentarlo.

Si cierra la ventana de login sin autenticarse, la aplicación se cerrará.

---

## 3. Pantalla principal

Después de iniciar sesión se muestra la ventana principal con cinco pestañas en la parte superior:

| Pestaña | Función |
|---|---|
| Lotes | Gestionar los sectores de cultivo |
| Recolectores | Registrar y gestionar trabajadores |
| Pesajes | Anotar los kilos recolectados por día |
| Pagos | Calcular y guardar liquidaciones de pago |
| Reportes | Consultar información histórica |

En la esquina superior derecha aparece el nombre del usuario que inició sesión.

### Salir de la aplicación

Cierre la ventana haciendo clic en la **X** de la esquina superior derecha. Aparecerá una confirmación; haga clic en **Sí** para salir.

---

## 4. Gestión de Lotes

Un **lote** es un sector o zona de cultivo a la que se asignan recolectores. Vaya a la pestaña **Lotes**.

### Crear un lote

1. Complete el campo **Código** (ejemplo: `A1`, `B2`).
   - Solo se permiten letras y números.
   - Máximo 10 caracteres.
   - Se guardará en **MAYÚSCULAS** automáticamente.
2. Escriba una **Descripción** opcional (ejemplo: "Lote norte - Sector 1").
3. Haga clic en **Guardar** (o el botón equivalente).

Si el código ya existe, aparecerá un mensaje de error indicando que debe usar uno diferente.

### Editar un lote

1. Seleccione el lote en la tabla.
2. Modifique los campos deseados.
3. Haga clic en **Actualizar**.

### Activar o desactivar un lote

- Un lote **desactivado** no aparecerá disponible al registrar pesajes ni al crear recolectores.
- Para cambiar el estado, seleccione el lote y haga clic en **Activar** o **Desactivar** según corresponda.

> **Nota**: Antes de desactivar un lote, asegúrese de que no tenga recolectores activos asignados, ya que no podrán registrar pesajes mientras el lote esté inactivo.

---

## 5. Gestión de Recolectores

Un **recolector** es un trabajador de la cosecha. Vaya a la pestaña **Recolectores**.

### Registrar un recolector

1. Ingrese el **Nombre completo** del trabajador.
2. Ingrese la **Cédula** (solo números, entre 5 y 15 dígitos).
3. Seleccione el **Lote** al que pertenece (opcional).
4. Haga clic en **Guardar**.

Si la cédula ya está registrada, aparecerá un mensaje de error. Verifique si el recolector ya existe en el sistema.

### Editar un recolector

1. Seleccione al recolector en la tabla.
2. Modifique los campos necesarios.
3. Haga clic en **Actualizar**.

### Activar o desactivar un recolector

- Un recolector **desactivado** no aparecerá en la lista al registrar pesajes.
- Seleccione al recolector y haga clic en **Activar** o **Desactivar**.

---

## 6. Registro de Pesajes

Un **pesaje** es el registro de los kilos que un recolector entregó en un día determinado. Vaya a la pestaña **Pesajes**.

### Registrar un pesaje

1. Seleccione el **Recolector** en la lista desplegable.
   - Solo aparecen recolectores **activos**.
2. Ingrese la **Fecha** en formato `AAAA-MM-DD` (ejemplo: `2025-06-15`).
   - No se permite ingresar fechas futuras.
3. Ingrese los **Kilos** recolectados (debe ser mayor a 0). Se acepta punto o coma como separador decimal.
4. Haga clic en **Registrar** (o el botón equivalente).

### Requisitos para registrar un pesaje

- El recolector debe estar **activo**.
- El lote al que pertenece el recolector también debe estar **activo**.
- La fecha no puede ser posterior a hoy.
- Los kilos deben ser un número positivo.

Si alguna condición no se cumple, aparecerá un mensaje indicando qué corregir.

### Ver pesajes registrados

La tabla en la parte inferior muestra todos los pesajes. Puede ordenar y revisar los registros existentes.

---

## 7. Cálculo de Pagos y Liquidación

Al final de un período de cosecha, se calcula el pago de cada recolector según los kilos acumulados. Vaya a la pestaña **Pagos**.

### Calcular los pagos

1. Ingrese la **Fecha de inicio** del período (formato `AAAA-MM-DD`).
2. Ingrese la **Fecha de fin** del período.
3. Ingrese el **Precio por kilo** (en la moneda local). Se acepta punto o coma decimal.
4. Haga clic en **Calcular**.

La tabla mostrará cada recolector con:
- Total de kilos acumulados en el período.
- Total a pagar (kilos × precio por kilo).

Los resultados se ordenan de mayor a menor pago.

> **Importante**: El precio se aplica al momento de liquidar, no cuando se registran los pesajes. Esto permite registrar pesajes durante todo el período y fijar el precio al final.

### Guardar la liquidación

Si los resultados son correctos:

1. Haga clic en **Guardar liquidación** (o el botón equivalente).
2. Se almacenará un registro permanente en el sistema con la fecha, período, precio y el detalle por recolector.

Una liquidación guardada puede consultarse posteriormente en la pestaña **Reportes**.

> **Nota**: Debe calcular primero antes de poder guardar. Si cierra el panel sin guardar, los cálculos se perderán.

---

## 8. Reportes

La pestaña **Reportes** permite consultar información histórica desde distintos ángulos.

### Tipos de reporte disponibles

| Reporte | Muestra |
|---|---|
| Por Recolector | Total de kilos y días trabajados por cada recolector en el período |
| Por Lote | Total de kilos producidos por cada lote en el período |
| Por Día | Total de kilos registrados por fecha |
| Liquidaciones | Historial de liquidaciones guardadas con detalle por recolector |

### Consultar un reporte

1. Seleccione el tipo de reporte en el menú desplegable.
2. Ingrese la **Fecha de inicio** y **Fecha de fin** del período a consultar.
3. Haga clic en **Generar** (o el botón equivalente).

Los resultados aparecerán en la tabla.

### Eliminar una liquidación

Solo el reporte de **Liquidaciones** permite eliminar registros.

1. Seleccione la fila de la liquidación que desea eliminar.
2. Haga clic en **Eliminar**.
3. Confirme la acción en el diálogo que aparece.

> **Advertencia**: La eliminación es permanente y no se puede deshacer.

---

## 9. Resolución de problemas comunes

### La aplicación no abre o muestra un error al iniciar

**Causa probable**: MySQL no está corriendo o la base de datos no existe.

**Solución**:
1. Verifique que el servicio de MySQL esté activo en su equipo.
2. Confirme que la base de datos `recolectores_db` existe (ejecute `schema.sql` si aún no lo ha hecho).
3. Verifique que el usuario y contraseña configurados en `persistence.xml` sean correctos.

---

### Aparece el mensaje "Recolector inactivo"

**Causa**: El recolector seleccionado está marcado como inactivo.

**Solución**: Vaya a la pestaña **Recolectores**, seleccione al trabajador y haga clic en **Activar**.

---

### Aparece el mensaje "Lote inactivo"

**Causa**: El lote al que pertenece el recolector está desactivado.

**Solución**: Vaya a la pestaña **Lotes**, seleccione el lote y haga clic en **Activar**.

---

### Aparece el mensaje "Cédula duplicada"

**Causa**: Ya existe un recolector registrado con esa cédula.

**Solución**: Busque en la tabla de recolectores si el trabajador ya está en el sistema. Si está desactivado, actívelo en lugar de crear un registro nuevo.

---

### Aparece el mensaje "No hay pesajes registrados en el período"

**Causa**: No existen pesajes entre las fechas ingresadas al intentar calcular pagos.

**Solución**: Verifique que las fechas son correctas y que existen pesajes registrados en ese rango. Revise la pestaña **Pesajes** para confirmarlo.

---

### El botón de login está bloqueado

**Causa**: Se ingresaron 3 contraseñas incorrectas consecutivamente.

**Solución**: Espere 30 segundos. El botón se habilitará automáticamente.
