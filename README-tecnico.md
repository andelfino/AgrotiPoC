# AgroMonitor Mini - Documentacion tecnica minima

## 1) Objetivo del prototipo
AgroMonitor Mini valida un flujo basico de monitoreo IoT: registrar eventos de sensores y consultarlos luego desde cliente mobile.  
El foco es demostrar un circuito completo **API + logica de negocio + persistencia en PostgreSQL** con reglas simples de severidad.

## 2) Arquitectura general
Arquitectura en capas, monolito liviano:

- **Cliente mobile (React Native/Expo)** consume HTTP JSON.
- **Backend Java 21** expone endpoint `/eventos` sobre `HttpServer` (JDK).
- **Servicio de dominio (`EventoService`)** valida datos y calcula severidad.
- **Repositorio (`EventoRepository`)** persiste/consulta en PostgreSQL por JDBC.
- **Base de datos PostgreSQL 16** almacena tabla `evento`.

## 3) Stack tecnologico
- **Backend:** Java 21, Ant, Gson, PostgreSQL JDBC
- **API HTTP:** `com.sun.net.httpserver.HttpServer`
- **Base de datos:** PostgreSQL 16
- **Contenedores:** Docker + Docker Compose
- **Mobile:** React Native (Expo), TypeScript
- **Tests:** JUnit 5 (integracion API + DB)

## 4) Flujo de datos
1. Cliente envia `POST /eventos` con `dispositivoId`, `tipoEvento`, `valor`.
2. Controlador parsea JSON y delega en servicio.
3. Servicio:
   - valida campos y rangos por tipo,
   - calcula `severidad`,
   - asigna `fechaHora` en UTC.
4. Repositorio inserta en tabla `evento`.
5. API responde `201` con evento persistido.
6. Cliente puede consultar `GET /eventos` para recuperar lista ordenada por fecha desc.

## 5) Endpoints disponibles
Base URL local: `http://localhost:8080`

### `POST /eventos`
Crea un evento.

**Request JSON**
```json
{
  "dispositivoId": "SEN-01",
  "tipoEvento": "temperatura",
  "valor": 31.4
}
```

**Response 201**
```json
{
  "id": 1,
  "dispositivoId": "SEN-01",
  "tipoEvento": "temperatura",
  "valor": 31.4,
  "severidad": "advertencia",
  "fechaHora": "2026-04-13T15:00:00Z"
}
```

**Errores**
- `400` solicitud invalida (campos faltantes, tipo invalido, rango invalido)
- `500` error interno o de base de datos

### `GET /eventos`
Lista todos los eventos (orden: `fecha_hora DESC, id DESC`).

**Response 200**
```json
[
  {
    "id": 1,
    "dispositivoId": "SEN-01",
    "tipoEvento": "temperatura",
    "valor": 31.4,
    "severidad": "advertencia",
    "fechaHora": "2026-04-13T15:00:00Z"
  }
]
```

## 6) Reglas de severidad
Tipos soportados: `temperatura`, `bateria`, `conexion`.

- **temperatura**
  - `valor > 30` -> `advertencia`
  - caso contrario -> `normal`
- **bateria**
  - `valor < 20` -> `critico`
  - caso contrario -> `normal`
- **conexion**
  - `valor == 0` -> `critico`
  - `valor == 1` -> `normal`

Validaciones de entrada:
- `dispositivoId`: obligatorio, no vacio
- `tipoEvento`: obligatorio y dentro de los tipos validos
- `valor`: obligatorio
- rango por tipo:
  - `conexion`: solo `0` o `1`
  - `bateria`: entre `0` y `100`
  - `temperatura`: sin rango extra

## 7) Estructura general del sistema
```text
AgrotiPoC/
  backend/
    src/com/agromonitor/
      Main.java
      controller/EventoController.java
      service/EventoService.java
      repository/EventoRepository.java
      config/DbConfig.java
      model/{Evento,TipoEvento,Severidad}.java
      dto/CrearEventoRequest.java
    test/com/agromonitor/integration/EventoApiIntegrationTest.java
    schema.sql
    build.xml
    docker-compose.yml
    Dockerfile
  mobile/
    src/api/eventosClient.ts
    src/types/evento.ts
    App.tsx
```

## 8) Como levantar el entorno

### Opcion recomendada (Docker Compose)
Desde `backend/`:
```bash
docker compose up --build
```

Servicios:
- API: `http://localhost:8080/eventos`
- PostgreSQL: `localhost:15432` (db `agromonitor`, user `agro`, pass `agro`)

### Opcion local con Ant (sin Docker para backend)
En `backend/`:
```bash
ant run
```
Variables opcionales:
- `JDBC_URL` (default `jdbc:postgresql://localhost:15432/agromonitor`)
- `DB_USER` (default `agro`)
- `DB_PASSWORD` (default `agro`)
- `PORT` (default `8080`)

## 9) Casos de prueba de integracion basicos
Cobertura actual de `EventoApiIntegrationTest`:

1. **Alta valida + lectura**
   - `POST` valido devuelve `201`
   - luego `GET` devuelve el evento persistido
2. **Tipo de evento invalido**
   - devuelve `400`
   - no persiste registros
3. **Valor faltante**
   - devuelve `400`
   - no persiste registros
4. **Valor invalido para conexion (`2`)**
   - devuelve `400`
   - no persiste registros
5. **`dispositivoId` vacio**
   - devuelve `400`
   - no persiste registros
6. **Calculo de severidad**
   - verifica combinaciones temperatura/bateria/conexion y severidad esperada
