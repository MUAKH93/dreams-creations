import { Button } from 'antd'
import { AppstoreOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'

export default function BackToModuleHub({ className = '' }) {
  const navigate = useNavigate()
  return (
    <div className={`app-module-hub-back ${className}`.trim()}>
      <Button
        type="text"
        block
        icon={<AppstoreOutlined />}
        onClick={() => navigate('/modules')}
        style={{ color: 'rgba(255,255,255,0.85)', textAlign: 'left', height: 40 }}
      >
        All systems
      </Button>
    </div>
  )
}
