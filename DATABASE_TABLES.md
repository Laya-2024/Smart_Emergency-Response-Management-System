# Database tables

All migrations run automatically from `src/main/resources/db/migration` when the Spring application starts. Database name: `emergency_response`.

| Table | Purpose |
|---|---|
| `app_users` | Accounts, email, BCrypt password hash, enabled state. |
| `user_roles` | Many-to-many role assignments: citizen, volunteer, donor, police, doctor, dispatcher, admin. |
| `password_reset_tokens` | Hashed, one-time, 20-minute password-reset tokens. |
| `emergencies` | Core SOS/emergency incident, type, status, reporter, coordinates, description. |
| `responder_profiles` | Verified responder organisation, service and availability. |
| `emergency_assignments` | Which responder was offered/accepted an incident. |
| `emergency_updates` | Timeline messages and status updates for an incident. |
| `trusted_contacts` | User-selected safety contacts; phone numbers are encrypted. |
| `shelters` | Shelter location, capacity and availability. |
| `disaster_events` | Major flood, tsunami and disaster coordination events. |
| `relief_requests` | Food, water, medicine, rescue and shelter assistance requests. |
| `donations` | Money/item donation pledges and payment reference. |
| `notification_outbox` | Reliable queued SMS/email/push notification records. |
| `audit_logs` | Security and operational audit trail. |

Important security rules: passwords are never stored directly; reset tokens are stored only as SHA-256 hashes; phone numbers are intended for application-layer encryption; audit logs must not contain sensitive location/medical data.
