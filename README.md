# Sistema de Gestión de Recolectores

Aplicación de escritorio en Java (Swing) para administrar recolectores agrícolas. Permite registrar trabajadores por lote, anotar pesajes diarios de cosecha, calcular y persistir liquidaciones de pago, y consultar reportes desde la misma interfaz.

---

## Características principales

- Gestión de **lotes** (sectores de cultivo): creación, edición, activación/desactivación.
- Gestión de **recolectores**: registro con cédula única, asignación a lote, estado activo/inactivo.
- Registro de **pesajes diarios**: cantidad de kilos por recolector, con trazabilidad del usuario que los anotó.
- Cálculo de **liquidaciones de pago**: por período y precio por kilo; almacenamiento persistente con detalle por recolector.
- **Reportes** en pantalla: por recolector, por lote, por día y de liquidaciones guardadas.
- **Autenticación** con bloqueo temporal tras intentos fallidos.

---

## Stack tecnológico

| Componente | Versión |
|---|---|
| Java | 17 |
| Interfaz gráfica | Swing (Look & Feel Nimbus) |
| ORM | Hibernate ORM 6.4.4.Final (Jakarta EE) |
| Base de datos | MySQL 8.x |
| Driver JDBC | MySQL Connector/J 8.3.0 |
| Build | Maven + maven-shade-plugin 3.5.1 (fat JAR) |

---

## Requisitos previos

- **Java 17** o superior instalado y en el `PATH`.
- **Maven 3.x** instalado.
- **MySQL 8.x** corriendo en `localhost:3306`.

---

## Instalación y ejecución

### 1. Crear la base de datos

Conectarse a MySQL y ejecutar el script de inicialización:

```sql
source src/main/resources/schema.sql
```

Esto crea la base de datos `recolectores_db`, todas las tablas e índices, e inserta el usuario administrador por defecto.

### 2. Configurar credenciales de MySQL (si difieren de `root`/`root123`)

Editar [src/main/resources/META-INF/persistence.xml](src/main/resources/META-INF/persistence.xml):

```xml
<property name="jakarta.persistence.jdbc.user"     value="TU_USUARIO"/>
<property name="jakarta.persistence.jdbc.password" value="TU_CONTRASEÑA"/>
```

### 3. Compilar y empaquetar

```bash
mvn clean package
```

### 4. Ejecutar

```bash
java -jar target/sistema-recolectores-1.0-SNAPSHOT.jar
```

---

## Credenciales por defecto

| Campo | Valor |
|---|---|
| Usuario | `admin` |
| Contraseña | `Admin1234` |

> Cambiar esta contraseña en producción ejecutando el hash SHA-256 del nuevo valor y actualizando el registro en la tabla `USUARIOS`.

---

## Módulos de la aplicación

| Pestaña | Módulo | Descripción |
|---|---|---|
| Lotes | `LotePanel` | Crear y gestionar lotes de cultivo |
| Recolectores | `RecolectorPanel` | Registrar y gestionar trabajadores |
| Pesajes | `PesajePanel` | Anotar pesajes diarios |
| Pagos | `PagoPanel` | Calcular y guardar liquidaciones |
| Reportes | `ReportePanel` | Consultar y eliminar reportes guardados |

---

## Estructura del proyecto

```
src/
  main/
    java/com/sistema/
      dao/          → Interfaces y implementaciones de repositorios
      exception/    → NegocioException (errores de dominio)
      model/        → Entidades JPA
      report/       → Generadores de reportes
      service/      → Lógica de negocio
      ui/           → Paneles Swing y ventanas
      util/         → ConexionBD, Validador, UIEstilo
    resources/
      META-INF/persistence.xml  → Configuración JPA/Hibernate
      schema.sql                → Script de inicialización de la BD
```

---

## Documentación adicional

- [MANUAL_TECNICO.md](MANUAL_TECNICO.md) — Arquitectura, entidades, servicios y guías para extender el sistema.
- [MANUAL_USUARIO.md](MANUAL_USUARIO.md) — Guía de uso paso a paso para usuarios finales.

---

## Autor

Jarol Herney Medina Perafan
