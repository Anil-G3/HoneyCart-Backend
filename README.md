# ⚙️ HoneyCart — Backend

> Modular e-commerce backend built with Spring Boot.  
> Handles authentication, product management, cart, orders, and payments — production-ready and built to hold up under real usage.

![Status](https://img.shields.io/badge/Status-Live-brightgreen?style=flat)
![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=flat&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=flat&logo=jsonwebtokens&logoColor=white)

---

## 🔗 Links

| | |
|---|---|
| 🖥️ Frontend Repo | [HoneyCart-Frontend](https://github.com/Anil-G3/HoneyCart-Frontend) |
| 📄 API Base URL | `http://localhost:8080/api` |

---

## 🧩 What This Does

HoneyCart is a full-stack e-commerce platform. This backend powers everything:

- **JWT Authentication** — stateless auth with role-based access (Admin & Customer), enforced at the filter level via Spring Security
- **Product Management** — CRUD APIs for products with category support
- **Cart & Orders** — complete cart lifecycle and order processing workflows
- **Payments** — Razorpay integrated for payment processing
- **Secure by design** — every protected route validates the JWT token; Admin and Customer roles get separate access flows

---

## 🔧 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Framework | Spring Boot, Spring MVC, Spring Security |
| Auth | JWT, Web Filters |
| ORM | Hibernate, JDBC |
| Database | MySQL |
| Payments | Razorpay |
| Frontend | React JS (separate repo) |
| Tools | Maven, Postman, Git, Docker |

---

## 🔐 Auth Flow

1. User registers or logs in → server issues a signed JWT token
2. Token is stored in an **HTTP Cookie** — frontend never handles it manually
3. Cookie is automatically sent with every subsequent request by the browser
4. Spring Security filter reads and validates the token from the cookie on every protected route
5. Role check — `ADMIN` and `CUSTOMER` routes are enforced separately

```
POST /api/auth/register   → Register a new user
POST /api/auth/login      → Sets JWT token in HTTP Cookie
POST /api/auth/logout     → Clears the cookie
```

All other routes require the JWT cookie to be present and valid.

---

## 📦 API Overview

| Module | What it covers |
|---|---|
| Auth | Register, Login |
| Products | List, Search, Add, Update, Delete |
| Cart | Add to cart, Update quantity, Remove, View cart |
| Orders | Place order, View order history, Admin order management |
| Payments | Razorpay order creation & payment capture |

> Full endpoint details available via Postman collection — *add your collection link here*

---

## 💳 Payments

Integrated **Razorpay** for end-to-end payment processing — order creation, payment capture, and a webhook-ready architecture for handling payment events.

---

## 🚀 Run Locally

**Prerequisites:** Java 17+, Maven, MySQL

**1. Clone the repo**
```bash
git clone https://github.com/Anil-G3/HoneyCart-Backend.git
cd HoneyCart-Backend
```

**2. Configure the database**

Create a MySQL database and update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/honeycart
spring.datasource.username=your_username
spring.datasource.password=your_password

app.jwt.secret=your_jwt_secret_key
app.jwt.expiration=86400000

razorpay.key.id=your_razorpay_key
razorpay.key.secret=your_razorpay_secret
```

**3. Build & run**
```bash
mvn clean install
mvn spring-boot:run
```

Server starts at `http://localhost:8080`

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/honeycart/
│   │   ├── controller/       # REST controllers
│   │   ├── service/          # Business logic
│   │   ├── repository/       # JPA repositories
│   │   ├── model/            # Entity classes
│   │   ├── dto/              # Request/Response DTOs
│   │   ├── security/         # JWT filter, Spring Security config
│   │   └── config/           # App configuration
│   └── resources/
│       └── application.properties
```

---

## 👨‍💻 Author

**G Anil Kumar** — Java Developer  
[GitHub](https://github.com/Anil-G3) · [LinkedIn](https://linkedin.com/in/anil-g3) · [Portfolio](https://portfolio-anil-20.netlify.app)
