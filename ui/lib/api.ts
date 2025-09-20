import { auth } from './auth';

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

async function request(path: string, init: RequestInit = {}) {
  const res = await fetch(path.startsWith('/api') ? path : `/api${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(init.headers || {}),
      ...(auth.token ? { Authorization: `Bearer ${auth.token}` } : {}),
    },
  });

  if (!res.ok) {
    let msg = res.statusText;
    try {
      const j = await res.json();
      msg = j.message || j.error || msg;
    } catch {}
    if (res.status === 401) msg = 'Tu sesión expiró. Iniciá sesión otra vez.';
    if (res.status === 403) msg = 'No tenés permisos para esta acción.';
    throw new ApiError(res.status, msg);
  }

  return res.status === 204 ? null : res.json();
}

export const api = {
  listAccounts: () => request('/accounts'),
  createAccount: (body: { name: string; initialBalance: number }) =>
    request('/accounts', { method: 'POST', body: JSON.stringify(body) }),
  // 👇 esta ruta debe ser /accounts/transfer (no /transfers)
  transfer: (body: { from: string; to: string; amount: number }) =>
    request('/accounts/transfer', { method: 'POST', body: JSON.stringify(body) }),
};

