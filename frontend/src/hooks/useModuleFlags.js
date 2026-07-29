import { useEffect, useState } from 'react'
import { financeModuleEnabled, shopModuleEnabled } from '../config/modules'
import { modulesAPI } from '../api/modules'

/**
 * Module flags from env (initial) + GET /api/modules (authoritative when backend is up).
 */
export function useModuleFlags() {
  const [showFinance, setShowFinance] = useState(financeModuleEnabled)
  const [showShop, setShowShop] = useState(shopModuleEnabled)

  useEffect(() => {
    modulesAPI.getFlags()
      .then(r => {
        if (r.data?.finance?.enabled) setShowFinance(true)
        if (r.data?.shop?.enabled) setShowShop(true)
      })
      .catch(() => {})
  }, [])

  return { showFinance, showShop }
}
