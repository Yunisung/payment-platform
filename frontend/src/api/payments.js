import { api } from './client'

export const paymentsApi = {
  list: ({ page = 0, size = 10, status } = {}) => {
    const params = new URLSearchParams({ page, size })
    if (status) params.set('status', status)
    return api.get(`/payments?${params}`)
  },
  get: (id) => api.get(`/payments/${id}`),
  create: (data) => api.post('/payments', data),
  cancel: (id) => api.delete(`/payments/${id}`),
}
