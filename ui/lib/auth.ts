const STORAGE = 'lp_token';

type Subscriber = () => void;

function read(): string | null { return localStorage.getItem(STORAGE); }
function write(v: string | null) {
  if (v) localStorage.setItem(STORAGE, v);
  else localStorage.removeItem(STORAGE);
}

const subs = new Set<Subscriber>();
const notify = () => subs.forEach(fn => fn());

export async function request(path: string, body?: any, method: string = 'POST') {
  const res = await fetch(`/api${path}`, {
    method,
    headers: { 'content-type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    let msg = res.statusText;
    try { msg = (await res.json()).message ?? msg; } catch {}
    throw new Error(msg);
  }
  return res.json();
}

export const auth = {
  get token(): string | null { return read(); },
  set token(v: string | null) { write(v); notify(); },

  onChange(fn: Subscriber): () => void {
    subs.add(fn);
    return () => subs.delete(fn);
  },

  async login(email: string, password: string) {
    const data = await request('/auth/login', { email, password });
    this.token = data.token;
  },

  async signup(email: string, password: string) {
    const data = await request('/auth/signup', { email, password });
    this.token = data.token;
  },

  logout() { this.token = null; }
};

// Útil si quieres llamar endpoints del backend con auth incluida
export async function api(path: string, body?: unknown, method?: 'GET'|'POST'|'DELETE') {
  return request(path, body, method);
}
