# Mono - Communicator Server

Spring Boot backend for the Mono instant messaging application.

## Features

- **Real-time chat** — WebSocket (STOMP) messaging per room, broadcast to `/topic/room.{id}`
- **Rooms** — create, list, and join named chat rooms; per-room message history
- **File transfer** — upload and download files (max 10 MB) via REST
- **Authentication** — register/login with BCrypt-hashed passwords; session token (Bearer) auth
- **Message persistence** — all chat messages stored in a relational database
- **API docs** — Swagger UI at `/swagger-ui.html`

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.4 |
| REST | Spring Web MVC |
| WebSocket | Spring WebSocket + STOMP |
| ORM | Spring Data JPA + Hibernate |
| Auth | jBCrypt + UUID session tokens |
| DB (dev) | H2 (file-based) |
| DB (prod) | Azure SQL (SQL Server) |
| File storage (dev) | Local filesystem (`uploads/`) |
| File storage (prod) | Azure Blob Storage |
| API docs | SpringDoc OpenAPI 3 |
| Monitoring | Spring Actuator |
| Build | Maven |
| Utilities | Lombok |

## API Endpoints

### Auth
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Create account (`username`, `password` ≥ 8 chars) |
| `POST` | `/api/auth/login` | Returns `sessionToken` |

All other endpoints require `Authorization: Bearer <sessionToken>`.

### Rooms
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/rooms` | List all rooms with online user count |
| `POST` | `/api/rooms` | Create a room (`name`) |
| `POST` | `/api/rooms/{id}/join` | Join a room |
| `GET` | `/api/rooms/{id}/messages` | Get message history (oldest first) |

### Files
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/files/upload` | Upload a file (multipart, max 10 MB) |
| `GET` | `/api/files/{filename}` | Download a file |

### WebSocket
Connect to `/ws`, then send to `/app/chat.sendMessage`.  
Subscribe to `/topic/room.{roomId}` for room messages or `/topic/public` for global.

## Running Locally

```bash
./mvnw spring-boot:run
```

Uses H2 file database (`chat-db.mv.db`) and local `uploads/` directory — no extra config needed.

H2 console available at `http://localhost:8080/h2-console`.

## Production (Azure)

Set the following environment variables:

```
SPRING_DATASOURCE_URL=jdbc:sqlserver://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
AZURE_STORAGE_CONNECTION_STRING=...
AZURE_STORAGE_CONTAINER_NAME=...
```

Deployed on **Azure App Service**.
