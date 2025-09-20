import { useQuery } from '@tanstack/react-query'
import { api, type UUID } from '../../lib/api'
import { AddEntryForm } from './AddEntryForm'

export function AccountDetails({ id }: { id: UUID }) {
  const { data, isLoading, error } = useQuery({
    queryKey: ['entries', id, 0, 20],
    queryFn: () => api.listEntries(id, 0, 20),
  })

  if (isLoading) return <div className="card">Cargando movimientos...</div>
  if (error) return <div className="card text-red-600">Error: {(error as Error).message}</div>

  return (
    <div className="space-y-4">
      <AddEntryForm accountId={id} />
      <div className="card">
        <div className="font-semibold mb-2">Últimos movimientos</div>
        <table className="w-full text-sm">
          <thead className="text-left text-gray-500">
            <tr><th className="py-2">Fecha</th><th>Tipo</th><th className="text-right">Importe</th></tr>
          </thead>
          <tbody>
            {data!.content.map(e => (
              <tr key={e.id} className="border-t">
                <td className="py-2">{new Date(e.at).toLocaleString()}</td>
                <td>{e.kind}</td>
                <td className="text-right">${e.amount.toFixed(2)}</td>
              </tr>
            ))}
            {data!.content.length === 0 && <tr><td colSpan={3} className="py-4 text-center text-gray-500">Sin movimientos</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  )
}
