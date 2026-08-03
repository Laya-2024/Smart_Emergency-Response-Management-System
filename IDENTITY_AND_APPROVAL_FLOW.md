# Identity verification and role approval flow

## Registration

1. The user submits full name, email, mobile number, password, and a selected role.
2. The server creates an account with both verification flags set to `false`.
3. It sends an email verification link and a six-digit mobile OTP through the configured SMS gateway.
4. The user cannot log in until both checks pass.

## Role rules

| Role | Verification and approval |
|---|---|
| Citizen | Email + mobile verification. |
| Donor | Email + mobile verification; optional donor profile review. |
| Volunteer | Email + mobile verification plus skills, availability, photo, ID document, and admin approval. |
| Doctor | Email + mobile verification plus hospital, medical-registration number, photo, ID, and admin approval. |
| Police | Email + mobile verification plus station, badge number, photo, ID, and admin approval. |
| Dispatcher / Admin | Created or approved only by an authorised administrator. |

## Security requirements

- Do not store Aadhaar numbers or files directly in MySQL. Mask an ID for review and store encrypted files in private object storage; only save the storage key and SHA-256 checksum in `verification_documents`.
- Store phone numbers encrypted; keep a one-way phone hash only for uniqueness/searching.
- Store OTPs and email tokens only as hashes, limit attempts, expire them quickly, and log verification/review events in `audit_logs`.
- Use an SMS-provider adapter with secrets in environment variables. Never commit API keys.
