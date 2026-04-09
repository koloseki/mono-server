# Mono - Comunicator Client-Server
Spring server for communication

## Project Goal

The main goal of the **Mono** project is to develop a functional instant messaging application based on a client-server architecture. The application is designed to enable reliable information exchange between users.

**Key features:**
* **Real-time chat:** Instant text messaging using the WebSocket protocol (STOMP).
* **File transfer:** The ability to securely send and receive files (e.g., photos, documents) via REST API (HTTP) requests.
* **Data archiving:** Persistent storage of conversation history and user data in a relational database (H2).

This project serves as a practical application of software design patterns, learning the Spring Boot framework, and understanding asynchronous network communication in Java.

## Technologies Used

**Backend (Server):**
* **Java 25:** Core programming language.
* **Spring Boot:** Main framework for building the server application.
* **Spring Web (REST):** For handling HTTP requests (file uploads/downloads).
* **Spring WebSocket & STOMP:** For real-time, bi-directional text messaging.
* **Spring Data JPA:** For object-relational mapping (ORM) and database interactions.
* **H2 Database:** Relational database used for storing users and message history.
* **Maven:** Project build and dependency management.
* **Lombok:** Boilerplate code reduction.

**Deployment & Hosting:**
* **Localhost:** Initial development, testing, and debugging environment.
* **Google Cloud Platform (GCP):** Target production cloud environment.
