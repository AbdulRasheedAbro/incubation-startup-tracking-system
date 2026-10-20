# Incubation & Startup Tracking System

A Java desktop application for managing startups enrolled in an incubation program — tracking founders, mentors, milestones, meetings, and startup evaluations, all in one place.

Built with **Java 21**, **Swing**, and **PostgreSQL**, following a layered architecture (Model → Repository → Service → UI).

---

## Overview

Incubators help early-stage startups grow by providing mentorship, structure, and time to build. Managing this process manually — spreadsheets, emails, scattered notes — makes it hard to track progress, assign mentors, or evaluate startups fairly.

This application gives an incubator a single place to:

- Register startups and their founders
- Assign mentors to startups
- Define milestones and track completion
- Schedule meetings and record outcomes
- Evaluate startups on eight criteria with automatic scoring
- View a live dashboard and export reports

Two roles are supported: **Administrator** (full control) and **Incubation Manager** (day-to-day tracking).

---

## Problem Statement

An incubation program involves many moving parts — startups, founders, mentors, milestones, meetings, evaluations — all connected to each other. Without a system:

- Progress is tracked in scattered documents
- Mentor assignments get lost
- Deadlines slip without anyone noticing
- Evaluations are inconsistent across managers
- Reports take hours to compile manually

This project solves those problems by centralizing everything in a relational database with a clean desktop interface on top.

---

## Features

### Authentication
- Single login page for both roles
- Passwords stored as SHA-256 hashes (never plain text)
- Role-based routing: Admin → AdminMainFrame, Manager → ManagerMainFrame

### Startup Management
- Add, edit, delete, and view startups
- Fields: ID, name, founder, co-founder, email, phone, industry, stage, incubation start date, expected graduation date, status
- Search by name
- Filter by industry, stage, and status

### Founder Management
- Maintain founders separately, linked to their startup
- One startup can have multiple founders
- Fields: ID, name, startup, email, phone, qualification, skills, LinkedIn URL

### Mentor Management
- Maintain mentor profiles (name, expertise, email, phone, organization)
- Assign one or more mentors to a startup (many-to-many via startup_mentors)
- Unassign a mentor when no longer needed

### Milestone Tracking
- Per-startup milestones with title, description, start date, deadline, completion date, status, and completion percentage
- Four statuses: NOT_STARTED, IN_PROGRESS, COMPLETED, DELAYED
- Auto-fills completion date when a milestone is marked COMPLETED
- Detects delayed milestones (past deadline, not yet completed)

### Meeting Management
- Schedule meetings between startups, mentors, and managers
- Six meeting types: MENTORING, PROGRESS_REVIEW, TECHNICAL_REVIEW, BUSINESS_REVIEW, PITCH_PREPARATION, INVESTOR_MEETING
- Record discussion notes, next action, and next meeting date

### Startup Evaluation
- Score each startup on eight criteria (1–10 each):
  - Team
  - Problem Validation
  - Product Development
  - Market Validation
  - Business Model
  - Traction
  - Revenue
  - Pitch Readiness
- Overall score computed automatically as the average of the eight

### Dashboard
Live counters for:
- Total startups, active, graduated, on hold
- Idea-stage, MVP-stage, and early-revenue startups
- Total mentors, total meetings, upcoming meetings
- Delayed milestones
- Average evaluation score across all startups

### Reports
Three built-in reports, each exportable to CSV:
1. Startup Progress Report — startup details, current stage, mentors, completed/pending milestones, latest overall score
2. Mentor Report — mentor name, assigned startups, number of meetings conducted
3. Incubation Performance Report — totals by status and stage, average evaluation score

---

## Screenshots

> Add screenshots from the running app. Create a `docs/` folder in the repo, upload the images, then reference them here.

| Screen | File |
|---|---|
| Login | `docs/login.png` |
| Admin Dashboard | `docs/admin-dashboard.png` |
| Startups | `docs/startups.png` |
| Founders | `docs/founders.png` |
| Mentors | `docs/mentors.png` |
| Milestones | `docs/milestones.png` |
| Meetings | `docs/meetings.png` |
| Evaluations | `docs/evaluations.png` |
| Reports | `docs/reports.png` |

---

## Technologies

| Layer | Technology |
|---|---|
| Language | Java 21 |
| UI Framework | Java Swing |
| Look & Feel | FlatLaf 3.5.4 |
| Database | PostgreSQL (hosted on Supabase) |
| JDBC Driver | PostgreSQL JDBC 42.7.4 |
| Build Tool | Maven |
| Password Storage | SHA-256 hashing |
| Report Export | CSV (built-in FileWriter) |

---

## Architecture

The application is organized in four layers. Each layer only talks to the one below it — the UI never issues SQL, and the services never touch Swing.

```
UI Layer (Swing)
      ↓
Service Layer (business rules, validation)
      ↓
Repository Layer (SQL)
      ↓
DatabaseConnection (JDBC)
      ↓
PostgreSQL (Supabase, SSL required)
```

### Layer responsibilities

| Layer | What it does | What it does not do |
|---|---|---|
| Model | Holds data, no logic | No SQL, no Swing |
| Repository | All SQL for one entity | No validation, no business rules |
| Service | Validation + business rules + coordinates repositories | No SQL, no Swing |
| UI | Renders screens, captures input, calls services | No SQL, no business logic |

---

## Database Design

Ten tables. All foreign keys enforced. Indexes on frequently filtered columns.

### Tables

| Table | Purpose |
|---|---|
| users | Administrators and managers |
| programs | Cohorts (e.g., "Spring 2025") |
| startups | Startups in the program |
| founders | Founders linked to startups |
| mentors | Mentor profiles |
| startup_mentors | Many-to-many mentor ↔ startup assignment |
| milestones | Checkpoints per startup |
| meetings | Scheduled sessions |
| evaluations | Score snapshots per startup |
| progress_reports | Saved report summaries |

### Key relationships

- One startup → many founders
- One startup → many milestones
- One startup → many meetings
- One startup → many evaluations
- Many mentors ↔ many startups (via startup_mentors)
- One user (manager) → many meetings

---

## ER Diagram

> Create the ER diagram at dbdiagram.io or draw.io, export as PNG, and place it at `docs/er-diagram.png`.

Relationship summary (text form):

```
users ─┐
       ├──< meetings >── startups ──< founders
       │                     │
       │                     ├──< milestones
       │                     ├──< evaluations
       │                     └──< startup_mentors >── mentors
       │
       └──< programs ──< startups
```

---

## Setup Instructions

### Prerequisites

- Java 21 or later
- Maven (or IntelliJ's bundled Maven)
- A PostgreSQL database — this project uses Supabase free tier, but any PostgreSQL 12+ works

### 1. Clone the repository

```bash
git clone https://github.com/AbdulRasheedAbro/incubation-startup-tracking-system.git
cd incubation-startup-tracking-system
```

### 2. Create the database

If using Supabase:

1. Create a free project at https://supabase.com
2. Open Connect → Session pooler → JDBC
3. Copy the connection string (host, port, database, user)

If using a local PostgreSQL:

```bash
createdb incubation
```

### 3. Configure the connection

Copy the template and fill in your credentials:

```bash
cp config.properties.example config.properties
```

Then edit `config.properties`:

```properties
db.host=aws-0-<region>.pooler.supabase.com
db.port=5432
db.name=postgres
db.user=postgres.<your-project-ref>
db.password=<your-password>
```

Do not commit `config.properties`. It contains real credentials and is already listed in `.gitignore`.

### 4. Build and run

```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.time.incubation.Main"
```

Or open the project in IntelliJ and run `Main`.

### 5. First login

On first launch, the app creates all 10 tables and inserts a default admin:

| Username | Password |
|---|---|
| admin | admin123 |

Change this password after first login in any real deployment.

---

## SQL Setup

The application runs the schema automatically on first launch, but here is the equivalent SQL if you want to create it manually:

```sql
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    full_name VARCHAR(150),
    email VARCHAR(150),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS programs (
    program_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE
);

CREATE TABLE IF NOT EXISTS startups (
    startup_id VARCHAR(50) PRIMARY KEY,
    startup_name VARCHAR(200) NOT NULL,
    founder_name VARCHAR(150),
    co_founder_name VARCHAR(150),
    email VARCHAR(150) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    industry VARCHAR(100),
    stage VARCHAR(30) NOT NULL,
    incubation_start_date DATE NOT NULL,
    expected_graduation_date DATE,
    current_status VARCHAR(20) NOT NULL,
    program_id VARCHAR(50) REFERENCES programs(program_id)
);

CREATE TABLE IF NOT EXISTS founders (
    founder_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    startup_id VARCHAR(50) NOT NULL REFERENCES startups(startup_id) ON DELETE CASCADE,
    email VARCHAR(150),
    phone VARCHAR(20),
    qualification VARCHAR(200),
    skills TEXT,
    linkedin_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS mentors (
    mentor_id VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    expertise VARCHAR(200),
    email VARCHAR(150),
    phone VARCHAR(20),
    organization VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS startup_mentors (
    startup_id VARCHAR(50) NOT NULL REFERENCES startups(startup_id) ON DELETE CASCADE,
    mentor_id VARCHAR(50) NOT NULL REFERENCES mentors(mentor_id) ON DELETE CASCADE,
    PRIMARY KEY (startup_id, mentor_id)
);

CREATE TABLE IF NOT EXISTS milestones (
    milestone_id VARCHAR(50) PRIMARY KEY,
    startup_id VARCHAR(50) NOT NULL REFERENCES startups(startup_id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE,
    deadline DATE,
    completion_date DATE,
    status VARCHAR(30) NOT NULL,
    completion_percentage INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS meetings (
    meeting_id VARCHAR(50) PRIMARY KEY,
    startup_id VARCHAR(50) NOT NULL REFERENCES startups(startup_id) ON DELETE CASCADE,
    mentor_id VARCHAR(50) REFERENCES mentors(mentor_id),
    manager_id VARCHAR(50) REFERENCES users(user_id),
    meeting_date DATE NOT NULL,
    meeting_time VARCHAR(10),
    meeting_type VARCHAR(50) NOT NULL,
    discussion_notes TEXT,
    next_action TEXT,
    next_meeting_date DATE
);

CREATE TABLE IF NOT EXISTS evaluations (
    evaluation_id VARCHAR(50) PRIMARY KEY,
    startup_id VARCHAR(50) NOT NULL REFERENCES startups(startup_id) ON DELETE CASCADE,
    team_score INT NOT NULL,
    problem_validation_score INT NOT NULL,
    product_development_score INT NOT NULL,
    market_validation_score INT NOT NULL,
    business_model_score INT NOT NULL,
    traction_score INT NOT NULL,
    revenue_score INT NOT NULL,
    pitch_readiness_score INT NOT NULL,
    overall_score DOUBLE PRECISION NOT NULL,
    evaluated_date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS progress_reports (
    report_id VARCHAR(50) PRIMARY KEY,
    startup_id VARCHAR(50) NOT NULL REFERENCES startups(startup_id) ON DELETE CASCADE,
    report_date DATE NOT NULL,
    summary TEXT,
    current_stage VARCHAR(30),
    overall_score DOUBLE PRECISION,
    completed_milestones INT,
    pending_milestones INT
);
```

---

## Usage

### Log in

Open the app, enter your username and password. The role determines which dashboard opens:

- Admin → full tab set (Dashboard, Startups, Founders, Mentors, Milestones, Meetings, Evaluations, Reports)
- Manager → focused tab set (Dashboard, Startups, Milestones, Meetings, Evaluations, Reports)

### Admin workflow

1. Startups tab → add a startup (e.g. ST001 / Tihami Tech / stage MVP / status ACTIVE)
2. Founders tab → add one or more founders linked to the startup
3. Mentors tab → add a mentor, then select them in the table and click Assign to link to a startup
4. Milestones tab → add milestones with deadlines
5. Meetings tab → schedule sessions between the startup and a mentor
6. Evaluations tab → score the startup on eight sliders, click Save Evaluation
7. Reports tab → generate a report and export it as CSV

### Example data — a software startup

| Entity | Example |
|---|---|
| Startup | ST001 / Tihami Tech / Software Development / MVP / ACTIVE |
| Founders | FN001 Tihami Khan (BS CS), FN002 Sara Ahmed (MBA) |
| Mentors | ME001 Dr. Bilal Rashid (Software Architecture), ME002 Ayesha Malik (Product Strategy) |
| Milestones | Problem Validation, Market Research, MVP Development, First Customer, Revenue Generation |
| Meetings | Mentoring, Business Review, Technical Review, Progress Review |
| Evaluation | Team 8, Problem 9, Product 7, Market 6, Business 8, Traction 5, Revenue 4, Pitch 7 → 6.75 / 10 |

---

## Project Structure

```
src/main/java/com/time/incubation/
├── Main.java
│
├── model/
│   ├── User.java
│   ├── UserRole.java
│   ├── Startup.java
│   ├── StartupStage.java
│   ├── StartupStatus.java
│   ├── Founder.java
│   ├── Mentor.java
│   ├── Milestone.java
│   ├── MilestoneStatus.java
│   ├── Meeting.java
│   ├── MeetingType.java
│   ├── Evaluation.java
│   └── ProgressReport.java
│
├── exception/
│   ├── DuplicateIdException.java
│   ├── DuplicateEmailException.java
│   ├── InvalidInputException.java
│   ├── RecordNotFoundException.java
│   └── DataBaseException.java
│
├── util/
│   ├── DatabaseConnection.java
│   ├── PasswordUtils.java
│   └── ValidationUtils.java
│
├── repository/
│   ├── UserRepository.java
│   ├── StartupRepository.java
│   ├── FounderRepository.java
│   ├── MentorRepository.java
│   ├── MilestoneRepository.java
│   ├── MeetingRepository.java
│   ├── EvaluationRepository.java
│   └── ProgressReportRepository.java
│
├── service/
│   ├── AuthService.java
│   ├── StartupService.java
│   ├── FounderService.java
│   ├── MentorService.java
│   ├── MilestoneService.java
│   ├── MeetingService.java
│   ├── EvaluationService.java
│   ├── DashboardService.java
│   └── ReportService.java
│
└── ui/
    ├── UiTheme.java
    ├── LoginFrame.java
    ├── AdminMainFrame.java
    ├── ManagerMainFrame.java
    ├── DashboardPanel.java
    ├── StartupPanel.java
    ├── FounderPanel.java
    ├── MentorPanel.java
    ├── MilestonePanel.java
    ├── MeetingPanel.java
    ├── EvaluationPanel.java
    └── ReportPanel.java
```

---

## Validation & Error Handling

Every user input is validated before reaching the database. The service layer throws typed exceptions that the UI catches and displays:

| Exception | When it's thrown |
|---|---|
| DuplicateIdException | Inserting a record whose primary key already exists |
| DuplicateEmailException | Email uniqueness violation |
| InvalidInputException | Empty required fields, bad dates, scores outside 1–10, graduation before start date, invalid email/phone |
| RecordNotFoundException | Lookup by ID returns nothing |
| DataBaseException | Wraps any underlying SQLException |

All database access uses PreparedStatement — no string concatenation, so SQL injection is prevented.

---

## Future Improvements

- BCrypt password hashing — SHA-256 is fast and unsalted; BCrypt/Argon2 is the production choice
- Role-scoped startup visibility — assign each startup to a specific manager so managers see only their own
- Email notifications — when tasks are assigned or deadlines approach
- PDF report export — currently CSV; PDFBox is already on the classpath, ready to add
- Charts on the dashboard — evaluation scores over time per startup
- Soft delete — mark records as inactive rather than hard-deleting
- Multi-program support — the programs table exists but isn't yet wired into the UI

---

## Acknowledgements

Built as part of a Java internship at TIME (The Institute of Management and Entrepreneurship).

Special thanks to my mentor and supervisor, Abdur Rafay, for the guidance on architecture, database design, and the mindset shift from programmer to engineer.

---

## License

This project is provided for educational purposes.
