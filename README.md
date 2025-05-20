# Project Introduction

This project is a high-performance review and coupon system inspired by the "HeiMa DianPing" course. It is built with Spring Boot, MyBatis-Plus, and Redis, and demonstrates practical solutions for high-concurrency scenarios such as distributed locking, Redis Streams, cache penetration/breakdown/avalanche, and more. The system includes core features like user login, merchant reviews, following, flash sale coupons, blog publishing, and commenting. The frontend resources are located in `src/main/resources/nginx-1.18.0`.

This project is suitable for learning enterprise-level backend development and hands-on practice with Redis and distributed systems.

## Features

- **User Authentication:** Secure login and registration with session/token management.
- **Merchant Reviews:** Users can post, view, and interact with shop reviews.
- **Follow System:** Users can follow/unfollow others and view followed users’ activities.
- **Flash Sale Coupons:** High-concurrency voucher (seckill) system using Redis Streams and distributed locks.
- **Blog System:** Users can publish blogs, comment, and interact with posts.
- **Commenting:** Nested comment support for blogs and shops.
- **High-Concurrency Solutions:** Implements distributed locking, cache strategies (penetration, breakdown, avalanche), and Redis Stream for order processing.
- **Frontend Resources:** Located in `src/main/resources/nginx-1.18.0`.

## Technology Stack

- **Backend:** Java, Spring Boot, MyBatis-Plus
- **Database:** MySQL
- **Cache & Messaging:** Redis (including Streams, distributed locks, caching)
- **Frontend:** Static resources (HTML/CSS/JS) in the `nginx-1.18.0` directory
- **Build Tool:** Maven

## Project Structure

- `src/main/java/com/hmdp/`
  - `controller/` — RESTful API controllers
  - `service/` — Business logic and interfaces
  - `entity/` — Data models/entities
  - `mapper/` — MyBatis-Plus mappers for database access
  - `utils/` — Utility classes (e.g., cache client, password encoder)
  - `config/` — Spring and MyBatis configuration
- `src/main/resources/`
  - `application.yaml` — Main configuration file
  - `mapper/` — MyBatis XML mapping files
  - `nginx-1.18.0/` — Frontend static resources

## Getting Started

1. **Clone the repository:**
   ```sh
   git clone https://github.com/cs001020/hmdp.git
   ```
2. **Switch to the initial branch for hands-on practice:**
   ```sh
   git checkout init
   ```
3. **Configure your database and Redis in `application.yaml`.**
4. **Start the backend application:**
   - Use your IDE or run with Maven:  
     `mvn spring-boot:run`
5. **Serve the frontend resources using nginx or your preferred static server.**

## Notes

- The `master` branch contains the full-featured code; the `init` branch is for step-by-step learning.
- For high-concurrency features (like seckill), ensure Redis is running and properly configured.
- If you encounter Redis Stream errors on the `master` branch, initialize the stream group:
  ```sh
  XGROUP CREATE stream.orders g1 $ MKSTREAM
  ```
