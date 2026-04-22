const styles = {
  PENDING:   'bg-yellow-100 text-yellow-800',
  APPROVED:  'bg-green-100  text-green-800',
  CANCELLED: 'bg-gray-100   text-gray-600',
  FAILED:    'bg-red-100    text-red-800',
}

const labels = {
  PENDING:   '대기',
  APPROVED:  '승인',
  CANCELLED: '취소',
  FAILED:    '실패',
}

export default function StatusBadge({ status }) {
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${styles[status] ?? 'bg-gray-100 text-gray-600'}`}>
      {labels[status] ?? status}
    </span>
  )
}
