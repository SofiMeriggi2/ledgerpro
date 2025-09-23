import { auth } from './auth';

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

export type UUID = string;

export interface Account {
  id: UUID;
  name: string;
  balance: number;
  ownerId: UUID;
  createdAt: string;
}

export interface LedgerEntry {
  id: UUID;
  amount: number;
  kind: 'CREDIT' | 'DEBIT';
  at: string;
  account?: Account;
  accountId?: UUID;
}

export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first?: boolean;
  last?: boolean;
  empty?: boolean;
}

type RequestInitWithBody = RequestInit & { body?: BodyInit | null };

async function request<T>(path: string, init: RequestInitWithBody = {}): Promise<T> {
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

  if (res.status === 204) {
    return null as T;
  }

  return (await res.json()) as T;
}

type CreateAccountBody = { name: string; initialBalance: number };
type TransferBody = { from: UUID; to: UUID; amount: number };
type EntryBody = { amount: number; kind: 'CREDIT' | 'DEBIT' };

export const api = {
  listAccounts: () => request<Account[]>('/accounts'),
  createAccount: (body: CreateAccountBody) =>
    request<Account>('/accounts', { method: 'POST', body: JSON.stringify(body) }),
  listEntries: (id: UUID, page = 0, size = 20) =>
    request<Page<LedgerEntry>>(`/accounts/${id}/entries?page=${page}&size=${size}`),
  addEntry: (id: UUID, body: EntryBody) =>
    request<LedgerEntry>(`/accounts/${id}/entries`, { method: 'POST', body: JSON.stringify(body) }),
  // 👇 esta ruta debe ser /accounts/transfer (no /transfers)
  transfer: (body: TransferBody) =>
    request<void>('/accounts/transfer', { method: 'POST', body: JSON.stringify(body) }),
};

