# Data Structure Lab Project

A full-stack **Smart Parking** web application. Drivers register/login, add vehicles, reserve spots in a lot, end their session to get a fee, and pay. The **Java (Spring Boot)** backend is built around classic data-structure concepts so the project doubles as a hands-on demonstration of how each structure speeds up a real backend operation.

## Stack

| Layer    | Technology                                            |
| -------- | ----------------------------------------------------- |
| Frontend | HTML + CSS + Vanilla JS, Leaflet for the map          |
| Backend  | **Java 17 + Spring Boot 3.2** (REST + JdbcTemplate)   |
| Database | MySQL / MariaDB                                       |
| Auth     | BCrypt password hashing (Spring Security Crypto)      |

## Data Structures used in the backend

Every structure below is wired into a real endpoint, not just a demo.

| Structure                    | File                                       | Where it is used                                                                                                                            |
| ---------------------------- | ------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------- |
| **HashMap + HashSet**        | `datastructures/SpotOccupancyIndex.java`   | `Map<lotId, Set<spotLabel>>` for **O(1)** "is this spot taken?" checks on every reservation.                                                |
| **PriorityQueue (min-heap)** | `datastructures/UnpaidPaymentQueue.java`   | Per-driver heap of unpaid logs ordered by `log_id`, so **Pay All** clears the **oldest debt first** in O(log n) per pop.                    |
| **Stack (`ArrayDeque`)**     | `datastructures/DriverActionStack.java`    | Bounded LIFO of the last 20 actions per driver (REGISTER, LOGIN, ADD_VEHICLE, START_SESSION, END_SESSION, PAY ...). Push/pop are O(1).      |
| **LinkedHashMap (LRU)**      | `datastructures/LRUCache.java`             | Custom Least-Recently-Used cache for parking-lot rows so frequent reads do not hit MySQL every time. Both `get` and `put` are O(1).         |
| **TreeMap**                  | `config/DataStructureRegistry.java`        | Sorted map of all parking lots keyed by `lot_id`, refreshed on startup; gives O(log n) lookups and ordered iteration.                       |

You can inspect them at runtime:

* `GET /admin/ds/stats`               — current sizes of the LRU cache and TreeMap
* `GET /admin/actions/{driverId}`     — most recent items on the action stack

## Run instructions

### 1. Database

Open `sql/data_structure_lab.sql` in HeidiSQL or MySQL Workbench and run the whole script. It creates the `data_structure_lab` database, all tables, the triggers that enforce single-active-session and unpaid-blocking, and seeds a demo user.

### 2. Backend (Java + Spring Boot)

Requires **JDK 17+** and **Maven 3.6+**.

```bash
cd backend
mvn clean package
java -jar target/data-structure-lab-project.jar
```

(or `mvn spring-boot:run` during development).

The backend listens on `http://localhost:3000`.

### 3. Frontend

The Spring Boot backend serves the frontend automatically at `/`. Just open:

```
http://localhost:3000
```

## Demo credentials

| Field    | Value                |
| -------- | -------------------- |
| Email    | `mahmoud@gmail.com`  |
| Password | `1234`               |

(Click the **"Try Demo"** button on the login screen to auto-fill these.)

## Project layout

```
Data Structure Lab Project/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/dslab/parking/
│       │   ├── DataStructureLabApplication.java   ← Spring Boot entry point
│       │   ├── config/                            ← CORS + DS registry
│       │   ├── controller/                        ← REST endpoints
│       │   ├── datastructures/                    ← Custom DS (LRU, Stack, PQ, ...)
│       │   ├── dto/
│       │   ├── model/
│       │   ├── repository/                        ← JdbcTemplate DAOs
│       │   └── service/                           ← Business logic that uses the DS
│       └── resources/application.properties
├── frontend/
│   ├── index.html
│   └── assets/{app.js, styles.css}
├── sql/
│   ├── data_structure_lab.sql                     ← schema + triggers + seed data
│   └── complex_queries.sql                        ← rubric queries (JOIN/GROUP BY/UNION/nested)
└── README.md
```

## REST endpoints (kept identical to the original API so the frontend works unchanged)

| Method | Path                            | Notes                                                                  |
| ------ | ------------------------------- | ---------------------------------------------------------------------- |
| POST   | `/auth/register`                | Create account (BCrypt-hashed password)                                |
| POST   | `/auth/login`                   |                                                                        |
| GET    | `/vehicle/{driverId}`           |                                                                        |
| POST   | `/vehicle/add`                  |                                                                        |
| DELETE | `/vehicle/{plate}` `/vehicles/{plate}` | Both supported for compatibility                                |
| GET    | `/cards/{driverId}`             |                                                                        |
| POST   | `/cards/add`                    |                                                                        |
| POST   | `/cards/set-default`            |                                                                        |
| DELETE | `/cards/{cardId}?driver_id=...` |                                                                        |
| GET    | `/lots/nearby`                  | LRU cache + TreeMap                                                    |
| GET    | `/session/active_spots?lot_id=` | Refreshes `SpotOccupancyIndex`                                         |
| GET    | `/session/has_unpaid?driver_id=`|                                                                        |
| GET    | `/session/active?driver_id=&plate_no=` |                                                                 |
| POST   | `/session/start`                | O(1) occupancy check via `SpotOccupancyIndex`, push to `DriverActionStack` |
| POST   | `/session/end`                  | Computes fee, releases spot in occupancy index                         |
| GET    | `/payments/due/{driverId}`      |                                                                        |
| POST   | `/payment/pay`                  | Removes one entry from `UnpaidPaymentQueue`                            |
| POST   | `/payment/pay_all`              | Drains the heap oldest-first (O(log n) per pop)                        |
| GET    | `/logs/driver/{driverId}`       |                                                                        |
| GET    | `/admin/drivers`                |                                                                        |
| GET    | `/admin/stats`                  |                                                                        |
| GET    | `/admin/actions/{driverId}`     | **NEW:** dump the per-driver action stack                              |
| GET    | `/admin/ds/stats`               | **NEW:** report the live size of the in-memory data structures         |
| GET    | `/reports/lot_summary`          | Aggregate + GROUP BY                                                   |
| GET    | `/reports/unpaid_above_average` | Nested query                                                           |
| GET    | `/reports/plates_union`         | Set operation (UNION)                                                  |

## Why these data structures here?

* **HashMap of HashSets** for occupancy: deciding "is spot P017 in lot 3 free?" must be fast — drivers click around the spot grid in real time. Hashing reduces it to a constant-time probe.
* **PriorityQueue** for "Pay All": fairness rule = clear the oldest debt first, otherwise we’d need to re-sort the unpaid list on every read.
* **Stack** for the action log: recency dominates ("show me what I just did"). LIFO is the natural fit; capping the size keeps memory bounded.
* **LRU cache** for parking lots: the same handful of lots get queried by everyone; caching them avoids a DB round trip per page load while still evicting cleanly when something new is read.
* **TreeMap** for sorted lookups by lot id: gives both ordered iteration (for the dropdown) and O(log n) `get`.
