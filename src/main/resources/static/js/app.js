// ── Role-aware header ─────────────────────────────────────────────────────────
fetch('/api/v1/account/me')
  .then(r => r.ok ? r.json() : null)
  .then(account => {
    if (!account) return;
    const navLogin = document.querySelector('#nav-login');
    const navRegister = document.querySelector('#nav-register');
    const userInfo = document.querySelector('#user-info');
    if (navLogin) navLogin.style.display = 'none';
    if (navRegister) { navRegister.textContent = 'My portal'; navRegister.href = '/portal.html'; }
    if (userInfo) userInfo.textContent = account.fullName;
  })
  .catch(() => {});

// ── SOS button ────────────────────────────────────────────────────────────────
const sosBtn = document.querySelector('#sos');
const result = document.querySelector('#result');

sosBtn?.addEventListener('click', () => {
  if (!navigator.geolocation) {
    result.textContent = 'Location unavailable. Please call 112 immediately.';
    return;
  }
  sosBtn.disabled = true;
  result.textContent = '📍 Getting your location…';

  navigator.geolocation.getCurrentPosition(
    async ({ coords }) => {
      try {
        const deviceId = localStorage.deviceId || (localStorage.deviceId = crypto.randomUUID());
        const res = await fetch('/api/v1/emergencies', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Idempotency-Key': crypto.randomUUID(),
            'X-Device-Id': deviceId
          },
          body: JSON.stringify({
            type: 'SOS',
            latitude: coords.latitude,
            longitude: coords.longitude,
            description: 'SOS submitted from SafeLink web app'
          })
        });
        if (res.ok) {
          result.textContent = '✅ SOS received. Stay safe — responders are being notified.';
          result.style.color = '#1a7f4b';
        } else {
          result.textContent = '⚠️ Could not submit SOS. Call 112 now.';
          result.style.color = '#df2336';
        }
      } catch {
        result.textContent = '⚠️ Network unavailable. Call 112 immediately.';
        result.style.color = '#df2336';
      } finally {
        setTimeout(() => { sosBtn.disabled = false; }, 10000);
      }
    },
    () => {
      result.textContent = '⚠️ Location permission denied. Call 112 immediately.';
      result.style.color = '#df2336';
      sosBtn.disabled = false;
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  );
});
