const message = document.querySelector('#workspace-message');
const unreadAlertsKey = 'safelinkUnreadAlerts';
const titles = { overview: 'Welcome to Smart Emergency Response', sos: 'Emergency SOS', women: 'Women safety', fire: 'Fire & accident', shelter: 'Shelters & relief', blood: 'Blood request', donation: 'Donation', alerts: 'Responder alerts', myrequests: 'My requests', feedback: 'Feedback' };
const showMessage = (text, error = false) => { message.textContent = text; message.className = `workspace-message ${error ? 'error' : 'success'}`; };

function updateAlertBadge(count = Number(localStorage.getItem(unreadAlertsKey) || 0)) {
  const badge = document.querySelector('#alerts-badge');
  if (!badge) return;
  badge.hidden = count < 1;
  badge.textContent = count > 99 ? '99+' : String(count);
}
function markAlertsRead() { localStorage.removeItem(unreadAlertsKey); updateAlertBadge(0); }
function addUnreadAlert() { const count = Number(localStorage.getItem(unreadAlertsKey) || 0) + 1; localStorage.setItem(unreadAlertsKey, String(count)); updateAlertBadge(count); }
updateAlertBadge();

function openSection(id, updateUrl = true) {
  if (!titles[id]) return;
  document.querySelectorAll('.workspace-section').forEach(s => s.classList.toggle('active', s.id === id));
  document.querySelectorAll('.nav-item[data-section]').forEach(i => i.classList.toggle('active', i.dataset.section === id));
  document.querySelector('#page-title').textContent = titles[id];
  message.className = 'workspace-message';
  if (updateUrl) history.pushState({ section: id }, '', `${window.location.pathname}#${id}`);
  if (id === 'shelter') loadShelters();
  if (id === 'alerts') { markAlertsRead(); loadAlerts(); }
  if (id === 'myrequests') loadMyRequests();
  if (id === 'feedback') { loadFeedbackEmergencies(); loadFeedbackHistory(); }
}

document.querySelectorAll('[data-section], [data-open]').forEach(btn => btn.addEventListener('click', e => {
  e.preventDefault();
  openSection(btn.dataset.section || btn.dataset.open);
}));

function openSectionFromUrl() {
  const id = window.location.hash.slice(1) || 'overview';
  openSection(titles[id] ? id : 'overview', false);
}
window.addEventListener('popstate', openSectionFromUrl);
window.addEventListener('hashchange', openSectionFromUrl);
openSectionFromUrl();

async function getCurrentLocation() {
  if (!navigator.geolocation) throw new Error('Location is not available in this browser. Call 112 for urgent help.');
  return new Promise((resolve, reject) => navigator.geolocation.getCurrentPosition(
    p => resolve(p.coords), () => reject(new Error('Location permission is needed to route this request.')), { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }));
}
async function responseError(r) { const b = await r.json().catch(() => ({})); if (b.detail) return b.detail; if (r.status === 403) return 'Only an approved responder can accept an alert. Only a dispatcher or administrator can resolve it.'; if (r.status === 409) return 'This emergency has already been accepted, resolved, or cancelled.'; return 'The request could not be completed.'; }

function emergencyLocation(alert) {
  const latitude = Number(alert.latitude);
  const longitude = Number(alert.longitude);
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) return '<small>Location unavailable</small>';
  const coordinates = `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
  const mapUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${latitude},${longitude}`)}`;
  return `<small>Location: ${coordinates} · <a href="${mapUrl}" target="_blank" rel="noopener noreferrer">Open directions</a></small>`;
}

function showResponderLocation(update) {
  const latitude = Number(update.responderLatitude);
  const longitude = Number(update.responderLongitude);
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) return;
  const panel = document.querySelector('#responder-location-update');
  if (!panel) return;
  panel.hidden = false;
  panel.textContent = `${update.responderName || 'Your responder'} location: ${latitude.toFixed(6)}, ${longitude.toFixed(6)}. `;
  const link = document.createElement('a');
  link.href = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${latitude},${longitude}`)}`;
  link.target = '_blank';
  link.rel = 'noopener noreferrer';
  link.textContent = 'Open responder location in Google Maps';
  panel.append(link);
}

document.querySelectorAll('.emergency-form').forEach(form => form.addEventListener('submit', async e => {
  e.preventDefault();
  try {
    const coords = await getCurrentLocation();
    const type = form.dataset.type || new FormData(form).get('type');
    const res = await fetch('/api/v1/emergencies', { method: 'POST', headers: { 'Content-Type': 'application/json', 'Idempotency-Key': crypto.randomUUID(), 'X-Device-Id': localStorage.deviceId || (localStorage.deviceId = crypto.randomUUID()) }, body: JSON.stringify({ type, latitude: coords.latitude, longitude: coords.longitude, description: new FormData(form).get('description') }) });
    if (!res.ok) throw new Error(await responseError(res));
    const created = await res.json();
    showMessage(`Alert sent. Emergency ID: ${created.id}. Call 112 if immediate help is needed.`);
    form.reset();
  } catch (err) { showMessage(err.message, true); }
}));

document.querySelectorAll('.relief-form').forEach(form => form.addEventListener('submit', async e => {
  e.preventDefault();
  try {
    const coords = await getCurrentLocation();
    const data = new FormData(form);
    const res = await fetch('/api/v1/relief-requests', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ requestType: data.get('requestType'), peopleCount: Number(data.get('peopleCount')), description: data.get('description'), latitude: coords.latitude, longitude: coords.longitude }) });
    if (!res.ok) throw new Error(await responseError(res));
    showMessage('Your request has been recorded for relief coordinators.'); form.reset();
  } catch (err) { showMessage(err.message, true); }
}));

document.querySelector('.blood-form').addEventListener('submit', async e => {
  e.preventDefault();
  try {
    const coords = await getCurrentLocation();
    const data = new FormData(e.target);
    const res = await fetch('/api/v1/blood-requests', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ bloodGroup: data.get('bloodGroup'), unitsRequired: Number(data.get('unitsRequired')), hospitalName: data.get('hospitalName'), contactNote: data.get('contactNote'), latitude: coords.latitude, longitude: coords.longitude }) });
    if (!res.ok) throw new Error(await responseError(res));
    showMessage('Your blood request has been recorded.'); e.target.reset();
  } catch (err) { showMessage(err.message, true); }
});

const donationForm = document.querySelector('.donation-form');
if (donationForm && !donationForm.querySelector('[name="targetName"]')) {
  const targetType = document.createElement('label');
  targetType.innerHTML = 'Donation for<select name="targetType" required><option value="ORGANIZATION">Verified organization</option><option value="PATIENT">Specific patient</option></select>';
  const targetName = document.createElement('label');
  targetName.innerHTML = 'Organization or patient name<input name="targetName" maxlength="160" required placeholder="Exact registered organization or patient name">';
  donationForm.querySelector('button').before(targetType, targetName);
}
donationForm.addEventListener('submit', async e => {
  e.preventDefault();
  try {
    const data = new FormData(e.target);
    const res = await fetch('/api/v1/donations', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ donationType: data.get('donationType'), amount: data.get('amount') || null, currency: data.get('currency').toUpperCase(), itemDescription: data.get('itemDescription'), targetName: data.get('targetName'), targetType: data.get('targetType') }) });
    if (!res.ok) throw new Error(await responseError(res));
    showMessage('Thank you — your donation pledge has been recorded.'); e.target.reset();
  } catch (err) { showMessage(err.message, true); }
});

document.querySelector('.feedback-form').addEventListener('submit', async e => {
  e.preventDefault();
  try {
    const data = new FormData(e.target);
    const res = await fetch(`/api/v1/emergencies/${data.get('emergencyId')}/feedback`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ rating: Number(data.get('rating')), comments: data.get('comments') }) });
    if (!res.ok) throw new Error(await responseError(res));
    showMessage('Thank you for your feedback.'); e.target.reset(); loadFeedbackEmergencies(); loadFeedbackHistory();
  } catch (err) { showMessage(err.message, true); }
});

// Profile dropdown
function toggleProfile() {
  const drop = document.querySelector('#profileDrop');
  drop.style.display = drop.style.display === 'block' ? 'none' : 'block';
}
document.addEventListener('click', e => {
  if (!e.target.closest('#profileBtn') && !e.target.closest('#profileDrop'))
    document.querySelector('#profileDrop').style.display = 'none';
});

async function loadShelters() {
  const list = document.querySelector('#shelter-list');
  try {
    const res = await fetch('/api/v1/shelters');
    if (!res.ok) throw new Error();
    const shelters = await res.json();
    list.innerHTML = '';
    if (!shelters.length) { list.textContent = 'No open shelters have been added yet.'; return; }
    shelters.forEach(s => { const c = document.createElement('article'); c.className = 'data-card'; c.innerHTML = `<b>${s.name}</b><span>${s.addressLine}</span><small>${s.capacityAvailable} spaces available</small>`; list.append(c); });
  } catch { list.textContent = 'Unable to load shelters.'; }
}

async function loadAlerts() {
  const list = document.querySelector('#alert-list');
  try {
    const res = await fetch('/api/v1/alerts');
    if (!res.ok) throw new Error();
    const alerts = await res.json();
    list.innerHTML = '';
    if (!alerts.length) { list.textContent = 'No routed alerts yet. Complete professional verification, then set your availability to Available.'; return; }
    alerts.forEach(a => {
      const c = document.createElement('article');
      c.className = 'data-card alert-card';
      const unavailable = a.acceptedByCurrentUser || a.status === 'RESOLVED' || a.status === 'ACKNOWLEDGED' || a.status === 'IN_PROGRESS';
      const state = a.status === 'RESOLVED' ? 'Resolved' : a.acceptedByCurrentUser ? 'You accepted this emergency' : unavailable ? 'Already accepted by another responder' : '';
      const location = emergencyLocation(a);
      c.innerHTML = `<b>${a.type.replaceAll('_', ' ')}</b><span>${a.description || 'A nearby emergency needs response.'}</span><small>${new Date(a.createdAt).toLocaleString()}${state ? ` · ${state}` : ''}</small>${unavailable ? '' : '<button class="secondary-action alert-accept" type="button">Accept & respond</button>'}`;
      c.querySelector('span')?.insertAdjacentHTML('afterend', location);
      if (a.acceptedByCurrentUser && a.status !== 'RESOLVED') addResolveButton(c, a);
      c.querySelector('.alert-accept')?.addEventListener('click', async event => {
        const button = event.currentTarget;
        button.disabled = true;
        try {
          const response = await fetch(`/api/v1/emergencies/${a.emergencyId}/accept`, { method: 'POST' });
          if (!response.ok) throw new Error(await responseError(response));
          button.textContent = 'Accepted';
          addResolveButton(c, a);
          showMessage('Response accepted. Please update the incident timeline while assisting.');
        } catch (error) {
          button.disabled = false;
          showMessage(error.message, true);
        }
      });
      list.append(c);
    });
  } catch { list.textContent = 'Unable to load alerts.'; }
}

function addResolveButton(card, alert) {
  if (card.querySelector('.resolve-emergency')) return;
  const resolve = document.createElement('button');
  resolve.className = 'primary-action resolve-emergency';
  resolve.type = 'button';
  resolve.textContent = 'Resolve emergency';
  resolve.addEventListener('click', async () => {
    resolve.disabled = true;
    try {
      const result = await fetch(`/api/v1/emergencies/${alert.emergencyId}/resolve`, { method: 'PATCH' });
      if (!result.ok) throw new Error(await responseError(result));
      resolve.textContent = 'Resolved';
      showMessage('Emergency marked as resolved. The requester can now leave feedback.');
    } catch (error) {
      resolve.disabled = false;
      showMessage(error.message, true);
    }
  });
  card.append(resolve);
}

async function loadMyRequests() {
  const fmt = d => d ? new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';
  const card = html => `<article class="data-card">${html}</article>`;

  const emList = document.querySelector('#my-em-list');
  fetch('/api/v1/emergencies/mine').then(r => r.ok ? r.json() : Promise.reject()).then(data => {
    const list = Array.isArray(data) ? data : (data.content || []);
    emList.innerHTML = list.length ? list.map(e => card(`<b>${(e.type||'').replaceAll('_', ' ')}</b><small>Status: ${e.status || 'OPEN'} · ${fmt(e.createdAt)}</small>${e.status === 'OPEN' ? `<button class="secondary-action cancel-emergency" type="button" data-id="${e.id}">Cancel alert</button>` : ''}`)).join('') : '<p style="color:#94a3b8">No emergencies submitted yet.</p>';
    emList.querySelectorAll('.cancel-emergency').forEach(button => button.addEventListener('click', async () => {
      if (!window.confirm('Cancel this emergency alert? Only use this if help is no longer needed.')) return;
      button.disabled = true;
      try {
        const response = await fetch(`/api/v1/emergencies/${button.dataset.id}/cancel`, { method: 'PATCH' });
        if (!response.ok) throw new Error(await responseError(response));
        showMessage('Emergency alert cancelled.');
        loadMyRequests();
      } catch (error) {
        button.disabled = false;
        showMessage(error.message, true);
      }
}));

async function loadFeedbackEmergencies() {
  const select = document.querySelector('#feedback-emergency');
  try {
    const response = await fetch('/api/v1/emergencies/mine');
    if (!response.ok) throw new Error();
    const emergencies = await response.json();
    const resolved = emergencies.filter(item => item.status === 'RESOLVED');
    select.innerHTML = '<option value="">Choose a resolved emergency</option>';
    if (!resolved.length) {
      select.innerHTML = '<option value="">No resolved emergencies available yet</option>';
      select.disabled = true;
      return;
    }
    select.disabled = false;
    resolved.forEach(item => {
      const option = document.createElement('option');
      option.value = item.id;
      option.textContent = `${item.type.replaceAll('_', ' ')} — ${new Date(item.createdAt).toLocaleDateString('en-IN')}`;
      select.append(option);
    });
  } catch {
    select.innerHTML = '<option value="">Unable to load resolved emergencies</option>';
    select.disabled = true;
  }
}

async function loadFeedbackHistory() {
  let list = document.querySelector('#feedback-history');
  if (!list) {
    const heading = document.createElement('h3');
    heading.textContent = 'My submitted feedback';
    heading.style.marginTop = '24px';
    list = document.createElement('div');
    list.id = 'feedback-history';
    list.className = 'data-list';
    document.querySelector('.feedback-form').after(heading, list);
  }
  try {
    const response = await fetch('/api/v1/feedback/mine');
    if (!response.ok) throw new Error();
    const entries = await response.json();
    list.innerHTML = '';
    if (!entries.length) { list.textContent = 'You have not submitted feedback yet.'; return; }
    entries.forEach(entry => {
      const card = document.createElement('article');
      card.className = 'data-card';
      const title = document.createElement('b');
      title.textContent = `${entry.emergencyType.replaceAll('_', ' ')} - ${'★'.repeat(entry.rating)}${'☆'.repeat(5 - entry.rating)}`;
      const comment = document.createElement('span');
      comment.textContent = entry.comments || 'No comments provided.';
      const date = document.createElement('small');
      date.textContent = new Date(entry.createdAt).toLocaleString('en-IN');
      card.append(title, comment, date); list.append(card);
    });
  } catch { list.textContent = 'Unable to load feedback history.'; }
}
  }).catch(() => { emList.innerHTML = '<p style="color:#94a3b8">No data available.</p>'; });

  const relList = document.querySelector('#my-relief-list');
  fetch('/api/v1/relief-requests/mine').then(r => r.ok ? r.json() : Promise.reject()).then(data => {
    const list = Array.isArray(data) ? data : (data.content || []);
    relList.innerHTML = list.length ? list.map(x => card(`<b>${x.requestType}</b><span>${x.description || ''}</span><small>People: ${x.peopleCount || 1} · ${fmt(x.createdAt)}</small>`)).join('') : '<p style="color:#94a3b8">No relief requests submitted yet.</p>';
  }).catch(() => { relList.innerHTML = '<p style="color:#94a3b8">No data available.</p>'; });

  const blList = document.querySelector('#my-blood-list');
  fetch('/api/v1/blood-requests/mine').then(r => r.ok ? r.json() : Promise.reject()).then(data => {
    const list = Array.isArray(data) ? data : (data.content || []);
    blList.innerHTML = list.length ? list.map(x => card(`<b>${x.bloodGroup} · ${x.unitsRequired} units</b><span>${x.hospitalName}</span><small>${fmt(x.createdAt)}</small>`)).join('') : '<p style="color:#94a3b8">No blood requests submitted yet.</p>';
  }).catch(() => { blList.innerHTML = '<p style="color:#94a3b8">No data available.</p>'; });

  const donList = document.querySelector('#my-donation-list');
  fetch('/api/v1/donations/mine').then(r => r.ok ? r.json() : Promise.reject()).then(data => {
    const list = Array.isArray(data) ? data : (data.content || []);
    donList.innerHTML = list.length ? list.map(x => card(`<b>${x.donationType}</b><span>${x.itemDescription || ''}</span><small>${x.amount ? x.currency + ' ' + x.amount : ''} · ${fmt(x.createdAt)}</small>`)).join('') : '<p style="color:#94a3b8">No donations submitted yet.</p>';
  }).catch(() => { donList.innerHTML = '<p style="color:#94a3b8">No data available.</p>'; });
}

async function loadFeedbackEmergencies() {
  const select = document.querySelector('#feedback-emergency');
  try {
    const response = await fetch('/api/v1/emergencies/mine');
    if (!response.ok) throw new Error();
    const emergencies = await response.json();
    const resolved = (Array.isArray(emergencies) ? emergencies : emergencies.content || [])
      .filter(item => item.status === 'RESOLVED');
    select.innerHTML = '<option value="">Choose a resolved emergency</option>';
    if (!resolved.length) {
      select.innerHTML = '<option value="">No resolved emergencies available yet</option>';
      select.disabled = true;
      return;
    }
    select.disabled = false;
    resolved.forEach(item => {
      const option = document.createElement('option');
      option.value = item.id;
      option.textContent = `${item.type.replaceAll('_', ' ')} - ${new Date(item.createdAt).toLocaleDateString('en-IN')}`;
      select.append(option);
    });
  } catch {
    select.innerHTML = '<option value="">Unable to load resolved emergencies</option>';
    select.disabled = true;
  }
}

// Load account info
fetch('/api/v1/account/me').then(r => r.ok ? r.json() : Promise.reject())
  .then(account => {
    document.querySelector('#account-name').textContent = `${account.fullName} · ${account.roles.join(', ')}`;
    document.querySelector('#profileBtn').textContent = (account.fullName || 'U')[0].toUpperCase();
    document.querySelector('#pd-name').textContent = account.fullName;
    document.querySelector('#pd-email').textContent = account.email || '';
    document.querySelector('#pd-roles').innerHTML = (account.roles || []).map(r => `<span style="background:#10233e;color:#fff;border-radius:12px;padding:2px 9px;font-size:.7rem;font-weight:700">${r}</span>`).join(' ');
    if (account.roles.includes('ADMIN')) document.querySelector('.admin-only').style.display = 'block';
  })
  .catch(() => { window.location.href = '/login.html'; });

// SSE alerts
const alertStream = new EventSource('/api/v1/alerts/stream');
alertStream.addEventListener('emergency-alert', event => {
  try {
    const a = JSON.parse(event.data);
    if (a.eventType === 'EMERGENCY_STATUS') {
      showMessage(a.message || `Emergency status updated to ${a.status}.`);
      showResponderLocation(a);
      if (window.location.hash === '#myrequests') loadMyRequests();
      if (window.location.hash === '#feedback' && a.status === 'RESOLVED') loadFeedbackEmergencies();
      if ('Notification' in window && Notification.permission === 'granted') new Notification('SafeLink update', { body: a.message || 'Your emergency status has changed.' });
      return;
    }
    addUnreadAlert();
    showMessage(`New ${String(a.type).replaceAll('_', ' ')} alert: ${a.description || 'A nearby user needs help.'}`);
    if (window.location.hash === '#alerts') loadAlerts();
    if ('Notification' in window && Notification.permission === 'granted')
      new Notification(`SafeLink: ${String(a.type).replaceAll('_', ' ')}`, { body: a.description || 'A user needs help.' });
  } catch (_) { }
});
if ('Notification' in window && Notification.permission === 'default') Notification.requestPermission();

if (navigator.geolocation) navigator.geolocation.getCurrentPosition(({ coords }) => {
  fetch('/api/v1/responders/me/status', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ availabilityStatus: 'AVAILABLE', latitude: coords.latitude, longitude: coords.longitude }) }).catch(() => {});
}, () => {});

document.querySelector('#logout-btn').addEventListener('click', async () => {
  localStorage.removeItem('safelinkDisplayName');
  await fetch('/logout', { method: 'POST' }).catch(() => {});
  window.location.href = '/login.html?logout=true';
});
