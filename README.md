# 🎫 TicketVortex API

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?style=for-the-badge&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-green?style=for-the-badge&logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Flyway](https://img.shields.io/badge/Flyway-Migration-red?style=for-the-badge&logo=flyway)
![OpenAPI](https://img.shields.io/badge/OpenAPI%203-Swagger%20UI-yellow?style=for-the-badge&logo=swagger)

**TicketVortex API** es una plataforma backend de alto rendimiento orientada a la gestión de eventos, zonas y reservas de tickets en tiempo real. Diseñada bajo una arquitectura modular y desacoplada (**Package-by-Feature**), implementa patrones de tolerancia a fallos, control estricto de concurrencia optimista, idempotencia en transacciones y expiración automática de reservas en segundo plano.

---

## 📋 Tabla de Contenidos

1. [Características Principales](#-características-principales)
2. [Arquitectura y Principios de Diseño](#-arquitectura-y-principios-de-diseño)
3. [Lógica de Negocio y Concurrencia](#-lógica-de-negocio-y-concurrencia)
4. [Tecnologías y Versiones](#-tecnologías-y-versiones)
5. [Estructura del Proyecto](#-estructura-del-proyecto)
6. [Configuración y Variables de Entorno](#-configuración-y-variables-de-entorno)
7. [Instalación y Ejecución](#-instalación-y-ejecución)
8. [Seguridad y Autenticación (JWT)](#-seguridad-y-autenticación-jwt)
9. [Manejo Global de Excepciones](#-manejo-global-de-excepciones)
10. [Documentación Completa de Endpoints](#-documentación-completa-de-endpoints)
    - [1. Authentication](#1-authentication)
    - [2. Events](#2-events)
    - [3. Zones](#3-zones)
    - [4. Users](#4-users)
    - [5. Reservations](#5-reservations)
11. [Documentación Interactiva (Swagger UI)](#-documentación-interactiva-swagger-ui)
12. [Testing y Cobertura](#-testing-y-cobertura)

---

## 🚀 Características Principales

- **Control de Sobreventa (Overselling Prevention):** Bloqueo optimista con incremento forzado de versión (`@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)`) en la entidad `Zone`.
- **Flujo de Reserva en 2 Pasos (Two-Phase Checkout):**
  1. *Reserva Temporal:* Bloquea stock con un TTL de 10 minutos en estado `RESERVED`.
  2. *Confirmación de Pago:* Transiciona a `CONFIRMED` asociando un identificador de transacción bancaria.
- **Idempotencia Garantizada:** Previene duplicidad de reservas y pagos concurrentes mediante llaves de idempotencia (`idempotencyKey`).
- **Límite Anti-Reventa:** Máximo 4 tickets activos (`RESERVED` o `CONFIRMED`) por usuario por evento.
- **Worker de Expiración Automatizado:** Tarea programada (`@Scheduled`) que libera reservas expiradas cada 60 segundos.
- **Seguridad Robusta:** Autenticación sin estado (Stateless) mediante JWT (HMAC-SHA-256) y autorización granular con `@PreAuthorize` a nivel de método (`ROLE_ADMIN`, `ROLE_CUSTOMER`).
- **Migraciones Automáticas:** Control de versiones del esquema relacional con Flyway.
- **Manejo Estandarizado de Errores:** Respuestas JSON homogéneas con validación campo por campo y mapeo preciso de estados HTTP (400, 401, 403, 404, 409).

---

## 🏛 Arquitectura y Principios de Diseño

El sistema sigue el estilo modular **Package-by-Feature** combinado con principios **SOLID**:

```
ticketvortexapi/
├── auth/         # Autenticación, JWT, registro y login
├── common/       # Configuración global, excepciones de dominio, OpenAPI
├── event/        # Catálogo de eventos
├── reservation/  # Motor de reservas, confirmación de pagos y worker de expiración
├── security/     # Filtros JWT, AuthenticationEntryPoint, AccessDeniedHandler
├── user/         # Gestión de usuarios y perfiles
└── zone/         # Capacidad, precios y control de concurrencia por zona
```

### Principios Clave:
- **Inversión de Dependencias (DIP):** Los servicios entre módulos interactúan estrictamente mediante interfaces de servicio (ej: `ReservationService` consume `ZoneService` y `UserService`), garantizando que ningún módulo acceda directamente a repositorios externos.
- **Single Responsibility (SRP):** Cada módulo gestiona su modelo de datos, mappers, DTOs y lógica de validación de forma autónoma.
- **Data Transfer Objects (DTO Records):** Modelado inmutable con validación declarativa (Jakarta Validation).

---

## ⚙ Lógica de Negocio y Concurrencia

### 1. Control de Concurrencia Optimista
Para evitar que dos clientes reserven la última entrada simultáneamente, la consulta de zona utiliza:
```java
@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
@Query("SELECT z FROM Zone z WHERE z.id = :id")
Optional<Zone> findByIdWithLock(@Param("id") UUID id);
```
Esto incrementa la columna `version` de la zona en cada reserva dentro de la transacción, arrojando `OptimisticLockingFailureException` si otro hilo modifica el registro concurrentemente.

### 2. Idempotencia en Reservas y Pagos
Tanto `POST /api/v1/reservations/{customerId}` como `POST /api/v1/reservations/confirm-payment` requieren un `idempotencyKey` (UUID). Si la red se interrumpe y el cliente reintenta la misma petición:
- El servicio detecta que la clave ya fue procesada.
- Retorna las reservas existentes de forma segura sin generar nuevos registros ni cobros duplicados.

### 3. Expiración de Reservas en Segundo Plano
La clase `ReservationExpirationTask` se ejecuta cada minuto:
```java
@Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
@Transactional
public void expirePendingReservations() {
    Instant now = Instant.now(Clock.systemUTC());
    List<Reservation> expired = repository.findByStatusAndExpiresAtBefore(Status.RESERVED, now);
    expired.forEach(r -> r.setStatus(Status.EXPIRED));
    repository.saveAll(expired);
}
```
Esto libera automáticamente el cupo de la zona para otros compradores cuando se cumple el tiempo límite de 10 minutos.

---

## 📦 Tecnologías y Versiones

| Componente | Tecnología | Versión |
| :--- | :--- | :--- |
| **Lenguaje** | Java (OpenJDK) | 21 (LTS) |
| **Framework** | Spring Boot | 4.1.1 |
| **Seguridad** | Spring Security | 6.x |
| **Tokens** | JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) | 0.12.6 |
| **Persistencia** | Spring Data JPA / Hibernate | 6.x / Jakarta Persistence 3.x |
| **Base de Datos** | PostgreSQL (Producción) / H2 (Testing) | 16+ / 2.4 |
| **Migraciones** | Flyway | Integrado con Spring Boot |
| **Validaciones** | Jakarta Validation (Hibernate Validator) | 3.0+ |
| **Documentación** | Springdoc OpenAPI Starter WebMvc UI | 3.1.0 |
| **Productividad** | Project Lombok | Última estable |
| **Build Tool** | Apache Maven | 3.9+ (vía Maven Wrapper `mvnw`) |

---

## 📂 Estructura del Proyecto

```text
src/
├── main/
│   ├── java/com/chacuio/ticketvortexapi/
│   │   ├── auth/
│   │   │   ├── controller/AuthController.java
│   │   │   ├── dto/{LoginRequestDTO, RegisterRequestDTO, RegisterResponseDTO, TokenResponseDTO}.java
│   │   │   └── service/{AuthService, AuthServiceImpl}.java
│   │   ├── common/
│   │   │   ├── config/OpenAPIConfig.java
│   │   │   └── exceptions/{BusinessException, ResourceNotFoundException, DataConflictException, ErrorResponseDTO, GlobalExceptionHandler}.java
│   │   ├── event/
│   │   │   ├── controller/EventController.java
│   │   │   ├── dto/{EventRequestDTO, EventResponseDTO, EventSummaryDTO}.java
│   │   │   ├── mapper/EventMapper.java
│   │   │   ├── model/Event.java
│   │   │   ├── repository/EventRepository.java
│   │   │   └── service/{EventService, EventServiceImpl}.java
│   │   ├── reservation/
│   │   │   ├── controller/ReservationController.java
│   │   │   ├── dto/{ReservationRequestDTO, ReservationResponseDTO, ReservationSummaryDTO, ConfirmPaymentRequestDTO, ConfirmPaymentResponseDTO}.java
│   │   │   ├── model/{Reservation, Status}.java
│   │   │   ├── repository/ReservationRepository.java
│   │   │   ├── service/{ReservationService, ReservationServiceImpl}.java
│   │   │   └── task/ReservationExpirationTask.java
│   │   ├── security/
│   │   │   ├── config/SecurityConfig.java
│   │   │   ├── filter/JwtFilter.java
│   │   │   └── service/{JwtService, CustomUserDetailsService}.java
│   │   ├── user/
│   │   │   ├── controller/UserController.java
│   │   │   ├── dto/{UserRequestDTO, UserResponseDTO, UserSummaryDTO, UserPatchRequestDTO}.java
│   │   │   ├── model/{User, Role}.java
│   │   │   ├── repository/UserRepository.java
│   │   │   └── service/{UserService, UserServiceImpl}.java
│   │   ├── zone/
│   │   │   ├── controller/ZoneController.java
│   │   │   ├── dto/{ZoneRequestDTO, ZoneResponseDTO, ZoneSummaryDTO}.java
│   │   │   ├── model/Zone.java
│   │   │   ├── repository/ZoneRepository.java
│   │   │   └── service/{ZoneService, ZoneServiceImpl}.java
│   │   └── TicketvortexapiApplication.java
│   └── resources/
│       ├── db/migration/V1__create_initial_schema.sql
│       └── application.properties
└── test/
    └── java/com/chacuio/ticketvortexapi/
        ├── event/controller/EventControllerIntegrationTest.java
        ├── event/service/EventServiceImplTest.java
        ├── reservation/controller/ReservationControllerIntegrationTest.java
        ├── reservation/service/ReservationServiceImplTest.java
        ├── user/service/UserServiceImplTest.java
        ├── zone/controller/ZoneControllerIntegrationTest.java
        └── zone/service/ZoneServiceImplTest.java
```

---

## 🔧 Configuración y Variables de Entorno

La aplicación carga automáticamente las variables desde un archivo `.env` o a través de variables de entorno del sistema operativo.

Crea un archivo `.env` en la raíz del proyecto:

```properties
# Base de Datos PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/ticketvortexdb
DB_USER=postgres
DB_PASSWORD=tu_contraseña

# Configuración JWT (HMAC-SHA-256: mínimo 256 bits / 32 caracteres)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

### Configuración en `application.properties`

```properties
spring.application.name=ticketvortexapi
spring.config.import=optional:file:.env[.properties]
spring.jackson.time-zone=UTC

# Conexión JPA / PostgreSQL
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

# Parámetros JWT
security.jwt.secret=${JWT_SECRET}
security.jwt.expiration=3600000        # 1 hora en ms
security.jwt.renew-time=604800000      # 7 días en ms
```

---

## 🛠 Instalación y Ejecución

### Prerrequisitos
- **Java 21 JDK** instalado y configurado en `JAVA_HOME`.
- **PostgreSQL 15+** en ejecución con la base de datos creada:
  ```sql
  CREATE DATABASE ticketvortexdb;
  ```

### Pasos para levantar el servicio

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/ticketvortexapi.git
   cd ticketvortexapi
   ```

2. **Crear el archivo `.env`** con las credenciales correspondientes.

3. **Compilar y verificar las pruebas:**
   ```bash
   # En Windows
   .\mvnw.cmd clean test

   # En Linux / macOS
   ./mvnw clean test
   ```

4. **Ejecutar la aplicación:**
   ```bash
   # En Windows
   .\mvnw.cmd spring-boot:run

   # En Linux / macOS
   ./mvnw spring-boot:run
   ```

La API quedará escuchando en `http://localhost:8080`.

---

## 🔒 Seguridad y Autenticación (JWT)

El sistema utiliza **Spring Security** con arquitectura sin sesiones (`SessionCreationPolicy.STATELESS`).

- **Roles Definidos:**
  - `CUSTOMER`: Comprador estándar de entradas.
  - `ADMIN`: Administrador de la plataforma (creación de eventos, gestión de zonas, consulta global).
- **Esquema de Autorización:**
  Los endpoints protegidos requieren el encabezado HTTP:
  ```http
  Authorization: Bearer <tu_token_jwt>
  ```
- **Filtro de Seguridad (`JwtFilter`):**
  Intercepta cada petición, extrae y valida el token con la firma secreta, carga el usuario y delega las excepciones de autenticación directamente al `HandlerExceptionResolver`.

---

## 🚨 Manejo Global de Excepciones

Todas las respuestas de error siguen el modelo unificado `ErrorResponseDTO`:

```json
{
  "message": "Validation failed for one or more fields",
  "status": 400,
  "path": "/api/v1/events",
  "timestamp": "2026-10-17T15:00:00Z",
  "fieldErrors": {
    "title": "Title is required",
    "zones": "An event must have at least one zone"
  }
}
```

### Códigos de Estado Manejados:
- **`400 Bad Request`:** Errores de validación (`MethodArgumentNotValidException`), formato de tipos (`MethodArgumentTypeMismatchException`), falta de capacidad (`NotEnoughCapacityException`) o límite de entradas superado (`TicketLimitExceededException`).
- **`401 Unauthorized`:** Token ausente, firma inválida o credenciales incorrectas.
- **`403 Forbidden`:** El usuario autenticado no posee el rol necesario (`AccessDeniedException`).
- **`404 Not Found`:** Recurso inexistente (`ResourceNotFoundException`, `NoResourceFoundException`).
- **`409 Conflict`:** Conflicto de unicidad en BD, ej. email o documento duplicado (`DataConflictException`, `DataIntegrityViolationException`).
- **`500 Internal Server Error`:** Errores no controlados del servidor.

---

## 📖 Documentación Completa de Endpoints

---

### 1. Authentication

Endpoints públicos para registro e inicio de sesión.

#### 1.1 Registrar Usuario
- **Método & URL:** `POST /api/v1/auth/register`
- **Seguridad:** Pública
- **Descripción:** Crea una nueva cuenta y retorna los datos del usuario junto con un token JWT de acceso inmediato.
- **Request Body:**
  ```json
  {
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan.perez@example.com",
    "documentNumber": "40123456",
    "password": "Password123!",
    "role": "CUSTOMER"
  }
  ```
- **Respuestas:**
  - **`201 Created`**:
    ```json
    {
      "userData": {
        "id": "e4f8d59a-8b1b-466d-9b0a-2bb62a5b2871",
        "firstName": "Juan",
        "lastName": "Pérez",
        "email": "juan.perez@example.com",
        "documentNumber": "40123456",
        "role": "CUSTOMER",
        "isActive": true,
        "createdAt": "2026-10-17T14:30:00Z",
        "updatedAt": null
      },
      "token": "eyJhbGciOiJIUzI1NiIsIn..."
    }
    ```
  - **`400 Bad Request`**: Datos inválidos o faltantes.
  - **`409 Conflict`**: El email o documento ya se encuentra registrado.

---

#### 1.2 Iniciar Sesión (Login)
- **Método & URL:** `POST /api/v1/auth/login`
- **Seguridad:** Pública
- **Descripción:** Valida credenciales y emite un token JWT.
- **Request Body:**
  ```json
  {
    "email": "juan.perez@example.com",
    "password": "Password123!"
  }
  ```
- **Respuestas:**
  - **`200 OK`**:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsIn..."
    }
    ```
  - **`400 Bad Request`**: Formato de email incorrecto o contraseña en blanco.
  - **`401 Unauthorized`**: Credenciales inválidas.

---

### 2. Events

Gestión del catálogo de eventos y sus zonas asociadas.

#### 2.1 Listar todos los Eventos
- **Método & URL:** `GET /api/v1/events`
- **Seguridad:** Requiere rol `ADMIN` o `CUSTOMER` (`Bearer Authentication`)
- **Respuestas:**
  - **`200 OK`**:
    ```json
    [
      {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "title": "Cosquín Rock 2027",
        "date": "2027-02-15T18:00:00",
        "location": "Aeródromo Santa María de Punilla, Córdoba"
      }
    ]
    ```
  - **`401 Unauthorized`**: Token ausente o inválido.

---

#### 2.2 Obtener Evento por ID
- **Método & URL:** `GET /api/v1/events/{id}`
- **Seguridad:** Requiere rol `ADMIN` o `CUSTOMER` (`Bearer Authentication`)
- **Parámetros de Ruta:**
  - `id` (UUID): Identificador único del evento.
- **Respuestas:**
  - **`200 OK`**:
    ```json
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "title": "Cosquín Rock 2027",
      "description": "Festival anual de música rock",
      "date": "2027-02-15T18:00:00",
      "location": "Aeródromo Santa María de Punilla, Córdoba",
      "zones": [
        {
          "id": "6a96e5b2-3c81-49b0-a0a1-9a70659dbdf1",
          "name": "Campo General",
          "capacity": 5000,
          "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        },
        {
          "id": "8b12e3a1-2d71-41c0-b1a2-8b60548caef2",
          "name": "VIP Fanatic",
          "capacity": 300,
          "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        }
      ],
      "createdAt": "2026-10-17T12:00:00Z",
      "updatedAt": null
    }
    ```
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`404 Not Found`**: El evento no existe.

---

#### 2.3 Crear Evento con Zonas
- **Método & URL:** `POST /api/v1/events`
- **Seguridad:** Requiere rol `ADMIN` (`Bearer Authentication`)
- **Descripción:** Da de alta un evento y crea atómicamente sus zonas iniciales.
- **Request Body:**
  ```json
  {
    "title": "Cosquín Rock 2027",
    "description": "Festival anual de música rock",
    "date": "2027-02-15T18:00:00",
    "location": "Aeródromo Santa María de Punilla, Córdoba",
    "zones": [
      {
        "name": "Campo General",
        "description": "Acceso general a todos los escenarios",
        "price": 35000.00,
        "capacity": 5000
      },
      {
        "name": "VIP Fanatic",
        "description": "Acceso preferencial, barra libre y catering",
        "price": 95000.00,
        "capacity": 300
      }
    ]
  }
  ```
- **Respuestas:**
  - **`201 Created`**: Retorna el objeto `EventResponseDTO` completo.
  - **`400 Bad Request`**: Datos inválidos, fecha no futura o lista de zonas vacía.
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`403 Forbidden`**: Requiere rol `ADMIN`.

---

### 3. Zones

Gestión de sectores, capacidad y precios por evento.

#### 3.1 Obtener Zona por ID
- **Método & URL:** `GET /api/v1/zones/{id}`
- **Seguridad:** Requiere rol `ADMIN` o `CUSTOMER` (`Bearer Authentication`)
- **Parámetros de Ruta:**
  - `id` (UUID): Identificador único de la zona.
- **Respuestas:**
  - **`200 OK`**:
    ```json
    {
      "id": "6a96e5b2-3c81-49b0-a0a1-9a70659dbdf1",
      "name": "Campo General",
      "description": "Acceso general a todos los escenarios",
      "price": 35000.00,
      "capacity": 5000,
      "version": 1,
      "event": {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "title": "Cosquín Rock 2027",
        "date": "2027-02-15T18:00:00",
        "location": "Aeródromo Santa María de Punilla, Córdoba"
      },
      "createdAt": "2026-10-17T12:00:00Z",
      "updatedAt": null
    }
    ```
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`404 Not Found`**: La zona no existe.

---

#### 3.2 Listar Zonas por Evento
- **Método & URL:** `GET /api/v1/zones/events/{eventId}`
- **Seguridad:** Requiere rol `ADMIN` o `CUSTOMER` (`Bearer Authentication`)
- **Parámetros de Ruta:**
  - `eventId` (UUID): Identificador del evento.
- **Respuestas:**
  - **`200 OK`**: Lista de objetos `ZoneSummaryDTO`.
  - **`401 Unauthorized`**: Token ausente o inválido.

---

#### 3.3 Agregar Zona a un Evento
- **Método & URL:** `POST /api/v1/zones/events/{eventId}`
- **Seguridad:** Requiere rol `ADMIN` (`Bearer Authentication`)
- **Parámetros de Ruta:**
  - `eventId` (UUID): Identificador del evento al que se adjuntará la zona.
- **Request Body:**
  ```json
  {
    "name": "Platea Alta",
    "description": "Ubicación numerada con vista panorámica",
    "price": 45000.00,
    "capacity": 800
  }
  ```
- **Respuestas:**
  - **`201 Created`**:
    ```json
    {
      "id": "7b21e4a3-4c92-41b1-a1b2-9a81760ecdf3",
      "name": "Platea Alta",
      "capacity": 800,
      "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    }
    ```
  - **`400 Bad Request`**: Datos inválidos, precio negativo o capacidad menor a 1.
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`403 Forbidden`**: Requiere rol `ADMIN`.
  - **`404 Not Found`**: El evento especificado no existe.

---

### 4. Users

Administración y actualización de perfiles de usuario.

#### 4.1 Listar todos los Usuarios
- **Método & URL:** `GET /api/v1/users`
- **Seguridad:** Requiere rol `ADMIN` (`Bearer Authentication`)
- **Respuestas:**
  - **`200 OK`**:
    ```json
    [
      {
        "id": "e4f8d59a-8b1b-466d-9b0a-2bb62a5b2871",
        "firstName": "Juan",
        "lastName": "Pérez",
        "email": "juan.perez@example.com"
      }
    ]
    ```
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`403 Forbidden`**: Requiere rol `ADMIN`.

---

#### 4.2 Obtener Usuario por ID
- **Método & URL:** `GET /api/v1/users/{id}`
- **Seguridad:** Requiere rol `ADMIN` o `CUSTOMER` (`Bearer Authentication`)
- **Parámetros de Ruta:**
  - `id` (UUID): Identificador del usuario.
- **Respuestas:**
  - **`200 OK`**: Retorna el objeto `UserResponseDTO`.
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`404 Not Found`**: Usuario no encontrado.

---

#### 4.3 Crear Usuario Administrativo
- **Método & URL:** `POST /api/v1/users`
- **Seguridad:** Requiere rol `ADMIN` (`Bearer Authentication`)
- **Request Body:**
  ```json
  {
    "firstName": "Admin",
    "lastName": "Principal",
    "email": "admin@ticketvortex.com",
    "documentNumber": "99999999",
    "password": "AdminPassword123!",
    "role": "ADMIN"
  }
  ```
- **Respuestas:**
  - **`201 Created`**: Retorna el objeto `UserResponseDTO` creado.
  - **`400 Bad Request`**: Validación fallida.
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`403 Forbidden`**: Requiere rol `ADMIN`.
  - **`409 Conflict`**: Email o documento ya existente.

---

#### 4.4 Actualización Parcial de Usuario (Patch)
- **Método & URL:** `PATCH /api/v1/users/{id}`
- **Seguridad:** Requiere rol `ADMIN` o `CUSTOMER` (`Bearer Authentication`)
- **Descripción:** Actualiza solo los campos provistos (al menos uno debe enviarse).
- **Request Body:**
  ```json
  {
    "firstName": "Juan Carlos",
    "email": "juancarlos.perez@example.com"
  }
  ```
- **Respuestas:**
  - **`200 OK`**: Retorna el objeto `UserResponseDTO` actualizado.
  - **`400 Bad Request`**: Ningún campo provisto o formato de email inválido.
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`404 Not Found`**: Usuario no encontrado.
  - **`409 Conflict`**: El nuevo email o documento ya está en uso por otro usuario.

---

#### 4.5 Eliminar Usuario
- **Método & URL:** `DELETE /api/v1/users/{id}`
- **Seguridad:** Requiere rol `ADMIN` (`Bearer Authentication`)
- **Respuestas:**
  - **`204 No Content`**: Usuario eliminado exitosamente.
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`403 Forbidden`**: Requiere rol `ADMIN`.
  - **`404 Not Found`**: Usuario no encontrado.

---

### 5. Reservations

Motor de reserva temporal, expiración y confirmación de pagos.

#### 5.1 Crear Reserva Temporal (Hold)
- **Método & URL:** `POST /api/v1/reservations/{customerId}`
- **Seguridad:** Requiere rol `ADMIN` o `CUSTOMER` (`Bearer Authentication`)
- **Descripción:** Bloquea temporalmente hasta 4 tickets en una zona durante 10 minutos. Es idempotente según el `idempotencyKey`.
- **Parámetros de Ruta:**
  - `customerId` (UUID): Identificador del usuario que efectúa la compra.
- **Request Body:**
  ```json
  {
    "zoneId": "6a96e5b2-3c81-49b0-a0a1-9a70659dbdf1",
    "quantity": 2,
    "idempotencyKey": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"
  }
  ```
- **Respuestas:**
  - **`201 Created`**:
    ```json
    [
      {
        "id": "11111111-2222-3333-4444-555555555555",
        "customer": {
          "id": "e4f8d59a-8b1b-466d-9b0a-2bb62a5b2871",
          "firstName": "Juan",
          "lastName": "Pérez",
          "email": "juan.perez@example.com"
        },
        "zone": {
          "id": "6a96e5b2-3c81-49b0-a0a1-9a70659dbdf1",
          "name": "Campo General",
          "capacity": 5000,
          "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        },
        "status": "RESERVED",
        "expiresAt": "2026-10-17T15:10:00Z",
        "idempotencyKey": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
        "createdAt": "2026-10-17T15:00:00Z",
        "updatedAt": null
      },
      {
        "id": "22222222-3333-4444-5555-666666666666",
        "customer": {
          "id": "e4f8d59a-8b1b-466d-9b0a-2bb62a5b2871",
          "firstName": "Juan",
          "lastName": "Pérez",
          "email": "juan.perez@example.com"
        },
        "zone": {
          "id": "6a96e5b2-3c81-49b0-a0a1-9a70659dbdf1",
          "name": "Campo General",
          "capacity": 5000,
          "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        },
        "status": "RESERVED",
        "expiresAt": "2026-10-17T15:10:00Z",
        "idempotencyKey": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
        "createdAt": "2026-10-17T15:00:00Z",
        "updatedAt": null
      }
    ]
    ```
  - **`400 Bad Request`**: Capacidad insuficiente (`NotEnoughCapacityException`), límite de 4 tickets por usuario superado (`TicketLimitExceededException`) o cantidad inválida.
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`404 Not Found`**: Cliente o zona no encontrados.

---

#### 5.2 Confirmar Pago de Reserva
- **Método & URL:** `POST /api/v1/reservations/confirm-payment`
- **Seguridad:** Requiere rol `ADMIN` o `CUSTOMER` (`Bearer Authentication`)
- **Descripción:** Valida que las reservas asociadas a la clave de idempotencia sigan vigentes (no expiradas), asigna el ID de transacción del gateway de pago y actualiza el estado a `CONFIRMED`.
- **Request Body:**
  ```json
  {
    "idempotencyKey": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "transactionId": "f9e8d7c6-b5a4-3210-9876-543210fedcba"
  }
  ```
- **Respuestas:**
  - **`200 OK`**:
    ```json
    {
      "idempotencyKey": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
      "transactionId": "f9e8d7c6-b5a4-3210-9876-543210fedcba",
      "status": "CONFIRMED",
      "confirmedAt": "2026-10-17T15:05:00Z",
      "reservations": [
        {
          "id": "11111111-2222-3333-4444-555555555555",
          "customerId": "e4f8d59a-8b1b-466d-9b0a-2bb62a5b2871",
          "customerEmail": "juan.perez@example.com",
          "zoneId": "6a96e5b2-3c81-49b0-a0a1-9a70659dbdf1",
          "zoneName": "Campo General",
          "status": "CONFIRMED",
          "price": 35000.00,
          "expiresAt": "2026-10-17T15:10:00Z",
          "createdAt": "2026-10-17T15:00:00Z"
        },
        {
          "id": "22222222-3333-4444-5555-666666666666",
          "customerId": "e4f8d59a-8b1b-466d-9b0a-2bb62a5b2871",
          "customerEmail": "juan.perez@example.com",
          "zoneId": "6a96e5b2-3c81-49b0-a0a1-9a70659dbdf1",
          "zoneName": "Campo General",
          "status": "CONFIRMED",
          "price": 35000.00,
          "expiresAt": "2026-10-17T15:10:00Z",
          "createdAt": "2026-10-17T15:00:00Z"
        }
      ]
    }
    ```
  - **`400 Bad Request`**: Una o más reservas han expirado (`ReservationExpiredException`).
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`404 Not Found`**: Clave de idempotencia inexistente.

---

#### 5.3 Obtener Reserva por ID
- **Método & URL:** `GET /api/v1/reservations/search/{reservationId}`
- **Seguridad:** Requiere rol `ADMIN` o `CUSTOMER` (`Bearer Authentication`)
- **Parámetros de Ruta:**
  - `reservationId` (UUID): Identificador único de la reserva.
- **Respuestas:**
  - **`200 OK`**: Retorna el objeto `ReservationResponseDTO`.
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`404 Not Found`**: Reserva no encontrada.

---

#### 5.4 Listar Reservas por Cliente
- **Método & URL:** `GET /api/v1/reservations/customer/{customerId}`
- **Seguridad:** Requiere rol `ADMIN` o `CUSTOMER` (`Bearer Authentication`)
- **Parámetros de Ruta:**
  - `customerId` (UUID): Identificador del cliente.
- **Respuestas:**
  - **`200 OK`**: Lista de objetos `ReservationSummaryDTO`.
  - **`401 Unauthorized`**: Token ausente o inválido.

---

#### 5.5 Listar Reservas por Evento
- **Método & URL:** `GET /api/v1/reservations/{eventId}`
- **Seguridad:** Requiere rol `ADMIN` (`Bearer Authentication`)
- **Parámetros de Ruta:**
  - `eventId` (UUID): Identificador del evento.
- **Respuestas:**
  - **`200 OK`**: Lista de objetos `ReservationSummaryDTO`.
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`403 Forbidden`**: Requiere rol `ADMIN`.

---

#### 5.6 Eliminar / Cancelar Reserva
- **Método & URL:** `DELETE /api/v1/reservations/{reservationId}`
- **Seguridad:** Requiere rol `ADMIN` (`Bearer Authentication`)
- **Parámetros de Ruta:**
  - `reservationId` (UUID): Identificador de la reserva a suprimir.
- **Respuestas:**
  - **`204 No Content`**: Reserva eliminada exitosamente.
  - **`401 Unauthorized`**: Token ausente o inválido.
  - **`403 Forbidden`**: Requiere rol `ADMIN`.
  - **`404 Not Found`**: Reserva no encontrada.

---

## 🖥 Documentación Interactiva (Swagger UI)

La API cuenta con soporte completo para OpenAPI 3 y una interfaz interactiva Swagger UI disponible sin restricciones de autenticación en:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html` o `http://localhost:8080/swagger-ui/index.html`
- **Especificación OpenAPI (JSON):** `http://localhost:8080/v3/api-docs`

> **Para probar endpoints protegidos desde Swagger UI:**
> 1. Realiza una petición a `POST /api/v1/auth/login` o `POST /api/v1/auth/register`.
> 2. Copia el token JWT de la respuesta.
> 3. Haz clic en el botón superior verde **Authorize 🔓** e ingresa el token.

---

## 🧪 Testing y Cobertura

La suite de pruebas automatizadas combina tests unitarios desacoplados (mediante mocks con Mockito) y pruebas de integración end-to-end (utilizando Spring Boot Test, H2 en memoria y MockMvc).

### Ejecutar todas las pruebas:
```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

### Resultados de la Suite:
- **Total de Tests:** 44 ejecutados
- **Fallos:** 0
- **Errores:** 0
- **Omitidos:** 0
- **Tasa de Éxito:** 100%

### Componentes Testeados:
- `EventControllerIntegrationTest`: Creación integral de eventos y zonas, validación de DTOs y manejo de errores 404.
- `EventServiceImplTest`: Lógica unitaria de persistencia y mapeo de eventos.
- `ZoneControllerIntegrationTest`: Consultas por ID y evento, creación administrativa (201) y verificación de denegación 403 a usuarios estándar.
- `ZoneServiceImplTest`: Bloqueo optimista, cálculo de capacidad y persistencia por lotes.
- `ReservationControllerIntegrationTest`: Flujo completo de reservas, idempotencia, rechazo por falta de stock y confirmación de pagos.
- `ReservationServiceImplTest`: Límite de 4 tickets, control de reservas expiradas e idempotencia.
- `UserServiceImplTest`: Alta, consulta, actualización parcial y verificación de unicidad de emails.

---

## 📄 Licencia

Este proyecto se encuentra bajo la licencia [MIT](LICENSE).
