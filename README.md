# Smart Emergency Response Management System

A secure, modular emergency-coordination platform for SOS, women's safety, fire, accidents, medical emergencies, shelter support, and disaster relief.

## Included foundation

- One-tap, geolocated SOS UI with clear fallback instructions if GPS or network fails.
- Spring MVC REST API with Hibernate/JPA, MySQL, Flyway migrations and Maven layout.
- Idempotency keys so failed mobile requests can safely retry without creating duplicate incidents.
- Strict coordinate and payload validation, optimistic locking, indexed status queues and paginated dispatch views.
- Least-privilege roles: citizen, volunteer, police, doctor, donor, dispatcher and administrator. Dispatcher actions require authentication.
- BCrypt password hashes for demo accounts, CSP security headers, error responses that do not disclose internals, and per-IP SOS burst protection.
- Actuator health/readiness probes, batch-friendly Hibernate settings, and an explicitly disabled Open Session in View anti-pattern.

## Run locally

1. Configure Eclipse to use **JDK 17 or newer** (Spring Boot 3 does not run on Java 8).
2. In `mysql_user_setup.sql`, replace the password placeholder with a strong local password and execute it as a MySQL administrator. Set `DB_USERNAME` and `DB_PASSWORD` before starting the application. Set a long random `APP_DATA_ENCRYPTION_KEY` before any deployment; the built-in key is for local Eclipse development only. Do not commit secrets.
3. Run `mvn spring-boot:run` or right-click `EmergencyResponseApplication.java` in Eclipse → **Run As → Java Application**.
4. Open `http://localhost:8080`. The schema is created by Flyway automatically.

The local default JDBC URL includes `allowPublicKeyRetrieval=true` for MySQL 8+/9 local authentication. For a deployed system use TLS and remove that local-only option.

Before the first run, execute `mysql_user_setup.sql` in MySQL Workbench as an administrator. It creates the local `emergency_app` account; `DB_USERNAME` defaults to that account but its password must be supplied through `DB_PASSWORD`.

If you create the database manually with `database_schema.sql`, Flyway now automatically baselines the schema at version 5 and applies only future migrations. Keep `FLYWAY_ENABLED=true`.

If an older manual schema is missing real-time tables, Flyway applies the included version-6 repair migration automatically on the next start.

Register a citizen, volunteer, or donor account through `/register.html`. Police, doctor, dispatcher, and administrator accounts must be provisioned only after organisation verification. Configure `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `APP_PUBLIC_URL` for account-verification and password-reset emails.

Local demo mode is enabled by default and creates `admin@safelink.local` with password `AdminDemo!123`; it also displays verification codes when SMTP is unavailable. Set `APP_DEMO_MODE=false` before deployment.

## Production completion checklist

- Put the service behind TLS, a WAF/API gateway, and a load balancer across at least two availability zones.
- Replace the in-memory rate limiter with a Redis-backed distributed limiter, and publish notifications through an outbox + durable queue (SMS, push, email) so alerts survive downstream outages.
- Add verified registration, MFA for responder/admin roles, password reset, audit trails, consent/retention controls, encryption at rest, and never log exact locations or medical details.
- Integrate geospatial responder matching, verified responder availability, escalation timers, offline mobile queueing, trusted contacts, safe-route check-ins, multilingual accessibility, and live case timelines.
- Use managed MySQL with automated backups/PITR, migration-only schema changes, monitoring/alerts, load tests, security scans, and disaster-recovery exercises.

## API

`POST /api/v1/emergencies` accepts `{ "type":"SOS", "latitude":12.97, "longitude":77.59, "description":"..." }` and requires an `Idempotency-Key` header. `GET /api/v1/emergencies` requires a responder account. `PATCH /api/v1/emergencies/{id}/acknowledge` requires DISPATCHER or ADMIN.

See `END_TO_END_TEST.md` for the shared-domain/two-device flow.
