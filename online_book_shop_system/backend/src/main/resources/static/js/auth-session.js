/**
 * Form login (/perform-login) only sets JSESSIONID. Static shop JS uses JWT in localStorage.
 * Call this on page load (with credentials) to copy session → localStorage token.
 */
async function syncAuthFromServerSession(apiBase) {
    const base = apiBase != null ? String(apiBase) : '';
    if (localStorage.getItem('token')) {
        return true;
    }
    try {
        const r = await fetch(base + '/api/auth/session-token', {
            method: 'GET',
            credentials: 'include',
            headers: { Accept: 'application/json' }
        });
        if (r.status === 401 || r.status === 403) {
            return false;
        }
        if (!r.ok) {
            return false;
        }
        const d = await r.json();
        if (d && d.token) {
            localStorage.setItem('token', d.token);
            if (d.email != null) {
                localStorage.setItem('user', JSON.stringify({
                    email: d.email,
                    fullName: d.fullName,
                    role: d.role
                }));
            }
            return true;
        }
    } catch (e) {
        console.debug('syncAuthFromServerSession', e);
    }
    return false;
}

/**
 * Clear JWT and Spring session (JSESSIONID). Without server logout, /login still sees you as logged in
 * and redirects back to the shop.
 */
async function performLogout(apiBase) {
    const base = apiBase != null ? String(apiBase) : '';
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    const root = base.endsWith('/') ? base.slice(0, -1) : base;
    const logoutUrl = root ? root + '/logout' : '/logout';
    try {
        await fetch(logoutUrl, { method: 'POST', credentials: 'include' });
    } catch (e) {
        console.debug('performLogout', e);
    }
    window.location.assign(root ? root + '/login?logout' : '/login?logout');
}
