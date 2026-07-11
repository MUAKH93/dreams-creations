import { useEffect, useState } from 'react'
import { Typography, message, Spin } from 'antd'
import { useAuth } from '../../context/AuthContext'
import { productionAPI } from '../../api/production'
import { salesAPI } from '../../api/sales'
import { dashboardAPI } from '../../api/dashboard'
import { apiErrorMessage } from '../../api/client'
import AdminManagerDashboard from './AdminManagerDashboard'
import SupervisorDashboard from './SupervisorDashboard'
import CustomerDashboard from './CustomerDashboard'

const { Title } = Typography

export default function DashboardPage() {
  const { auth } = useAuth()
  const role = auth?.role
  const [loading, setLoading] = useState(true)
  const [summary, setSummary] = useState(null)
  const [alerts, setAlerts] = useState([])
  const [batches, setBatches] = useState([])
  const [assignments, setAssignments] = useState([])
  const [balance, setBalance] = useState(null)
  const [bills, setBills] = useState([])
  const [designs, setDesigns] = useState([])

  const fmtMoney = (v) => Number(v || 0).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })

  useEffect(() => {
    setLoading(true)

    if (role === 'ADMIN' || role === 'MANAGER') {
      Promise.allSettled([
        dashboardAPI.getSummary(),
        salesAPI.getAlerts(),
        productionAPI.getBatches(),
      ]).then(([summaryRes, alertsRes, batchesRes]) => {
        if (summaryRes.status === 'fulfilled') setSummary(summaryRes.value.data)
        if (alertsRes.status === 'fulfilled') setAlerts(alertsRes.value.data)
        if (batchesRes.status === 'fulfilled') setBatches(batchesRes.value.data)
      }).finally(() => setLoading(false))
      return
    }

    if (role === 'SUPERVISOR') {
      productionAPI.getMyAssignments()
        .then(r => setAssignments(r.data))
        .catch(err => message.error(apiErrorMessage(err)))
        .finally(() => setLoading(false))
      return
    }

    if (role === 'CUSTOMER') {
      Promise.allSettled([
        salesAPI.getMyBalance(),
        salesAPI.getMyBills(),
        productionAPI.getDesigns(),
      ]).then(([bal, bill, des]) => {
        if (bal.status === 'fulfilled') setBalance(bal.value.data)
        if (bill.status === 'fulfilled') setBills(bill.value.data)
        if (des.status === 'fulfilled') setDesigns(des.value.data)
      }).finally(() => setLoading(false))
      return
    }

    setLoading(false)
  }, [role])

  const titles = {
    ADMIN: 'Admin Dashboard',
    MANAGER: 'Manager Dashboard',
    SUPERVISOR: 'Supervisor Dashboard',
    CUSTOMER: 'My Dashboard',
  }

  if (loading && !summary && !assignments.length && !bills.length) {
    return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>
  }

  return (
    <div>
      <Title level={4} className="page-title">{titles[role] || 'Dashboard'}</Title>

      {(role === 'ADMIN' || role === 'MANAGER') && (
        <AdminManagerDashboard
          role={role}
          summary={summary}
          alerts={alerts}
          batches={batches}
          loading={loading}
          fmtMoney={fmtMoney}
        />
      )}

      {role === 'SUPERVISOR' && (
        <SupervisorDashboard assignments={assignments} loading={loading} />
      )}

      {role === 'CUSTOMER' && (
        <CustomerDashboard balance={balance} bills={bills} designs={designs} loading={loading} />
      )}
    </div>
  )
}
