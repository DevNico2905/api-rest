# API REST — Catálogo de Productos

API REST construida con **Spring Boot 4.1.1** y **Java 21** para gestionar un catálogo de productos organizados por categorías, con persistencia en **PostgreSQL** mediante Spring Data JPA.

---

## Tabla de contenidos

- [Stack tecnológico](#stack-tecnológico)
- [Requisitos previos](#requisitos-previos)
- [Puesta en marcha](#puesta-en-marcha)
- [Configuración](#configuración)
- [Arquitectura](#arquitectura)
- [Modelo de datos](#modelo-de-datos)
- [Validaciones](#validaciones)
- [Endpoints](#endpoints)
  - [Categorías](#categorías)
  - [Productos](#productos)
- [Manejo de errores](#manejo-de-errores)
- [Pruebas](#pruebas)
- [Datos de ejemplo](#datos-de-ejemplo)
- [Comandos útiles de PostgreSQL](#comandos-útiles-de-postgresql)
- [Limitaciones conocidas](#limitaciones-conocidas)

---

## Stack tecnológico

| Componente | Versión / Detalle |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring Web MVC | `spring-boot-starter-webmvc` |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL (driver en *runtime*) |
| Validación | `spring-boot-starter-validation` (Jakarta Bean Validation) |
| Boilerplate | Lombok (vía `annotationProcessorPaths`) |
| Build | Maven (con *wrapper* `./mvnw`) |
| Contenedores | Docker Compose |

Paquete base: `com.devnico.api_rest`.

> El nombre de paquete original `com.devnico.api-rest` no es válido en Java, por eso el proyecto usa `com.devnico.api_rest` (ver `HELP.md`).

---

## Requisitos previos

- **JDK 21 o superior** (el proyecto compila con `release 21`).
- **Docker** y **Docker Compose**, para levantar PostgreSQL.
- No se requiere instalar Maven: el repositorio incluye el wrapper (`./mvnw`).

---

## Puesta en marcha

### 1. Levantar la base de datos

```bash
docker compose up -d
```

Esto crea el contenedor `postgres-spring-boot` exponiendo PostgreSQL en el **puerto 5332** del host (mapeado al 5432 del contenedor).

### 2. Crear la base de datos `amigos`

El contenedor solo crea la base de datos por defecto `amigoscode`, pero la aplicación apunta a `amigos`. Es necesario crearla **una sola vez**:

```bash
docker exec -it postgres-spring-boot psql -U amigoscode -c "CREATE DATABASE amigos;"
```

Sin este paso, el arranque de la aplicación falla al no poder conectarse.

### 3. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### 4. Otros comandos de build

```bash
./mvnw clean package        # Compila y empaqueta el JAR en target/
./mvnw clean               # Limpia artefactos de compilación
java -jar target/api-rest-0.0.1-SNAPSHOT.jar   # Ejecuta el JAR empaquetado
```

---

## Configuración

Toda la configuración vive en `src/main/resources/application.properties`:

```properties
spring.application.name=api-rest
spring.datasource.url=jdbc:postgresql://localhost:5332/amigos
spring.datasource.username=amigoscode
spring.datasource.password=password

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.show-sql=true
```

| Propiedad | Efecto |
|---|---|
| `ddl-auto=create-drop` | Hibernate **crea el esquema en cada arranque y lo elimina al apagar la aplicación**. Los datos no persisten entre ejecuciones. |
| `show-sql=true` | Imprime en consola cada sentencia SQL ejecutada. |
| `format_sql=true` | Formatea ese SQL para que sea legible. |

Credenciales definidas en `docker-compose.yaml`:

| Parámetro | Valor |
|---|---|
| Usuario | `amigoscode` |
| Contraseña | `password` |
| Puerto (host) | `5332` |
| Contenedor | `postgres-spring-boot` |

---

## Arquitectura

El proyecto sigue una arquitectura clásica en capas, con un *slice* vertical por agregado (`Product` y `Category`):

```
Cliente HTTP
    │
    ▼
controller/        @RestController — expone endpoints, mapea códigos HTTP, aplica @Valid
    │
    ▼
service/           Interfaz — define el contrato de negocio
service/impl/      @Service — implementación, orquesta el repositorio y lanza errores
    │
    ▼
repository/        @Repository — extiende JpaRepository, consultas derivadas del nombre
    │
    ▼
entity/            @Entity — modelo JPA + restricciones de validación y de DDL
```

### Estructura de directorios

```
src/main/java/com/devnico/api_rest/
├── ApiRestApplication.java          # Punto de entrada @SpringBootApplication
├── controller/
│   ├── CategoryController.java      # /api/v1/categories
│   └── ProductController.java       # /api/v1/products
├── service/
│   ├── CategoryService.java         # Contrato
│   ├── ProductService.java          # Contrato
│   └── impl/
│       ├── CategoryServiceImpl.java
│       └── ProductServiceImpl.java
├── repository/
│   ├── CategoryRepository.java
│   └── ProductRepository.java
└── entity/
    ├── Category.java
    ├── Product.java
    └── enums/
        └── Status.java

src/main/resources/
├── application.properties
└── datos.json                       # 20 productos de ejemplo (no se cargan automáticamente)

src/test/java/com/devnico/api_rest/
└── ApiRestApplicationTests.java     # contextLoads()
```

### Convenciones del código

Estas convenciones se repiten en todo el proyecto; conviene mantenerlas al añadir funcionalidad:

- **Sin DTOs.** Las entidades JPA se usan directamente como cuerpo de petición y de respuesta.
- **Par interfaz + `Impl`.** Cada servicio declara su contrato en `service/` y lo implementa en `service/impl/`. Los controladores dependen de la interfaz, nunca de la implementación.
- **Inyección por constructor.** Sin `@Autowired`; las dependencias son campos `private final`.
- **Validación en la entidad.** Las anotaciones de Bean Validation conviven con las restricciones DDL (`nullable`, `length`, `unique`) sobre la misma `@Column`.
- **Tipos monetarios y numéricos.** El precio es `BigDecimal` (nunca `Double`, por precisión) y la cantidad es `Integer` (envuelto, para que `@NotNull` tenga efecto).
- **Códigos HTTP explícitos.** `201 Created` en POST, `200 OK` en GET/PUT, `204 No Content` en DELETE.

---

## Modelo de datos

### `Category`

| Campo Java | Columna | Tipo | Restricciones |
|---|---|---|---|
| `idCategory` | `id_category` | `bigint` | PK, autogenerada (`IDENTITY`) |
| `categoryName` | `category_name` | `varchar(100)` | `NOT NULL`, `UNIQUE`, `@NotBlank` |

### `Product`

| Campo Java | Columna | Tipo | Restricciones |
|---|---|---|---|
| `id` | `id` | `bigint` | PK, autogenerada (`IDENTITY`) |
| `productName` | `name` | `varchar(100)` | `NOT NULL`, `UNIQUE`, `@NotBlank` |
| `description` | `description` | `varchar(500)` | Opcional, `@Size(max = 500)` |
| `price` | `price` | `numeric` | `NOT NULL`, `@NotNull`, tipo `BigDecimal` |
| `amount` | `amount` | `integer` | `NOT NULL`, `@NotNull`, `@PositiveOrZero` |
| `status` | `status` | `varchar` | `NOT NULL`, enum persistido como texto (`EnumType.STRING`) |
| `category` | `id_category` | `bigint` | FK → `category.id_category`, `NOT NULL`, relación `@ManyToOne` |

### Enum `Status`

```java
DISPONIBLE, NO_DISPONIBLE
```

### Relación

Un `Product` pertenece a exactamente una `Category` (`@ManyToOne`). La relación es **unidireccional**: `Category` no expone la lista de sus productos.

### Consultas derivadas (`ProductRepository`)

| Método | Consulta generada |
|---|---|
| `findByProductName(String)` | Producto por nombre exacto (devuelve `Optional`) |
| `findByStatus(Status)` | Productos filtrados por disponibilidad |
| `findProductsByCategory(Category)` | Productos de una categoría |
| `count()` | Total de productos |

---

## Validaciones

Las validaciones se aplican en dos niveles:

1. **Bean Validation** (`@NotBlank`, `@NotNull`, `@Size`, `@PositiveOrZero`) — se ejecuta cuando el controlador anota el cuerpo con `@Valid`; devuelve `400 Bad Request`.
2. **Restricciones DDL** (`nullable`, `length`, `unique`) — las impone PostgreSQL; una violación (por ejemplo, un nombre duplicado) se propaga como error de la base de datos.

Endpoints que aplican `@Valid`: creación de producto, creación de categoría y actualización de categoría. **La actualización de producto (`PUT /api/v1/products/{id}`) no valida el cuerpo.**

---

## Endpoints

Base URL: `http://localhost:8080`

### Categorías

Prefijo: `/api/v1/categories`

#### Crear categoría

```http
POST /api/v1/categories
Content-Type: application/json

{
  "categoryName": "Food"
}
```

**Respuesta `201 Created`**

```json
{
  "idCategory": 1,
  "categoryName": "Food"
}
```

#### Listar todas las categorías

```http
GET /api/v1/categories
```

**Respuesta `200 OK`** — array de categorías.

#### Actualizar categoría

```http
PUT /api/v1/categories/{id}
Content-Type: application/json

{
  "categoryName": "Vehicles"
}
```

**Respuesta `200 OK`** — categoría actualizada. Solo se modifica `categoryName`.

#### Eliminar categoría

```http
DELETE /api/v1/categories/{id}
```

**Respuesta `204 No Content`**

---

### Productos

Prefijo: `/api/v1/products`

#### Crear producto

```http
POST /api/v1/products
Content-Type: application/json

{
  "productName": "Malteada Popsy",
  "description": "Juan Valdez Crack",
  "price": 10.0,
  "amount": 10,
  "status": "DISPONIBLE",
  "category": {
    "idCategory": 2,
    "categoryName": "Tech"
  }
}
```

**Respuesta `201 Created`** — producto creado con su `id`.

> La categoría se envía **anidada**, identificada por `idCategory`, y debe existir previamente.

#### Listar todos los productos

```http
GET /api/v1/products
```

**Respuesta `200 OK`** — array de productos con su categoría anidada.

#### Buscar producto por nombre

```http
GET /api/v1/products/find-by-name/{name}
```

- `200 OK` — el producto encontrado.
- `404 Not Found` — cuerpo de texto plano: `Product not found`.

#### Buscar producto por ID

```http
GET /api/v1/products/find-by-id/{id}
```

- `200 OK` — el producto encontrado.
- `404 Not Found` — cuerpo de texto plano: `Product not found`.

#### Actualizar producto

```http
PUT /api/v1/products/{id}
Content-Type: application/json

{
  "productName": "PS5",
  "description": "White",
  "price": 1300.99,
  "amount": 0,
  "status": "NO_DISPONIBLE",
  "category": {
    "idCategory": 2,
    "categoryName": "Tech"
  }
}
```

**Respuesta `200 OK`** — producto actualizado.

> Se actualizan `productName`, `description`, `price`, `amount` y `status`. **La categoría no se reasigna**, aunque se incluya en el cuerpo.

#### Cambiar solo el estado

```http
PUT /api/v1/products/status/{id}
Content-Type: application/json

"NO_DISPONIBLE"
```

**Respuesta `200 OK`** — producto con el nuevo estado.

> El cuerpo es el valor del enum como cadena JSON (entre comillas), no un objeto.

#### Listar productos por estado

```http
GET /api/v1/products/list-by-status/{status}
```

Donde `{status}` es `DISPONIBLE` o `NO_DISPONIBLE`.

**Respuesta `200 OK`** — array de productos.

#### Listar productos por categoría

```http
GET /api/v1/products/get-all-by-category/{idCategory}
```

**Respuesta `200 OK`** — array de productos de esa categoría.

> El parámetro de ruta es el **ID numérico** de la categoría. Spring Data Web lo convierte automáticamente en la entidad `Category` consultando el repositorio.

#### Contar productos

```http
GET /api/v1/products/count-all
```

**Respuesta `200 OK`** — texto plano:

```
There are 20 products.
```

#### Eliminar producto

```http
DELETE /api/v1/products/{id}
```

**Respuesta `204 No Content`**

---

## Manejo de errores

El proyecto **no cuenta con un `@ControllerAdvice`** ni con excepciones de dominio propias. El comportamiento actual es:

| Situación | Resultado |
|---|---|
| Validación `@Valid` fallida | `400 Bad Request` |
| Producto no encontrado en `find-by-id` / `find-by-name` | `404 Not Found` con texto plano |
| Producto o categoría no encontrado en `update`, `delete` o `changeStatus` | `RuntimeException` → **`500 Internal Server Error`** |
| Violación de restricción de base de datos (nombre duplicado, FK inexistente) | `500 Internal Server Error` |

---

## Pruebas

```bash
./mvnw test                                              # Toda la suite
./mvnw test -Dtest=ApiRestApplicationTests               # Una clase
./mvnw test -Dtest=ApiRestApplicationTests#contextLoads  # Un método
```

La única prueba automatizada es `contextLoads()`, que verifica que el contexto de Spring arranca correctamente. **Requiere que PostgreSQL esté levantado.**

### Pruebas manuales con `Request.http`

El archivo `Request.http` de la raíz contiene una petición lista para ejecutar por cada endpoint y funciona como la superficie de prueba real del proyecto. Se ejecuta desde el **HTTP Client de IntelliJ IDEA** (clic en el icono ▶ junto a cada petición); el historial de respuestas queda en `.idea/httpRequests/`.

Al añadir un endpoint nuevo, conviene añadir también su petición a este archivo.

---

## Datos de ejemplo

`src/main/resources/datos.json` contiene **20 productos de ejemplo** (periféricos, monitores, mobiliario de oficina, etc.) con los campos `productName`, `description`, `price`, `amount` y `status`.

Este archivo **no se carga automáticamente** al arrancar: no existe ningún *seeder* que lo lea. Sirve como fuente para poblar la base de datos manualmente vía `POST /api/v1/products`, recordando añadir el objeto `category` a cada producto, ya que el JSON no lo incluye y el campo es obligatorio.

---

## Comandos útiles de PostgreSQL

```bash
docker exec -it postgres-spring-boot bash
psql -U amigoscode
```

Dentro de `psql`:

| Comando | Descripción |
|---|---|
| `\l` | Lista las bases de datos |
| `\c amigos` | Conecta a la base de datos `amigos` |
| `\dt` | Muestra las tablas de la base de datos actual |
| `\d <tabla>` | Estructura de una tabla: columnas, tipos, PK/FK e índices |
| `\q` | Sale de la sesión |

---

## Limitaciones conocidas

Puntos abiertos del proyecto, útiles como hoja de ruta:

- **Los datos no persisten.** Con `ddl-auto=create-drop`, el esquema se elimina al apagar la aplicación. Para conservar los datos entre ejecuciones hay que cambiar a `update` o `validate` y gestionar el esquema con migraciones (Flyway/Liquibase).
- **El volumen de Docker no está montado.** `docker-compose.yaml` declara el volumen `db` pero el servicio no lo usa, por lo que los datos también se pierden al recrear el contenedor.
- **Credenciales en texto plano** dentro de `application.properties`; deberían externalizarse a variables de entorno.
- **Sin manejo global de excepciones**: los errores de negocio se traducen en `500` en lugar de `404` / `409`.
- **Sin DTOs**: las entidades JPA se exponen directamente, lo que acopla el contrato de la API al modelo de persistencia.
- **Sin paginación** en los listados (`GET /products`, `GET /categories`).
- **Sin cobertura de pruebas** más allá de `contextLoads()`; no hay pruebas de controladores ni de servicios.
- **`PUT /products/{id}` no valida** el cuerpo ni permite reasignar la categoría.
- La base de datos `amigos` debe crearse a mano porque no coincide con la que inicializa el contenedor.
