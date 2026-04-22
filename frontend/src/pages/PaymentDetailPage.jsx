import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { paymentsApi } from '../api/payments'
import Layout from '../components/Layout'
import StatusBadge from '../components/StatusBadge'
import Spinner from '../components/Spinner'

function Row({ label, children }) {
  return (
    <div className="flex py-3.5 border-b border-gray-50 last:border-0">
      <dt className="w-32 text-sm text-gray-500 shrink-0">{label}</dt>
      <dd className="text-sm text-gray-800 font-medium">{children}</dd>
    </div>
  )
}

function formatAmount(amount, currency) {
  return new Intl.NumberFormat('ko-KR', { style: 'currency', currency }).format(amount)
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  })
}

export default function PaymentDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [payment, setPayment] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [cancelling, setCancelling] = useState(false)
  const [confirmCancel, setConfirmCancel] = useState(false)

  useEffect(() => {
    paymentsApi.get(id)
      .then(setPayment)
      .catch((err) => setError(err.message ?? '결제 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [id])

  const handleCancel = async () => {
    setCancelling(true)
    try {
      const updated = await paymentsApi.cancel(id)
      setPayment(updated)
      setConfirmCancel(false)
    } catch (err) {
      setError(err.message ?? '결제 취소에 실패했습니다.')
    } finally {
      setCancelling(false)
    }
  }

  return (
    <Layout>
      <div className="max-w-lg mx-auto">
        <div className="flex items-center gap-2 mb-6">
          <button
            onClick={() => navigate('/payments')}
            className="text-sm text-gray-400 hover:text-gray-600"
          >
            ← 목록으로
          </button>
        </div>

        <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <h1 className="text-base font-semibold text-gray-900">결제 상세</h1>
            {payment && <StatusBadge status={payment.status} />}
          </div>

          {loading ? (
            <div className="flex justify-center items-center h-48">
              <Spinner />
            </div>
          ) : error ? (
            <div className="flex flex-col items-center justify-center h-48 gap-3">
              <p className="text-sm text-red-500">{error}</p>
              <button
                onClick={() => navigate('/payments')}
                className="text-sm text-indigo-600 hover:underline"
              >
                목록으로 돌아가기
              </button>
            </div>
          ) : payment ? (
            <>
              <dl className="px-6 py-2">
                <Row label="결제 ID"># {payment.paymentId}</Row>
                <Row label="주문명">{payment.orderName}</Row>
                <Row label="금액">{formatAmount(payment.amount, payment.currency)}</Row>
                <Row label="통화">{payment.currency}</Row>
                <Row label="상태"><StatusBadge status={payment.status} /></Row>
                <Row label="생성일시">{formatDate(payment.createdAt)}</Row>
                <Row label="멱등성 키">
                  <span className="font-mono text-xs text-gray-500 break-all">
                    {payment.idempotencyKey}
                  </span>
                </Row>
              </dl>

              {/* 취소 버튼 영역 */}
              {payment.status === 'APPROVED' && (
                <div className="px-6 py-4 bg-gray-50 border-t border-gray-100">
                  {!confirmCancel ? (
                    <button
                      onClick={() => setConfirmCancel(true)}
                      className="w-full border border-red-200 text-red-600 rounded-lg py-2 text-sm font-medium hover:bg-red-50 transition-colors"
                    >
                      결제 취소
                    </button>
                  ) : (
                    <div className="space-y-2">
                      <p className="text-sm text-center text-gray-600 font-medium">정말 취소하시겠습니까?</p>
                      <div className="flex gap-2">
                        <button
                          onClick={() => setConfirmCancel(false)}
                          className="flex-1 border border-gray-200 text-gray-600 rounded-lg py-2 text-sm hover:bg-white transition-colors"
                        >
                          아니요
                        </button>
                        <button
                          onClick={handleCancel}
                          disabled={cancelling}
                          className="flex-1 bg-red-600 text-white rounded-lg py-2 text-sm font-medium hover:bg-red-700 disabled:opacity-50 transition-colors"
                        >
                          {cancelling ? '처리 중...' : '취소 확인'}
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </>
          ) : null}
        </div>
      </div>
    </Layout>
  )
}
