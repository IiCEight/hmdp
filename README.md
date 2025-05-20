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

## Java后端实现简历描述

### Java后端开发项目经验 —— 高性能点评与优惠券系统

**项目简介：**
独立开发高并发点评与优惠券系统，采用Spring Boot、MyBatis-Plus、Redis等主流技术，支持用户登录、商户点评、关注、秒杀优惠券、博客发布与评论等功能，前后端分离，具备企业级高并发处理能力。

**核心职责与技术亮点：**
- 负责系统整体后端架构设计与核心功能开发，采用分层架构（Controller-Service-Mapper），结构清晰，易于维护。
- 实现用户注册、登录、会话管理，用户信息高效缓存于Redis，提升系统响应速度。
- 设计并实现关注系统，关注关系存储于Redis集合，支持高效的共同关注查询。
- 优化商户点评、博客发布、评论等社交功能，支持点赞、评论等互动。
- 设计并实现高并发秒杀优惠券模块，利用Redis Stream和分布式锁，保障数据一致性与高性能。
- 深入解决缓存穿透、击穿、雪崩等问题，封装CacheClient工具类，提升系统稳定性。
- 使用MyBatis-Plus简化数据库操作，提升开发效率。
- 负责系统配置、数据库设计与性能调优，保障系统高可用与可扩展性。

**技术栈：**
Spring Boot、MyBatis-Plus、Redis、MySQL、Maven
