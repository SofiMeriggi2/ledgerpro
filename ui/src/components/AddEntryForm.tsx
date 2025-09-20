import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { api, type UUID } from '../../lib/api'
import { useMutation, useQueryClient } from '@tanstack/react-query'

const schema = z.object({
  amount: z.coerce.number().positive('> 0'),
  kind: z.enum(['CREDIT', 'DEBIT']),
})
type FormData = z.infer<typeof schema>

export function AddEntryForm({ accountId }: { accountId: UUID }) {
  const qc = useQueryClient()
  const { register, handleSubmit, formState: { errors }, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { amount: 0, kind: 'DEBIT' }
  })

  const add = useMutation({
    mutationFn: (d: FormData) => api.addEntry(accountId, d),
    onSuccess: () => {
      reset()
      qc.invalidateQueries({ queryKey: ['entries', accountId] })
      qc.invalidateQueries({ queryKey: ['accounts'] }) // balance
    }
  })

  return (
    <form className="card space-y-3" onSubmit={handleSubmit(d => add.mutate(d))}>
      <div className="font-semibold">Nuevo movimiento</div>

      <label className="label">Importe</label>
      <input className="input" type="number" step="0.01" {...register('amount', { valueAsNumber: true })} />
      {errors.amount && <p className="text-red-600 text-sm">{errors.amount.message}</p>}

      <label className="label">Tipo</label>
      <select className="input" {...register('kind')}>
        <option value="DEBIT">DEBIT (sale)</option>
        <option value="CREDIT">CREDIT (entra)</option>
      </select>

      <button className="btn" disabled={add.isPending}>
        {add.isPending ? 'Agregando...' : 'Agregar'}
      </button>
    </form>
  )
}
