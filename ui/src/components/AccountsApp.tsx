import { useState } from 'react';
import CreateAccountForm from './CreateAccountForm';
import AccountList from './AccountList';
import TransferForm from './TransferForm';
import { AccountDetails } from './AccountDetails';

export default function AccountsApp() {
  const [selected, setSelected] = useState<string | null>(null);

  return (
    <div className="max-w-6xl mx-auto p-6">
      <header className="mb-6">
        <h1 className="text-3xl font-bold">LedgerPro</h1>
        <p className="text-gray-600">Cuentas, movimientos y transferencias</p>
      </header>

      <div className="grid grid-cols-12 gap-6">
        <aside className="col-span-4 space-y-4">
          <CreateAccountForm />
          <AccountList selectedId={selected} onSelect={setSelected} />
        </aside>

        <section className="col-span-8 space-y-4">
          <TransferForm />
          {!selected ? (
            <div className="card">Seleccioná una cuenta para ver sus movimientos.</div>
          ) : (
            <AccountDetails id={selected} />
          )}
        </section>
      </div>
    </div>
  );
}
