import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm, type Resolver } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { api } from '../../lib/api'

// 1) schema y tipo a partir del schema
const schema = z.object({
  name: z.string().min(1, 'Requerido'),
  initialBalance: z.coerce.number().min(0, 'Debe ser ≥ 0'),
})
type FormData = z.infer<typeof schema>

export default function CreateAccountForm() {
  const qc = useQueryClient()

  // 2) resolver casteado para evitar el error de tipos
  const resolver = zodResolver(schema) as Resolver<FormData>

  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } =
    useForm<FormData>({
      resolver,
      defaultValues: { name: '', initialBalance: 0 },
    })

  const create = useMutation({
    mutationFn: (d: FormData) => api.createAccount(d),
    onSuccess: () => {
      reset()
      qc.invalidateQueries({ queryKey: ['accounts'] })
    },
  })

  // 3) sin SubmitHandler, tipamos inline
  const onSubmit = (d: FormData) => create.mutate(d)

  return (
    <form className="card space-y-3" onSubmit={handleSubmit(onSubmit)}>
      <div className="font-semibold">Nueva cuenta</div>

      <label className="label">Nombre</label>
      <input className="input" {...register('name')} />
      {errors.name && <p className="text-red-600 text-sm">{errors.name.message}</p>}

      <label className="label">Saldo inicial</label>
      <input
        className="input"
        type="number"
        step="0.01"
        {...register('initialBalance', { valueAsNumber: true })}
      />
      {errors.initialBalance && (
        <p className="text-red-600 text-sm">{errors.initialBalance.message}</p>
      )}

      <button className="btn w-full" disabled={isSubmitting || create.isPending}>
        {create.isPending ? 'Creando…' : 'Crear'}
      </button>

      {create.isError && (
        <p className="text-red-600 text-sm">
          {(create.error as any)?.message ?? 'Error al crear la cuenta'}
        </p>
      )}
    </form>
  )
}
