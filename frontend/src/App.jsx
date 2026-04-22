import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import PaymentsPage from './pages/PaymentsPage'
import CreatePaymentPage from './pages/CreatePaymentPage'
import PaymentDetailPage from './pages/PaymentDetailPage'

function PrivateRoute({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/login" replace />
}

function PublicRoute({ children }) {
  const { user } = useAuth()
  return user ? <Navigate to="/payments" replace /> : children
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
          <Route path="/signup" element={<PublicRoute><SignupPage /></PublicRoute>} />
          <Route path="/payments" element={<PrivateRoute><PaymentsPage /></PrivateRoute>} />
          <Route path="/payments/new" element={<PrivateRoute><CreatePaymentPage /></PrivateRoute>} />
          <Route path="/payments/:id" element={<PrivateRoute><PaymentDetailPage /></PrivateRoute>} />
          <Route path="*" element={<Navigate to="/payments" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
