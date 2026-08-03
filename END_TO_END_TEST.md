# End-to-end two-device test

## One-time setup

1. Import this folder as an Eclipse Maven project on the server laptop.
2. Create the MySQL schema using `database_schema.sql`, then run Eclipse with `FLYWAY_ENABLED=false`.
3. Start the server on the LAN, for example `http://192.168.1.10:8080`. Set `APP_PUBLIC_URL` to that address.
4. On both devices, open the same server address. Do not run separate local servers.

## Accounts and approval

1. Register a Citizen account and a second account for the responder. In demo mode the verification codes are shown in the browser alert.
2. Verify email and mobile for both accounts.
3. Sign in as an administrator and approve the responder's role application in `/admin.html`.
4. Sign in as the approved responder on the second device, open `/dashboard.html`, choose `AVAILABLE`, and grant location access.

## Alert test

1. Sign in as the citizen on the first device and press **Send SOS**.
2. A WOMEN_SAFETY or SOS alert is delivered only to approved nearby police/dispatcher users.
3. An ACCIDENT alert can be delivered to approved nearby doctor/police/volunteer/dispatcher users.
4. Keep the responder dashboard open. It receives the live browser alert through the shared Spring server.
5. If no delivery is acknowledged after one minute, the server expands routing to 15 km.

## Important limits for this version

- Live alerts require the responder browser dashboard to be open. Background phone notifications require a real web-push/mobile application provider.
- Demo OTP display is for local college testing only. Set `APP_DEMO_MODE=false` and configure SMTP before deployment.
- Test with a non-production database and test documents only.
