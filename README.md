# 🎫 Ticketing App

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

A ticketing system made with Spring Boot and React for efficient issue tracking and resolution management.

## ✨ Features

- 🎯 **Ticket Management**: Create, read, and manage support tickets
- 🔄 **Status Tracking**: Track ticket status (OPEN, IN_PROGRESS, CLOSED)
- 💡 **Solution Management**: Add solutions and automatically close tickets
- 🚀 **RESTful API**: Clean and intuitive REST endpoints
- 🛡️ **Error Handling**: Comprehensive exception handling
- 📊 **Database Integration**: H2 database with JPA/Hibernate

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/loickcherimont/ticketing-app.git
   cd ticketing-app
   ```

2. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access the API**
   ```
   http://localhost:8080/api/tickets
   ```

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/tickets
```

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/tickets` | Get all tickets |
| `GET` | `/api/tickets/{id}` | Get ticket by ID |
| `POST` | `/api/tickets` | Create new ticket |
| `PATCH` | `/api/tickets/{id}/solve` | Solve ticket with solution |
| `PATCH` | `/api/tickets/{id}/in-progress` | Set ticket in progress |

### Examples

#### Create a Ticket
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Database Connection Issue",
    "description": "Unable to connect to the database",
    "status": "OPEN"
  }'
```

#### Solve a Ticket
```bash
curl -X PATCH http://localhost:8080/api/tickets/1/solve \
  -H "Content-Type: application/json" \
  -d '{
    "solution": "Updated database configuration and restarted the service"
  }'
```

#### Set Ticket In Progress
```bash
curl -X PATCH http://localhost:8080/api/tickets/1/in-progress
```

## 🏗️ Architecture

```
src/main/java/dev/loickcherimont/ticketing_app/
├── controller/          # REST controllers
├── service/            # Business logic
│   └── impl/          # Service implementations
├── repository/         # Data access layer
├── model/             # Entity models
└── exception/         # Custom exceptions
```

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.x
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA
- **Build Tool**: Maven
- **Language**: Java 17



This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 🔐 License

<div align='center'>

&copy; 2025 [Loïck Chérimont](https://github.com/loickcherimont 'Loïck Chérimont').  
Licensed under the [MIT License](./LICENSE 'MIT License').
</div>