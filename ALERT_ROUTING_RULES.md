# Emergency alert-routing rules

The server, never the browser, decides who receives an alert. A user receives an emergency only when their account, documents, role, availability and location satisfy the policy.

| Emergency | Recipients | Never send to |
|---|---|---|
| Women safety / SOS | Verified police/dispatchers near the incident; optionally the reporter's trusted contacts | Public users, unverified volunteers, donors |
| Accident | Verified nearby doctors, police, trained volunteers and dispatchers | Unverified or unavailable users |
| Medical | Verified doctors, ambulance/dispatch and police if escalation is needed | Public users unless the reporter explicitly asks trusted contacts |
| Fire | Fire/dispatch, police and verified disaster volunteers | General public |
| Flood / tsunami / disaster | Dispatch, verified disaster volunteers, shelters and authorised relief teams | Unverified accounts |
| Shelter / relief | Verified relief volunteers, shelter managers and dispatch | General public |

## Safe proximity rules

- Match only responders with approved documents, an active account, `AVAILABLE` status, a recent location and an allowed service type.
- Start with a small radius (for example 5 km); expand gradually only if no responder accepts within the escalation timer.
- Send only the minimum details needed to respond. Do not expose a women's-safety reporter's identity/location to ordinary users.
- A responder must explicitly accept an alert. Dispatch keeps an audit trail and can revoke abusive accounts.

## Multi-laptop behaviour

All laptops/mobile devices must connect to the same deployed Spring server and MySQL database. The server then creates `alert_deliveries`, publishes a real-time push/WebSocket event, and falls back to queued email/SMS/push. Running separate local servers on separate laptops will not share alerts.
