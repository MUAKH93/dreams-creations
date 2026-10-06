import { useEffect, useState } from 'react'
import { Typography } from 'antd'
import { RightOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { financeModuleEnabled } from '../../config/modules'
import { getErpModules } from '../../config/moduleHub'
import { modulesAPI } from '../../api/modules'
import BrandLogo from '../../components/BrandLogo'
import '../../styles/module-hub.css'

const { Text } = Typography

export default function ModuleHubPage() {
  const { auth } = useAuth()
  const navigate = useNavigate()
  const [showFinance, setShowFinance] = useState(financeModuleEnabled)

  useEffect(() => {
    modulesAPI.getFlags()
      .then(r => { if (r.data?.finance?.enabled) setShowFinance(true) })
      .catch(() => {})
  }, [])

  const modules = getErpModules(auth?.role, { showFinance })

  return (
    <div className="module-hub-page">
      <div className="module-hub-page__header">
        <BrandLogo variant="inline" />
        <h1 style={{ marginTop: 16 }}>Dreams Creations ERP</h1>
        <p>Production and Finance — factory operations and accounting in one app.</p>
      </div>

      <div className="module-hub-grid">
        {modules.map(mod => {
          const Icon = mod.icon
          return (
            <button
              key={mod.key}
              type="button"
              className="module-hub-card"
              onClick={() => navigate(mod.route)}
            >
              <div className="module-hub-card__icon" style={{ background: mod.accent, color: mod.color }}>
                <Icon />
              </div>
              <h2 className="module-hub-card__title" style={{ color: mod.color }}>{mod.title}</h2>
              <p className="module-hub-card__desc">{mod.description}</p>
              <Text className="module-hub-card__action" style={{ color: mod.color }}>
                Open module <RightOutlined />
              </Text>
            </button>
          )
        })}
      </div>
    </div>
  )
}
