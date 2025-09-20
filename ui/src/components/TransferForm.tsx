// ui/src/components/TransferForm.tsx
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm, type Resolver } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { api } from '../../lib/api'

type Account = { id: string; name: string }

const schema = z.object({
  from: z.string().uuid('Elegí una cuenta'),
  to: z.string().uuid('Elegí una cuenta'),
  amount: z.coerce.number().positive('Monto > 0'),
}).refine(d => d.from !== d.to, {
  message: 'Origen y destino no pueden ser iguales',
  path: ['to'],
})

type FormData = z.infer<typeof schema>

export default function TransferForm() {
  const qc = useQueryClient()
  const { data: accounts = [], isLoading } = useQuery({
    queryKey: ['accounts'],
    queryFn: api.listAccounts,
  })

  // 👇 mismo fix que en CreateAccountForm
  const resolver = zodResolver(schema) as Resolver<FormData>

  const { register, handleSubmit, formState: { errors, isSubmitting }, reset, watch } =
    useForm<FormData>({ resolver, defaultValues: { amount: 0 } })

  const transfer = useMutation({
    mutationFn: (d: FormData) => api.transfer(d),
    onSuccess: () => {
      reset({ amount: 0, from: undefined, to: undefined } as any)
      qc.invalidateQueries({ queryKey: ['accounts'] })
    },
  })

  // 👇 onSubmit va ADENTRO del componente
  const onSubmit = (d: FormData) => transfer.mutate(d)

  const from = watch('from')
  const to = watch('to')

  return (
    <form className="card space-y-3" onSubmit={handleSubmit(onSubmit)}>
      <div className="font-semibold">Transferir entre cuentas</div>

      <label className="label">Desde</label>
      <select className="input" {...register('from')}>
        <option value="">Seleccioná origen</option>
        {accounts.map((a: Account) => (
          <option key={a.id} value={a.id}>{a.name}</option>
        ))}
      </select>
      {errors.from && <p className="text-red-600 text-sm">{errors.from.message}</p>}

      <label className="label">Hacia</label>
      <select className="input" {...register('to')}>
        <option value="">Seleccioná destino</option>
        {accounts.map((a: Account) => (
          <option key={a.id} value={a.id}>{a.name}</option>
        ))}
      </select>
      {errors.to && <p className="text-red-600 text-sm">{errors.to.message}</p>}

      <label className="label">Monto</label>
      <input
        className="input"
        type="number"
        step="0.01"
        {...register('amount', { valueAsNumber: true })}
      />
      {errors.amount && <p className="text-red-600 text-sm">{errors.amount.message}</p>}

      <button
        className="btn w-full"
        disabled={isSubmitting || transfer.isPending || isLoading || !from || !to || from === to}
      >
        {transfer.isPending ? 'Transfiriendo…' : 'Transferir'}
      </button>

      {transfer.isError && (
        <p className="text-red-600 text-sm">
          {(transfer.error as any)?.message ?? 'Error al transferir'}
        </p>
      )}
    </form>
  )
}
