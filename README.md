# Attendance Tracking System

A full-stack web app built for **UC Leuven-Limburg as a real client**, letting lecturers track and navigate student attendance across large classes — with sorting, search, and visual reporting.

Built by a team of student developers over a semester, following **Scrum / Agile** with weekly sprints, daily stand-ups, and live demos to the client.

## What it does

- Lets lecturers browse and manage attendance records across multiple courses and groups
- Live search and multi-column sorting so lecturers can find what they need instantly
- Filter records by course, group, date range, or attendance status
- Visual reporting with charts that turn raw attendance data into readable trends at a glance
- Responsive interface designed around how lecturers actually work day to day

## My role — Junior Developer (front-end focus)

I worked as a junior developer focused on the **React / Next.js front-end**. Features I built or contributed to:

- **Attendance sorting** — multi-column sort so lecturers can order by name, date, status, or course
- **Search and filtering** — live search bar and filter controls connected to the backend API
- **Data visualisation** — chart components (attendance rate over time, per-group breakdowns) using a charting library
- **Responsive layout** — ensuring the interface works cleanly on both desktop and tablet
- **API integration** — connecting front-end components to the Java REST backend with proper loading and error states

I also participated fully in the Scrum process — sprint planning, daily stand-ups, and client demos at the end of each sprint.

## Tech stack

| Layer | Stack |
|-------|-------|
| Frontend | React, Next.js, TypeScript |
| Backend | Java, Spring Boot |
| Database | PostgreSQL + Spring Data JPA |
| Testing | Cypress (E2E) |
| Process | Agile / Scrum, Git, GitHub Actions |

## Repo structure

```
.
├── frontend/   → React / Next.js client (my main contribution)
│   ├── app/        → Next.js app router pages
│   ├── components/ → shared UI components
│   └── cypress/    → end-to-end tests
└── backend/    → Java / Spring Boot REST API
    └── src/        → domain model, repositories, controllers
```

---

> Team project for the Junior Workplace Project module at UC Leuven-Limburg, delivered to UCLL as a real client. Any actual student data has been removed from the repository.

