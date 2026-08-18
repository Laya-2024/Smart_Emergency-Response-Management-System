let myLat = null, myLng = null;

// ── Load user info ────────────────────────────────────────────────────────────
fetch('/api/v1/account/me')
  .then(r => r.ok ? r.json() : Promise.reject())
  .then(a => {
    const ui = document.querySelector('#user-info');
    if (ui) ui.textContent = `${a.fullName} · ${a.roles.join(', ')}`;
  })
  .catch(() => location.href = '/login.html');

// ── Logout ────────────────────────────────────────────────────────────────────
document.querySelector('#logout-link')?.addEventListener('click', async e => {
  e.preventDefault();
  await fetch('/logout', { method: 'POST' }).catch(() => {});
  location.href = '/login.html?logout=true';
});

// ── Get location ──────────────────────────────────────────────────────────────
const locStatus = document.querySelector('#location-status');
if (navigator.geolocation) {
  navigator.geolocation.getCurrentPosition(
    ({ coords }) => {
      myLat = coords.latitude; myLng = coords.longitude;
      if (locStatus) locStatus.textContent = `📍 Location: ${myLat.toFixed(4)}, ${myLng.toFixed(4)}`;
    },
    () => { if (locStatus) locStatus.textContent = '⚠️ Location unavailable — enable GPS for alerts.'; }
  );
} else {
  if (locStatus) locStatus.textContent = '⚠️ Geolocation not supported.';
}

// ── Update responder status ───────────────────────────────────────────────────
document.querySelector('#save-status')?.addEventListener('click', async () => {
  const statusMsg = document.querySelector('#status-msg');
  const status = document.querySelector('#availability').value;
  try {
    await fetch('/api/v1/responders/me/status', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ availabilityStatus: status, latitude: myLat || 0, longitude: myLng || 0 })
    });
    if (statusMsg) { statusMsg.style.color = '#1a7f4b'; statusMsg.textContent = `✅ Status updated to ${status}`; }
  } catch {
    if (statusMsg) { statusMsg.style.color = '#df2336'; statusMsg.textContent = 'Failed to update status.'; }
  }
});

// ── Live alert stream (SSE) ───────────────────────────────────────────────────
const alertsList = document.querySelector('#alerts-list');
const alertsEmpty = document.querySelector('#alerts-empty');

function addAlertCard(alert) {
  if (alertsEmpty) alertsEmpty.style.display = 'none';
  const card = document.createElement('div');
  card.className = 'alert-item';
  card.id = `alert-${alert.emergencyId}`;
  card.innerHTML = `
    <div class="alert-type">🚨 ${alert.type}</div>
    <div class="alert-loc">📍 Lat: ${alert.latitude?.toFixed(4) ?? '?'}, Lng: ${alert.longitude?.toFixed(4) ?? '?'}</div>
    <div style="font-size:.82rem;color:#536070;margin-top:4px">${alert.description || ''}</div>
    <button class="btn-accept" data-id="${alert.emergencyId}">Accept &amp; respond</button>
  `;
  alertsList?.prepend(card);
  if (Number.isFinite(Number(alert.latitude)) && Number.isFinite(Number(alert.longitude))) {
    const link = document.createElement('a');
    link.href = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${alert.latitude},${alert.longitude}`)}`;
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
    link.textContent = 'Open SOS location in Google Maps';
    card.querySelector('.alert-loc')?.append(document.createElement('br'), link);
  }

  card.querySelector('.btn-accept')?.addEventListener('click', async function () {
    this.disabled = true; this.textContent = 'Accepting…';
    try {
      await fetch(`/api/v1/emergencies/${alert.emergencyId}/accept`, { method: 'POST' });
      this.textContent = '✅ Accepted';
      this.style.background = '#1a7f4b';
      // Move to incidents
      const incList = document.querySelector('#incidents-list');
      if (incList) {
        incList.querySelector('p')?.remove();
        incList.insertAdjacentHTML('beforeend', `
          <div class="alert-item" style="border-color:#1a7f4b">
            <div class="alert-type" style="color:#1a7f4b">✅ ${alert.type} — accepted</div>
            <div class="alert-loc">📍 Lat: ${alert.latitude?.toFixed(4) ?? '?'}, Lng: ${alert.longitude?.toFixed(4) ?? '?'}</div>
          </div>`);
      }
    } catch {
      this.disabled = false; this.textContent = 'Accept & respond';
    }
  });
}

try {
  const stream = new EventSource('/api/v1/alerts/stream');
  stream.addEventListener('emergency-alert', e => {
    try { addAlertCard(JSON.parse(e.data)); } catch {}
  });
  stream.onerror = () => {};
} catch {}
