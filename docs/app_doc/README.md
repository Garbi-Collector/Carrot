# Sistema Carrot Chat

> Sistema de chat en tiempo real desarrollado con Spring Boot y WebSocket

## Presentación del Equipo

- **Gabriel Scipioni (405262)** - Garbi-Collector
  - GitHub: [@Garbi-Collector](https://github.com/Garbi-Collector)

---

## Descripción del Proyecto

**Carrot Chat** es una aplicación de mensajería en tiempo real que utiliza WebSocket para comunicación bidireccional entre clientes y servidor. El backend está construido con Spring Boot, proporcionando una arquitectura robusta, escalable y fácil de mantener.

### Características Principales

- Chat en tiempo real mediante WebSocket
- Sistema de autenticación y manejo de sesiones
- API REST completa para gestión de datos
- Arquitectura limpia con separación de responsabilidades
- Base de datos relacional MySQL
- Containerización con Docker

---

## Tecnologías y Herramientas Utilizadas

### Lenguajes y Frameworks

- **Java 17** - Lenguaje de programación principal
- **Spring Boot 3.1.3** - Framework backend
  - Spring Web
  - Spring WebSocket
  - Spring Data JPA
  - Spring Security

### Base de Datos

- **MySQL 8.0.38** - Sistema de gestión de base de datos relacional

### Herramientas de Desarrollo

- **Git** - Control de versiones
- **IntelliJ IDEA** - IDE principal
- **DataGrip** - Gestión de base de datos

### Modelado y Documentación

- **DrawIO** - Diagramas UML y arquitectura
- **Swagger/OpenAPI** - Documentación de API
- **JavaDoc** - Documentación de código

---

## Arquitectura del Sistema

El proyecto sigue una arquitectura en capas:

```
┌─────────────────────────────────────┐
│        Capa de Presentación         │
│     (Controllers, WebSocket)        │
└─────────────────────────────────────┘
              ⬇
┌─────────────────────────────────────┐
│        Capa de Negocio              │
│          (Services)                 │
└─────────────────────────────────────┘
              ⬇
┌─────────────────────────────────────┐
│      Capa de Persistencia           │
│    (Repositories, Entities)         │
└─────────────────────────────────────┘
              ⬇
┌─────────────────────────────────────┐
│        Base de Datos MySQL          │
└─────────────────────────────────────┘
```

---

## Instalación y Configuración

### Prerrequisitos

- Java 17
- Maven 3.6+
- MySQL 8.0+

## Documentación

Este proyecto cuenta con documentación completa en varios formatos:

- **[Entidades JPA](/entities.md)** - Modelo de datos y relaciones de base de datos
- **[API Specification](/api_doc)** - Documentación Swagger/OpenAPI de todos los endpoints
- **[Diagramas UML](/diagrams/class_diagram.md)** - Diagramas de clases, componentes y secuencias
- **[JavaDoc](/java_doc)** - Documentación generada del código fuente

---

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/
│   │   └── com/carrot/chat/
│   │       ├── config/        # Configuraciones
│   │       ├── controller/    # Controladores REST
│   │       ├── dto/           # Data Transfer Objects
│   │       ├── entity/        # Entidades JPA
│   │       ├── exception/     # Manejo de excepciones
│   │       ├── models/        # Modelos adicionales
│   │       ├── repository/    # Acceso a datos
│   │       ├── security/      # Seguridad y JWT
│   │       ├── service/       # Lógica de negocio
│   │       └── websocket/     # Comunicación WebSocket
│   └── resources/
│       ├── application.properties
│       └── data.sql
└── test/                      # Tests unitarios
```

---

## Contribuciones

Este es un proyecto personal desarrollado para aprendizaje y demostración de habilidades.

---

## Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

---

## Contacto

**Gabriel Scipioni**
- GitHub: [@Garbi-Collector](https://github.com/Garbi-Collector)

---

<div align="center">
  <p><strong>Desarrollado con 🥕 y ☕</strong></p>
  <p><em>Carrot Chat - Conectando personas en tiempo real</em></p>
</div>