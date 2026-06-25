# 🔗 Designing a Scalable URL Shortener Service Using Redis Caching

A scalable URL shortening service built using **Spring Boot**, **MySQL**, **Spring Data JPA**, and **Redis**. The application converts long URLs into short, shareable links and efficiently redirects users to the original URLs while leveraging Redis caching for high performance and reduced database load.

---

## 📖 Project Overview

This project aims to design and implement a scalable URL shortening service capable of generating unique short URLs, storing URL mappings securely, and redirecting users efficiently. Redis caching is integrated to improve response time by minimizing database lookups for frequently accessed URLs.

---

## 🚀 Features

- Generate unique short URLs
- Redirect short URLs to original URLs
- Store URL mappings in MySQL
- Redis caching for faster retrieval
- RESTful API implementation
- Responsive web interface using Thymeleaf
- Layered architecture (Controller, Service, Repository)

---

## 🏗️ System Architecture

```
User
   │
   ▼
Spring Boot Controller
   │
   ▼
Service Layer
   │
   ├─────────────┐
   ▼             ▼
Redis Cache    MySQL Database
   │             │
   └──────┬──────┘
          ▼
 URL Mapping
          │
          ▼
 URL Redirection
```

---

## 🛠️ Technology Stack

### Backend
- Java 26
- Spring Boot
- Spring Data JPA
- Hibernate

### Database
- MySQL

### Cache
- Redis

### Frontend
- Thymeleaf
- HTML5
- CSS3
- JavaScript

### Build Tool
- Maven

### Development Tools
- VS Code
- Git
- GitHub
- Postman
- MySQL Workbench

---

## 📂 Project Structure

```
src
 ├── controller
 ├── service
 ├── repository
 ├── entity
 ├── dto
 ├── resources
 │     ├── templates
 │     └── application.properties
 └── UrlShortenerApplication.java
```

---

## ⚙️ Installation

### Clone Repository

```bash
git clone https://github.com/YOUR_USERNAME/url-shortener.git
```

### Navigate

```bash
cd url-shortener
```

### Configure Database

Create MySQL database

```sql
CREATE DATABASE url_shortener;
```

Update

```
application.properties
```

with your MySQL credentials.

---

### Build Project

```bash
./mvnw clean compile
```

---

### Run Application

```bash
./mvnw spring-boot:run
```

---

## 🌐 Access Application

```
http://localhost:8081
```

---

## 📊 Database Schema

### Table: short_urls

| Column | Type |
|---------|------|
| id | BIGINT |
| original_url | LONGTEXT |
| short_code | VARCHAR(10) |
| created_at | DATETIME |
| expires_at | DATETIME |

---

## 🔄 Workflow

1. User enters a long URL.
2. System generates a unique short code.
3. URL mapping is stored in MySQL.
4. Frequently accessed mappings are cached in Redis.
5. User visits the short URL.
6. System retrieves the original URL from Redis or MySQL.
7. User is redirected to the original website.

---

## 📈 Future Enhancements

- Click Analytics
- Custom Short URLs
- User Authentication
- URL Expiration
- QR Code Generation
- Distributed Redis Cluster
- Docker Deployment
- Kubernetes Support

---

## 👨‍💻 Author

**Your Name**

B.Tech CSE (Cyber Security)

Institute of Aeronautical Engineering

---

## 📄 License

This project is licensed under the MIT License.
