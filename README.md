# 🎧 Ticketing API

## 🌱 About

This is **back-end system** made with Spring Boot for efficient issue tracking and resolution management.

## 🖥️ Tech stack

- **Back-End :** Java 21, Spring (Spring Boot, Spring MVC, Spring Data JPA), API REST
- **Database :** H2 (SQL)
- **DevOps / DevTools :** Git / GitHub, Maven, Linux (Fedora) 

### Prerequisites

Before project running, you'll need :  

*Actually, you won't need more dependencies to run correctly this API.*

## 🚀 Setup

1. Clone the repository using :

```bash
git clone https://github.com/loickcherimont/ticketing-api.git
```

2. Go in the project and run it :
```bash
cd ticketing-api
./mvnw clean spring-boot:run
```

3. Access to API with route : http://localhost:8080/api/tickets

## ▶️ Usage

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/tickets` | Get all tickets |
| `GET` | `/api/tickets/{id}` | Get ticket by ID |
| `POST` | `/api/tickets` | Create new ticket |
| `PATCH` | `/api/tickets/{id}/solve` | Solve ticket with solution |
| `PATCH` | `/api/tickets/{id}/in-progress` | Set ticket in progress |

## 📄 API Documentation

Try the Swagger UI here : https://ticketing-api-production-92ac.up.railway.app/swagger-ui/index.html

![Preview API documentation](.github/endpoints-docs.png 'API docs with Swagger UI | Ticket API')



## 🔑 License

<div align="center">Copyright &copy; 2026 | Loick CHERIMONT | All Rights Reserved.</div>
