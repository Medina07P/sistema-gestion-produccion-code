# Manual Técnico — Sistema de Gestión de Recolectores

Versión: 1.0 | Java 17 | Hibernate 6.4.4 | MySQL 8.x

---

## Tabla de contenidos

1. [Arquitectura general](#1-arquitectura-general)
2. [Modelo de datos](#2-modelo-de-datos)
3. [Capa DAO](#3-capa-dao)
4. [Capa de servicios](#4-capa-de-servicios)
5. [Capa UI](#5-capa-ui)
6. [Reportes](#6-reportes)
7. [Utilidades y manejo de errores](#7-utilidades-y-manejo-de-errores)
8. [Configuración y despliegue](#8-configuración-y-despliegue)
9. [Cómo extender el sistema](#9-cómo-extender-el-sistema)

---

## 1. Arquitectura general

El sistema sigue una **arquitectura de 3 capas** estricta, sin framework de inyección de dependencias. El cableado se hace manualmente en el constructor de `MainFrame`.

```
┌─────────────────────────────────────────────────────────────┐
│                    Capa UI (Swing)                           │
│  LoginFrame  MainFrame  *Panel  ReportePanel                │
└───────────────────────┬─────────────────────────────────────┘
                        │ llama a interfaces de servicio
┌───────────────────────▼─────────────────────────────────────┐
│                  Capa de Servicios                           │
│  ILoteService  IRecolectorService  IPesajeService            │
│  IPagoService  IUsuarioService                               │
└───────────────────────┬─────────────────────────────────────┘
                        │ llama a interfaces DAO
┌───────────────────────▼─────────────────────────────────────┐
│                    Capa DAO                                  │
│  ILoteDAO  IRecolectorDAO  IPesajeDAO                        │
│  ILiquidacionDAO  IUsuarioDAO                                │
└───────────────────────┬─────────────────────────────────────┘
                        │ usa EntityManager
┌───────────────────────▼─────────────────────────────────────┐
│         Hibernate 6 / Jakarta Persistence (JPA)             │
│         MySQL 8.x  ←  ConexionBD (singleton EMF)            │
└─────────────────────────────────────────────────────────────┘
```

### Wiring manual de dependencias

Todo el grafo de objetos se construye en `MainFrame` ([src/main/java/com/sistema/ui/MainFrame.java](src/main/java/com/sistema/ui/MainFrame.java)):

```
// DAOs
ILoteDAO          loteDAO          = new LoteDAOImpl();
IRecolectorDAO    recolectorDAO    = new RecolectorDAOImpl();
IPesajeDAO        pesajeDAO        = new PesajeDAOImpl();
ILiquidacionDAO   liquidacionDAO   = new LiquidacionDAOImpl();

// Servicios
ILoteService       loteService       = new LoteService(loteDAO);
IRecolectorService recolectorService = new RecolectorService(recolectorDAO, loteDAO);
IPesajeService     pesajeService     = new PesajeService(pesajeDAO, recolectorDAO);
IPagoService       pagoService       = new PagoService(pesajeDAO, liquidacionDAO);
```

No se usa Spring, CDI ni ningún contenedor IoC. Cada interfaz tiene exactamente una implementación concreta.

### Por qué `jakarta.persistence.*`

Hibernate 6 abandonó `javax.persistence` y migró a `jakarta.persistence` (Jakarta EE 9+). Cualquier import `javax.persistence.*` en código nuevo no compilará con Hibernate 6.

---

## 2. Modelo de datos

### Entidades JPA

Todas las entidades residen en [src/main/java/com/sistema/model/](src/main/java/com/sistema/model/).

| Clase | Tabla | Propósito |
|---|---|---|
| `Usuario` | `USUARIOS` | Usuario del sistema; contraseña en SHA-256 hex |
| `Lote` | `LOTES` | Agrupación de recolectores; código único en mayúsculas |
| `Recolector` | `RECOLECTORES` | Trabajador; cédula única; pertenece a un `Lote` (nullable) |
| `Pesaje` | `PESAJES` | Registro diario de kilos; FK a `Recolector` y `Usuario` |
| `Liquidacion` | `LIQUIDACIONES` | Cabecera de liquidación: período, precio, usuario |
| `LiquidacionDetalle` | `LIQUIDACION_DETALLE` | Un registro por recolector dentro de una liquidación |
| `Pago` | _(ninguna)_ | DTO en memoria; **no es `@Entity`**; solo usado en `PagoPanel` |

### Diagrama de relaciones

```
USUARIOS ────────────────────────────────────┐
    │                                         │
    │ (registra)                              │ (registra)
    ▼                                         ▼
PESAJES ──────────────► RECOLECTORES ◄──── LIQUIDACION_DETALLE
                              │                     │
                              │ ManyToOne            │ ManyToOne
                              ▼                     │
                           LOTES           LIQUIDACIONES
                                                    │
                                          FK usuario │
                                                    ▼
                                               USUARIOS
```

### Notas clave

- `Recolector.lote` es nullable (un recolector puede no estar asignado a ningún lote).
- El lote de un pesaje se obtiene **en tiempo de ejecución** via `pesaje.getRecolector().getLote()`, no se almacena en `PESAJES`.
- `Liquidacion` tiene `@OneToMany(cascade = ALL)` sobre `LiquidacionDetalle`: guardar la cabecera persiste automáticamente todos los detalles.
- `Pago` es un objeto DTO construido en `PagoService.calcularPagos(...)` y descartado al cerrar el panel; nunca toca la base de datos.

---

## 3. Capa DAO

Ubicación: [src/main/java/com/sistema/dao/](src/main/java/com/sistema/dao/)
Implementaciones: [src/main/java/com/sistema/dao/impl/](src/main/java/com/sistema/dao/impl/)

### Jerarquía

```
IGenericDAO<T, ID>
    ├─ guardar(T)
    ├─ actualizar(T)
    ├─ eliminar(ID)
    ├─ buscarPorId(ID) → Optional<T>
    └─ listarTodos() → List<T>

    implementado por → GenericDAOImpl<T, ID>
```

Cada DAO específico extiende `IGenericDAO` y puede agregar métodos de consulta propios:

| Interfaz | Implementación | Métodos adicionales notables |
|---|---|---|
| `IUsuarioDAO` | `UsuarioDAOImpl` | `buscarPorUsername(String)` |
| `ILoteDAO` | `LoteDAOImpl` | `buscarPorCodigo(String)`, `listarActivos()` |
| `IRecolectorDAO` | `RecolectorDAOImpl` | `buscarPorCedula(String)`, `listarActivos()` |
| `IPesajeDAO` | `PesajeDAOImpl` | `buscarPorPeriodo(LocalDate, LocalDate)` |
| `ILiquidacionDAO` | `LiquidacionDAOImpl` | `listarTodas()` |

Todos los DAOs obtienen un `EntityManager` fresco por operación desde `ConexionBD.getEntityManager()`.

---

## 4. Capa de servicios

Ubicación: [src/main/java/com/sistema/service/](src/main/java/com/sistema/service/)

Los servicios encapsulan **toda la lógica de negocio**. Usan `Validador` para validaciones y lanzan `NegocioException` para señalar errores de reglas de negocio.

### `IUsuarioService` / `UsuarioServiceImpl`

- `autenticar(username, password) → Usuario`: aplica SHA-256 hex a la contraseña ingresada y compara con el valor almacenado. Lanza `NegocioException` si el usuario no existe, está inactivo o la contraseña no coincide.

### `ILoteService` / `LoteService`

- `crear(codigo, descripcion)`: código en MAYÚSCULAS, máximo 10 caracteres, único en la BD.
- `actualizar(id, codigo, descripcion)`: mismas validaciones.
- `activar(id)` / `desactivar(id)`: soft-delete via campo `activo`.
- `listarTodos()` / `listarActivos()` / `buscarPorId(id)`.

### `IRecolectorService` / `RecolectorService`

- `crear(nombre, cedula, loteId)`: cédula válida (`[0-9]{5,15}`), única en la BD; nombre obligatorio, máximo 100 caracteres; lote opcional.
- `actualizar(id, nombre, cedula, loteId)`: mismas validaciones.
- `activar(id)` / `desactivar(id)`: soft-delete.
- `listar()` / `buscarPorId(id)`.

### `IPesajeService` / `PesajeService`

- `registrar(recolectorId, fechaStr, kilosStr, usuario)`:
  1. Parsea `fechaStr` con formato `AAAA-MM-DD`.
  2. Valida que la fecha no sea futura.
  3. Valida que los kilos sean > 0.
  4. Verifica que el recolector y su lote estén activos.
  5. Persiste `new Pesaje(recolector, fecha, kilos, usuario)`.
- `eliminar(id)`.
- `listarTodos()`.

### `IPagoService` / `PagoService`

Archivo: [src/main/java/com/sistema/service/PagoService.java](src/main/java/com/sistema/service/PagoService.java)

- `calcularPagos(inicio, fin, precioPorKilo) → List<Pago>`:
  1. Valida precio > 0 y rango de fechas válido.
  2. Obtiene todos los pesajes del período.
  3. Agrupa kilos por recolector en un `LinkedHashMap<Recolector, Double>`.
  4. Construye un `Pago` por recolector con fórmula: `totalPago = totalKilos * precioPorKilo`.
  5. Devuelve la lista ordenada de mayor a menor pago.

- `guardarLiquidacion(inicio, fin, precioPorKilo, pagos, usuario)`:
  1. Valida que haya pagos calculados y usuario no nulo.
  2. Crea `Liquidacion` (cabecera) + lista de `LiquidacionDetalle` (uno por recolector).
  3. Asigna los detalles a la cabecera y llama a `liquidacionDAO.guardar(liq)` — la cascada persiste los detalles automáticamente.

- `eliminarLiquidacion(id)`: delega en `liquidacionDAO.eliminar(id)`.

---

## 5. Capa UI

Ubicación: [src/main/java/com/sistema/ui/](src/main/java/com/sistema/ui/)

| Clase | Tipo | Descripción |
|---|---|---|
| `LoginFrame` | `JDialog` | Modal de autenticación; 3 intentos fallidos → bloqueo 30 s |
| `MainFrame` | `JFrame` | Ventana principal; contiene el `JTabbedPane` con todas las pestañas |
| `LotePanel` | `JPanel` | CRUD de lotes |
| `RecolectorPanel` | `JPanel` | CRUD de recolectores; lista lotes disponibles |
| `PesajePanel` | `JPanel` | Registro y listado de pesajes diarios |
| `PagoPanel` | `JPanel` | Cálculo de pagos y guardado de liquidaciones |
| `ReportePanel` | `JPanel` | Visualización polimórfica de reportes vía `IReporteGenerador` |

### Flujo de inicio

```
main()
  └─ LoginFrame.setVisible(true)  ← bloquea hasta autenticación o cierre
       └─ getUsuarioAutenticado()
            ├─ null → System.exit(0)
            └─ Usuario → new MainFrame(nombre, usuario).setVisible(true)
```

### Recarga de datos entre pestañas

`MainFrame` agrega un `ChangeListener` al `JTabbedPane`:
- Al activar la pestaña **Recolectores** → llama a `recolectorPanel.refrescar()`.
- Al activar la pestaña **Pesajes** → llama a `pesajePanel.refrescar()`.

Esto asegura que los combos de selección siempre muestren datos actualizados.

---

## 6. Reportes

Ubicación: [src/main/java/com/sistema/report/](src/main/java/com/sistema/report/)

Los reportes **no usan iText, JasperReports ni Apache POI**. Se muestran directamente en una `JTable` dentro de `ReportePanel`.

### Interfaz `IReporteGenerador`

```java
String getNombre();                      // título en el combo selector
String[] getColumnasEncabezado();        // encabezados de la tabla
List<Object[]> generarFilas(LocalDate inicio, LocalDate fin, Usuario usuario);
boolean soportaEliminar();               // habilita el botón "Eliminar"
void eliminar(Long id);                  // solo si soportaEliminar() == true
```

### Implementaciones disponibles

| Clase | Columnas principales | Soporta eliminar |
|---|---|---|
| `ReportePorRecolector` | Recolector, Cédula, Total Kilos, Días | No |
| `ReportePorLote` | Lote, Total Kilos, Recolectores activos | No |
| `ReportePorDia` | Fecha, Total Kilos, Cantidad de pesajes | No |
| `ReporteLiquidaciones` | ID, Período, Precio x Kilo, Recolector, Total Kilos, Total Pago | Sí |

`ReportePanel` consume cualquier implementación de forma polimórfica: al cambiar el reporte seleccionado en el combo reconstruye la tabla con los encabezados correspondientes.

---

## 7. Utilidades y manejo de errores

### `ConexionBD` ([src/main/java/com/sistema/util/ConexionBD.java](src/main/java/com/sistema/util/ConexionBD.java))

Singleton que gestiona el `EntityManagerFactory`. Se inicializa la primera vez que se llama a `getEntityManager()`. `ConexionBD.cerrar()` se invoca al salir de la aplicación para liberar recursos.

Si MySQL no está disponible al arrancar, la excepción se propaga hasta `main()` y la aplicación muestra un mensaje de error y termina. No hay reintentos.

### `Validador` ([src/main/java/com/sistema/util/Validador.java](src/main/java/com/sistema/util/Validador.java))

Clase no instanciable con métodos estáticos de validación. Todos lanzan `NegocioException` con mensajes orientados al usuario final.

| Método | Comportamiento |
|---|---|
| `requerido(valor, campo)` | Falla si `null` o vacío |
| `longitudMaxima(valor, max, campo)` | Falla si supera `max` caracteres |
| `positivo(valor, campo)` | Falla si ≤ 0 |
| `parsearDouble(texto, campo)` | Acepta coma o punto decimal |
| `parsearFecha(texto, campo)` | Formato ISO `AAAA-MM-DD` |
| `rangoFechas(inicio, fin)` | Falla si `inicio` > `fin` |
| `cedula(cedula)` | Regex `[0-9]{5,15}` |

### `NegocioException` ([src/main/java/com/sistema/exception/NegocioException.java](src/main/java/com/sistema/exception/NegocioException.java))

`RuntimeException` sin checked. Los servicios la lanzan para señalar violaciones de reglas de negocio. Los paneles la capturan con un bloque `catch (NegocioException e)` y muestran el mensaje en `JOptionPane.showMessageDialog`.

### `UIEstilo` ([src/main/java/com/sistema/util/UIEstilo.java](src/main/java/com/sistema/util/UIEstilo.java))

Constantes estáticas de estilo Swing: colores del encabezado, fuentes, fondo de paneles. Centraliza la apariencia visual sin necesidad de modificar cada panel individualmente.

---

## 8. Configuración y despliegue

### `persistence.xml` ([src/main/resources/META-INF/persistence.xml](src/main/resources/META-INF/persistence.xml))

```xml
<persistence-unit name="sistemaRecolectoresPU" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

    <!-- Entidades registradas — OBLIGATORIO agregar aquí cualquier nueva @Entity -->
    <class>com.sistema.model.Lote</class>
    <class>com.sistema.model.Recolector</class>
    <class>com.sistema.model.Pesaje</class>
    <class>com.sistema.model.Usuario</class>
    <class>com.sistema.model.Liquidacion</class>
    <class>com.sistema.model.LiquidacionDetalle</class>

    <properties>
        <property name="jakarta.persistence.jdbc.url"
                  value="jdbc:mysql://localhost:3306/recolectores_db?useSSL=false&amp;serverTimezone=UTC"/>
        <property name="jakarta.persistence.jdbc.user"     value="root"/>
        <property name="jakarta.persistence.jdbc.password" value="root123"/>
        <property name="hibernate.hbm2ddl.auto"            value="update"/>
        <property name="hibernate.show_sql"                value="true"/>
    </properties>
</persistence-unit>
```

**Advertencia**: `hbm2ddl.auto=update` modifica el esquema automáticamente en desarrollo. En producción se recomienda cambiarlo a `validate` y gestionar migraciones manualmente con `schema.sql`.

### Build y distribución

```bash
mvn clean package
# Genera: target/sistema-recolectores-1.0-SNAPSHOT.jar
```

El plugin `maven-shade-plugin` empaqueta todas las dependencias en un único JAR autoejecutable. El `MANIFEST.MF` apunta a `com.sistema.ui.MainFrame` como clase principal.

### Comportamiento ante fallo de MySQL

Si la conexión falla al iniciar, Hibernate lanza una excepción en `ConexionBD.getEntityManager()`. Esta se propaga hasta `main()`, donde se muestra un diálogo de error y se llama a `System.exit(1)`. No existe mecanismo de reintento ni modo sin conexión.

---

## 9. Cómo extender el sistema

### Agregar una nueva entidad

1. Crear la clase en [src/main/java/com/sistema/model/](src/main/java/com/sistema/model/) con anotaciones `@Entity`, `@Table`, `@Id`, etc.
2. **Registrarla en `persistence.xml`** — sin este paso Hibernate no la reconocerá:
   ```xml
   <class>com.sistema.model.NuevaEntidad</class>
   ```
3. Crear la interfaz DAO en `com.sistema.dao` extendiendo `IGenericDAO<NuevaEntidad, Long>`.
4. Crear la implementación en `com.sistema.dao.impl` extendiendo `GenericDAOImpl`.
5. Crear la interfaz de servicio en `com.sistema.service` y su implementación.
6. Crear el panel Swing en `com.sistema.ui`.
7. Instanciar y cablear en el constructor de `MainFrame` y agregar la pestaña al `JTabbedPane`.

### Agregar un reporte

1. Crear una clase en [src/main/java/com/sistema/report/](src/main/java/com/sistema/report/) que implemente `IReporteGenerador`.
2. Implementar los métodos: `getNombre()`, `getColumnasEncabezado()`, `generarFilas(inicio, fin, usuario)`, `soportaEliminar()`, `eliminar(id)`.
3. En `MainFrame`, agregar la nueva instancia a la lista `reportes`:
   ```java
   List<IReporteGenerador> reportes = Arrays.asList(
       new ReportePorRecolector(pesajeDAO),
       // ...
       new NuevoReporte(dependencia)   // ← agregar aquí
   );
   ```
`ReportePanel` lo mostrará automáticamente en el combo selector.

### Agregar validaciones en `Validador`

Agregar un método estático en [src/main/java/com/sistema/util/Validador.java](src/main/java/com/sistema/util/Validador.java):

```java
public static void miValidacion(String valor, String campo) {
    requerido(valor, campo);
    if (!valor.matches("REGEX")) {
        throw new NegocioException("Mensaje claro para el usuario.");
    }
}
```

Llamarlo desde el servicio correspondiente antes de persistir datos.
