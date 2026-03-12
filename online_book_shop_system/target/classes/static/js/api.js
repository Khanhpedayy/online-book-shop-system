/**
 * API Client — Fetch wrapper for Spring Boot backend
 * Base URL: /api (same origin)
 */
const API = {
    BASE: '/api',

    async request(method, url, body = null) {
        const opts = {
            method,
            headers: { 'Content-Type': 'application/json' },
        };
        if (body) opts.body = JSON.stringify(body);

        const res = await fetch(this.BASE + url, opts);

        if (!res.ok) {
            let msg = `Error ${res.status}`;
            try {
                const err = await res.json();
                msg = err.message || err.error || msg;
            } catch (_) { }
            throw new Error(msg);
        }

        const text = await res.text();
        return text ? JSON.parse(text) : null;
    },

    get(url) { return this.request('GET', url); },
    post(url, body) { return this.request('POST', url, body); },
    put(url, body) { return this.request('PUT', url, body); },
    patch(url, body) { return this.request('PATCH', url, body); },
    delete(url) { return this.request('DELETE', url); },
};

/* Lowercase alias — pages use `api.get(...)` */
const api = API;
