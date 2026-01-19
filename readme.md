# 🏆 Fantasy Premier League Pro

A comprehensive full-stack application providing real-time Premier League statistics, fixtures, standings, and injury reports. Built with a robust **Java Spring Boot** backend and a dynamic **Angular** frontend.

## 📂 Project Structure

The project is organized as a monorepo for clear separation of concerns:

- `backend/`: Spring Boot API handling data persistence, match simulation, and external API synchronization.
- `frontend/fantasy-fpl-frontend/`: Angular SPA providing a high-contrast dashboard for league data.

---

## 🚀 Getting Started

### 1. Backend Setup (Java Spring Boot)

**Requirements:**
- Java 21 or higher
- Maven

**Configuration:**
The backend uses an external sports API for real-time data. Ensure your configuration is set in:

`backend/src/main/resources/application.properties`

```properties
# League Configuration (Default: 152 for Premier League)
fpl.league.id=152

# Database Configuration (Development: H2 In-Memory)
spring.datasource.url=jdbc:h2:mem:fpl_db
spring.jpa.hibernate.ddl-auto=update
```

**Running the Backend:**

Navigate to the backend directory:
```
cd backend
```

Build the project:
```
./mvnw clean install
```

Run the application:
```
./mvnw spring-boot:run
```

API will be available at:
```
http://localhost:8080/api/league
```

---

### 2. Frontend Setup (Angular)

**Requirements:**
- Node.js (LTS version)
- Angular CLI

**Running the Frontend:**

Navigate to the frontend directory:
```
cd frontend/fantasy-fpl-frontend
```

Install dependencies:
```
npm install
```

Start the development server:
```
ng serve
```

Dashboard will be available at:
```
http://localhost:4200
```

---

## 🛠 Features

- **Real-time Standings:** Automatically syncs official Premier League tables including Wins, Draws, Losses, and Goal Difference.
- **Fixture Syncing:** Fetch specific matchweeks from the API and update local results.
- **Fail-safe Injury Tracking:** A two-tier system that provides injury statuses even if the dedicated medical API is unavailable.
- **Club Hub:** Detailed views for every team, including their current squad and recent form.
- **High-Contrast UI:** A sleek, dark-themed dashboard designed for readability and professional aesthetics.

---

## 🧪 API Endpoints

Method | Endpoint | Description
------ | -------- | -----------
POST | /api/league/sync-teams | Syncs all clubs and players from the API
POST | /api/league/sync-standings | Updates the league table with official stats
GET | /api/league/fixtures/sync | Syncs fixtures for a specific date range
POST | /api/league/sync-topscorers | Syncs player goals and assists

---

## 🔒 Git Management

This project uses a centralized root-level `.gitignore` to keep the repository clean of build artifacts.

Ignored paths include:
- `node_modules/`
- `dist/`
- `target/`
- IDE metadata (`.idea/`, `.vscode/`)
