# College Fest & Hackathon Management System (HackArena)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-darkgreen.svg)](https://www.thymeleaf.org/)
[![License](https://img.shields.io/badge/License-Academic-lightgrey.svg)]()

A robust, enterprise-grade Spring Boot management platform designed for college festivals, hackathons, and technical competitions. It orchestrates role-based access for Administrators, Panel Judges, and Participants, automated team registration with leader onboarding, capacity enforcement, panel judge assignment, multi-dimensional scoring, and an event-specific leaderboard featuring multi-level tiebreaking.

The application features a **Linear-inspired minimalist SaaS dashboard** built with clean HTML5, custom CSS design tokens, and server-rendered Thymeleaf—free of heavy frontend framework bloat.

---

## Table of Contents

1. [Role-Based Architecture & Portals](#role-based-architecture--portals)
2. [Features](#features)
3. [Tech Stack](#tech-stack)
4. [Architecture & Directory Structure](#architecture--directory-structure)
5. [Prerequisites](#prerequisites)
6. [Database Configuration](#database-configuration)
7. [Running the Application](#running-the-application)
8. [Leaderboard Scoring & Tiebreaking Logic](#leaderboard-scoring--tiebreaking-logic)
9. [REST API & Postman](#rest-api--postman)
10. [Business Rules & Validation](#business-rules--validation)

---

## Role-Based Architecture & Portals

The platform enforces strict backend permissions via `SessionAuthInterceptor` and dedicated route namespaces:

| Role | Access URL | Allowed Routes | Key Capabilities |
| :--- | :--- | :--- | :--- |
| **Admin** | `/login` (Admin tab) &rarr; `/dashboard` | `/dashboard/**`, `/events/**`, `/teams/**`, `/participants/**`, `/judges/**`, `/scores/**`, `/leaderboard/**`, `/admin/**` | Full system control, event management, judge credential provisioning, track assignments. |
| **Participant** | `/login` (Participant tab) &rarr; `/portal/dashboard` | `/portal/**` | Browse competitions, register own team, manage roster members, view event leaderboard. |
| **Judge** | `/login` (Judge tab) &rarr; `/judge/dashboard` | `/judge/**` | View assigned tracks, evaluate eligible teams, submit/edit multi-dimensional scores (0–10). |

*Note: Passwords for judges and participants are never stored in plaintext—they are securely hashed with salted SHA-256 via `PasswordUtil`.*

---

## Features

### 🔐 Multi-Role Authentication & Access Control
- Dedicated role selection tabs on `/login` (Admin, Participant, Judge).
- Server-side route guards enforcing session integrity and preventing cross-role privilege escalation.
- Admin portal tools to provision login credentials and assign tracks to panel judges.

### 👥 Participant Portal
- **Dashboard Overview**: Summary of registered team, competition track, teammates, and live event leaderboard.
- **Event Catalog**: Visual catalog of all festival events, venues, schedules, and team size limits.
- **Team Management**: Register a team (auto-assigns the participant as leader) and invite teammates up to event capacity limits.

### ⚖️ Judge Portal & Scored Evaluations
- **Assigned Track Filtering**: Judges only see and evaluate teams in events they have been assigned to by the Administrator.
- **5-Dimensional Scoring Model** (0.0 to 10.0 scale):
  - Innovation & Originality
  - Technical Implementation (Primary Tiebreaker)
  - Presentation & Pitch
  - UI / UX Design
  - Overall Impression
- **Idempotent Scoring**: Updating an existing evaluation overwrites the previous score without duplicate rows.

### 🏆 Event-Specific Leaderboard with Multi-Level Tiebreaking
- **Event Filter**: Dedicated dropdown dynamically populated with competitions.
- **Tiebreaking Formula**:
  1. Average Composite Score (`DESC`)
  2. Average Technical Implementation Score (`DESC`)
  3. Average Innovation Score (`DESC`)
  4. True Tie: Teams sharing identical marks across all criteria are awarded the same rank.
- **Podium Visuals**: Gold, Silver, and Bronze badges highlighting top-performing teams.

### 📅 Event Scheduling & Conflict Prevention
- Complete CRUD operations for competitions, cultural events, and hackathons.
- Automated venue-time conflict detection preventing overlapping schedules.

---

## Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.5.4 (Spring Web MVC, Spring Data JPA, Hibernate, Bean Validation) |
| **Frontend** | Thymeleaf 3, HTML5, Custom Vanilla CSS (Design Tokens, Responsive Grid) |
| **Database** | MySQL 8.0+ with HikariCP connection pooling |
| **Security** | Role-aware `HttpSession` interceptors, Salted SHA-256 hashing |
| **Build & Tooling** | Apache Maven 3.9+, IntelliJ IDEA |
| **Testing** | JUnit 5, Mockito, Spring Boot Starter Test |

---

## Architecture & Directory Structure

```
college-fest-management/
├── pom.xml                                      # Maven build dependencies & plugins
├── README.md                                    # Project documentation
├── postman/
│   └── College-Fest-Management.postman_collection.json # Ready-to-import Postman suite
└── src/
    ├── main/
    │   ├── java/com/collegefest/management/
    │   │   ├── CollegeFestManagementApplication.java  # Main application entry point
    │   │   ├── api/                             # REST controllers serving JSON
    │   │   ├── config/                          # SessionAuthInterceptor & WebConfig
    │   │   ├── dto/                             # Data Transfer Objects (LeaderboardEntry)
    │   │   ├── entity/                          # JPA Entities (Admin, Judge, Participant, Team, Score, Event)
    │   │   ├── exception/                       # Custom exceptions & GlobalExceptionHandler
    │   │   ├── repository/                      # Spring Data JPA Repository interfaces
    │   │   ├── service/                         # Business logic, conflict checks, scoring & tiebreakers
    │   │   ├── util/                            # PasswordUtil (salted SHA-256)
    │   │   └── web/                             # Spring MVC Controllers (Admin, JudgePortal, ParticipantPortal)
    │   └── resources/
    │       ├── application.properties           # Database & server configuration
    │       ├── schema.sql                       # Database initialization
    │       ├── sample-data.sql                  # Optional demo dataset
    │       ├── static/css/style.css             # Linear-inspired design system
    │       └── templates/                       # Thymeleaf templates & fragments
    │           ├── admin/                       # Admin credential & judge assignment views
    │           ├── fragments/                   # Modular navbars (Admin, Judge, Participant)
    │           ├── judge/                       # Judge portal (dashboard, scoring, forms)
    │           └── portal/                      # Participant portal (dashboard, events, teams)
    └── test/
        └── java/com/collegefest/management/     # Unit and integration test suites
```

---

## Prerequisites

Before running the application, ensure you have:
- **Java Development Kit (JDK) 21** or higher (`java -version`)
- **Apache Maven 3.9+** (or IntelliJ's bundled Maven)
- **MySQL Server 8.0+** running locally on port `3306`
- **IntelliJ IDEA** (Community or Ultimate) or any preferred Java IDE

---

## Database Configuration

### 1. Create the Database
Log into your MySQL instance and run:

```sql
CREATE DATABASE college_fest_db;
```

> **Note:** Hibernate creates and updates tables automatically on startup (`spring.jpa.hibernate.ddl-auto=update`), and `schema.sql` establishes base tables and default users.

### 2. Configure Credentials
Check `src/main/resources/application.properties` to ensure credentials match your MySQL setup:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/college_fest_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=ayush
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

---

## Running the Application

### Option A: From IntelliJ IDEA (Recommended)
1. Open IntelliJ IDEA &rarr; **File** &rarr; **Open** &rarr; select the `college-fest-management` folder.
2. Allow Maven to import dependencies.
3. Verify the Project SDK is set to Java 21 (**File** &rarr; **Project Structure** &rarr; **Project** &rarr; **SDK: 21**).
4. Open `src/main/java/com/collegefest/management/CollegeFestManagementApplication.java`.
5. Click the green **Run** icon next to `main` (or press `Shift + F10`).

### Option B: From the Terminal
```bash
mvn spring-boot:run
```

The application starts on port `8080`.

### Default Credentials & Login
Navigate to **`http://localhost:8080`**:
- **Admin**:
  - Role Tab: **Admin**
  - Username: `admin`
  - Password: `admin123`
- **Judge / Participant**:
  - Select the corresponding role tab.
  - Judges can have credentials created or updated via the Admin panel under **Judges &rarr; Assign**.

---

## Leaderboard Scoring & Tiebreaking Logic

1. **Criteria Calculation**:
   $$\text{Final Score} = \frac{\text{Innovation} + \text{Technical} + \text{Presentation} + \text{UI/UX} + \text{Overall}}{5}$$
   Rounded to 2 decimal places.
2. **Aggregation**:
   Teams are evaluated across all assigned panel scores.
3. **Tiebreaker Hierarchy**:
   - Primary: Highest Average Composite Score
   - Secondary: Highest Average Technical Implementation Score
   - Tertiary: Highest Average Innovation Score
   - True Tie: Teams identical in all three dimensions share the exact same rank.

---

## REST API & Postman

The application exposes a RESTful JSON API (`/api/**`) for automated testing and client integration.

| Resource | HTTP Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **Events** | `GET` | `/api/events` | List all events |
| | `POST` | `/api/events` | Create a new event |
| **Teams** | `GET` | `/api/teams` | List all teams |
| | `POST` | `/api/teams?eventId={id}` | Register a new team |
| **Participants** | `GET` | `/api/participants` | List all participants |
| | `POST` | `/api/participants?teamId={id}` | Add a participant |
| **Judges** | `GET` | `/api/judges` | List all judges |
| | `POST` | `/api/judges` | Register a new judge |
| **Scores** | `POST` | `/api/scores?teamId={id}&judgeId={id}` | Submit / update evaluation scores |
| **Leaderboard** | `GET` | `/api/leaderboard` | Get composite global leaderboard |
| | `GET` | `/api/leaderboard?eventId={id}` | Get event-specific ranked leaderboard |

---

## Business Rules & Validation

1. **Venue Conflict Prevention**: Overlapping start/end times in the same venue are strictly rejected.
2. **Team Capacity Limits**: Teams cannot exceed the `maxTeamSize` defined by their event.
3. **Automated Team Leader Registration**: Team leaders are enrolled as Participant #1 automatically.
4. **Judge Event Isolation**: Evaluators can only score teams competing within their assigned tracks.
5. **Secure Authentication**: Hashed password storage with role-based session guards.
