import { useState } from 'react';
import { auth } from '../../lib/auth';

type Props = { onDone?: () => void };

export default function AuthScreen({ onDone }: Props) {
  const [mode, setMode] = useState<'login'|'signup'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string|null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true); setError(null);
    try {
      if (mode === 'login') await auth.login(email, password);
      else await auth.signup(email, password);
      onDone?.();
    } catch (err: any) {
      setError(err?.message ?? 'Error');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-[85vh] bg-slate-50 flex items-start justify-center">
      <div className="w-full max-w-md mt-16">
        <div className="bg-white shadow-xl rounded-2xl overflow-hidden">
          <div className="flex">
            <button
              className={`flex-1 py-3 text-sm font-medium ${mode==='login' ? 'bg-slate-900 text-white' : 'bg-slate-100'}`}
              onClick={() => setMode('login')}
            >
              Iniciar sesión
            </button>
            <button
              className={`flex-1 py-3 text-sm font-medium ${mode==='signup' ? 'bg-slate-900 text-white' : 'bg-slate-100'}`}
              onClick={() => setMode('signup')}
            >
              Registrarse
            </button>
          </div>

          <form onSubmit={onSubmit} className="p-6 space-y-4">
            <label className="block">
              <div className="label">Email</div>
              <input
                className="input"
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                required
              />
            </label>

            <label className="block">
              <div className="label">Contraseña</div>
              <input
                className="input"
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
              />
            </label>

            {error && <div className="card text-red-600">{error}</div>}

            <button className="btn w-full" disabled={loading}>
              {loading ? 'Procesando…' : (mode === 'login' ? 'Ingresar' : 'Crear cuenta')}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
