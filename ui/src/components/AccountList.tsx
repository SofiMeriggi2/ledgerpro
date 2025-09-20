import { useQuery } from '@tanstack/react-query'
import { api, type Account } from '../../lib/api'

export default function AccountList(props: {
  selectedId?: string | null
  onSelect: (id: string) => void
}) {
  const { data, isLoading, error } = useQuery({
    queryKey: ['accounts'],
    queryFn: api.listAccounts,
  })

  // siempre un array, aunque haya error
  const list: Account[] = Array.isArray(data) ? data : []

  if (isLoading) return <div className="card">Cargando cuentas…</div>
  if (error) return <div className="card text-red-600">Error: {(error as Error).message}</div>

  return (
    <div className="card">
      <div className="font-semibold mb-2">Cuentas</div>
      <ul className="divide-y">
        {list.map((a) => (
          <li
            key={a.id}
            className={`py-2 cursor-pointer ${props.selectedId === a.id ? 'font-semibold' : ''}`}
            onClick={() => props.onSelect(a.id)}
            title={a.id}
          >
            <div className="flex justify-between">
              <span>{a.name}</span>
              <span>${a.balance.toFixed(2)}</span>
            </div>
          </li>
        ))}
        {list.length === 0 && (
          <li className="py-2 text-gray-500">No hay cuentas aún.</li>
        )}
      </ul>
    </div>
  )
}
