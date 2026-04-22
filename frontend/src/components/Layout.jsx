import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path) => location.pathname === path

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white border-b border-gray-200">
        <div className="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <Link to="/payments" className="text-indigo-600 font-bold text-lg tracking-tight">
              PayPlatform
            </Link>
            <Link
              to="/payments"
              className={`text-sm font-medium ${isActive('/payments') ? 'text-indigo-600' : 'text-gray-500 hover:text-gray-800'}`}
            >
              결제 내역
            </Link>
            <Link
              to="/payments/new"
              className={`text-sm font-medium ${isActive('/payments/new') ? 'text-indigo-600' : 'text-gray-500 hover:text-gray-800'}`}
            >
              결제 생성
            </Link>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-sm text-gray-500">{user?.email}</span>
            <button
              onClick={handleLogout}
              className="text-sm text-gray-500 hover:text-gray-800 border border-gray-200 rounded px-3 py-1"
            >
              로그아웃
            </button>
          </div>
        </div>
      </nav>
      <main className="max-w-6xl mx-auto px-4 py-8">{children}</main>
    </div>
  )
}
