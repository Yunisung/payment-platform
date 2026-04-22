import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { paymentsApi } from '../api/payments'
import Layout from '../components/Layout'

const CURRENCIES = ['KRW', 'USD', 'EUR', 'JPY']

export default function CreatePaymentPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    idempotencyKey: crypto.randomUUID(),
    amount: '',
    currency: 'KRW',
    orderName: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const refreshKey = () => setForm((f) => ({ ...f, idempotencyKey: crypto.randomUUID() }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const result = await paymentsApi.create({
        ...form,
        amount: Number(form.amount),
      })
      navigate(`/payments/${result.paymentId}`)
    } catch (err) {
      setError(err.message ?? '결제 요청에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Layout>
      <div className="max-w-lg mx-auto">
        <div className="mb-6">
          <h1 className="text-xl font-bold text-gray-900">결제 생성</h1>
          <p className="mt-1 text-sm text-gray-500">새로운 결제를 요청합니다.</p>
        </div>

        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
          {error && (
            <div className="mb-5 p-3 bg-red-50 border border-red-100 rounded-lg text-sm text-red-600">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* 멱등성 키 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                멱등성 키
                <span className="ml-1 text-xs text-gray-400 font-normal">(중복 결제 방지)</span>
              </label>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={form.idempotencyKey}
                  onChange={(e) => setForm({ ...form, idempotencyKey: e.target.value })}
                  className="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-xs font-mono text-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                />
                <button
                  type="button"
                  onClick={refreshKey}
                  className="px-3 py-2 text-xs text-indigo-600 border border-indigo-200 rounded-lg hover:bg-indigo-50 transition-colors whitespace-nowrap"
                >
                  재생성
                </button>
              </div>
            </div>

            {/* 주문명 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">주문명</label>
              <input
                type="text"
                required
                maxLength={200}
                value={form.orderName}
                onChange={(e) => setForm({ ...form, orderName: e.target.value })}
                className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                placeholder="예: 아메리카노 2잔"
              />
            </div>

            {/* 금액 + 통화 */}
            <div className="flex gap-3">
              <div className="flex-1">
                <label className="block text-sm font-medium text-gray-700 mb-1">금액</label>
                <input
                  type="number"
                  required
                  min="0.01"
                  step="0.01"
                  value={form.amount}
                  onChange={(e) => setForm({ ...form, amount: e.target.value })}
                  className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  placeholder="10000"
                />
              </div>
              <div className="w-28">
                <label className="block text-sm font-medium text-gray-700 mb-1">통화</label>
                <select
                  value={form.currency}
                  onChange={(e) => setForm({ ...form, currency: e.target.value })}
                  className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white"
                >
                  {CURRENCIES.map((c) => (
                    <option key={c} value={c}>{c}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex gap-3 pt-2">
              <button
                type="button"
                onClick={() => navigate('/payments')}
                className="flex-1 border border-gray-200 text-gray-600 rounded-lg py-2.5 text-sm font-medium hover:bg-gray-50 transition-colors"
              >
                취소
              </button>
              <button
                type="submit"
                disabled={loading}
                className="flex-1 bg-indigo-600 text-white rounded-lg py-2.5 text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {loading ? '처리 중...' : '결제 요청'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Layout>
  )
}
