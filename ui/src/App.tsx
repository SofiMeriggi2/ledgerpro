import { useEffect, useState } from 'react';
import { auth } from '../lib/auth';
import AuthScreen from './components/AuthScreen';
import AccountsApp from './components/AccountsApp';

export default function App() {
  const [, force] = useState(0);
  const signed = !!auth.token;

  useEffect(() => {
    // re-render si cambia el token
    return auth.onChange(() => force(x => x + 1));
  }, []);

  if (!signed) return (
    <>
      <Header signed={false} />
      <AuthScreen onDone={() => force(x => x + 1)} />
    </>
  );

  return (
    <>
      <Header signed />
      <AccountsApp />
    </>
  );
}

function Header({ signed = false }: { signed?: boolean }) {
  return (
    <header className="bg-slate-900 text-white">
      <div className="max-w-6xl mx-auto flex items-center justify-between p-3">
        <h1 className="text-2xl font-extrabold tracking-tight">LedgerPro</h1>
        {signed && (
          <button className="btn !bg-white !text-slate-900" onClick={() => auth.logout()}>
            Cerrar sesión
          </button>
        )}
      </div>
    </header>
  );
}
