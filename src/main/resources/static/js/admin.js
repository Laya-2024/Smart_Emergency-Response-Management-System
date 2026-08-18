async function load() {
  const [applicationsResponse, metricsResponse, accountsResponse] = await Promise.all([
    fetch('/api/v1/role-applications/pending'),
    fetch('/api/v1/admin/metrics'),
    fetch('/api/v1/admin/accounts')
  ]);
  const applications = await applicationsResponse.json();
  const metrics = await metricsResponse.json();
  const accounts = await accountsResponse.json();

  document.querySelector('#metrics').textContent = JSON.stringify(metrics, null, 2);
  document.querySelector('#accounts').innerHTML = accounts.map(account => {
    const identity = account.emailVerified && account.phoneVerified ? 'Verified' : 'Email/phone verification incomplete';
    const application = account.applicationStatus
      ? `${account.requestedRole}: ${account.applicationStatus} (${account.documentCount}/2 documents)`
      : 'No responder application';
    return `<article><b>${account.name}</b> - ${account.email}<br><small>Roles: ${account.roles.join(', ')} | Account: ${identity}<br>Responder application: ${application}</small></article>`;
  }).join('') || 'No accounts registered.';

  document.querySelector('#applications').innerHTML = applications.map(application => {
    const requiredDocuments = ['CITIZEN', 'DONOR'].includes(application.requestedRole) ? 1 : 2;
    return `
    <article>
      <b>${application.applicantName}</b><br>
      <span>${application.requestedRole} - ${application.organisationName}</span><br>
      <small>${application.documentCount}/${requiredDocuments} required documents uploaded</small><br>
      <button ${application.documentCount < requiredDocuments ? 'disabled title="Required verification document is missing"' : ''} onclick="review('${application.id}', true)">Approve</button>
      <button onclick="review('${application.id}', false)">Reject</button>
    </article>`;
  }).join('') || 'No pending applications.';
}

async function review(id, approved) {
  const response = await fetch(`/api/v1/role-applications/${id}/review`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ approved, notes: '' })
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    alert(body.detail || 'Review could not be completed.');
  }
  load();
}

load().catch(() => { document.querySelector('#applications').textContent = 'Unable to load administration data.'; });
