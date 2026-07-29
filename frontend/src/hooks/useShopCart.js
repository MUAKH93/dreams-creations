import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { shopAPI } from '../api/shop'
import {
  readGuestCart,
  writeGuestCart,
  clearGuestCart,
  addGuestCartLine,
  guestCartCount,
  guestCartSummary,
} from '../utils/shopGuestCart'

export function useShopCart() {
  const { auth } = useAuth()
  const isCustomer = auth?.role === 'CUSTOMER'
  const [cart, setCart] = useState(null)
  const [itemCount, setItemCount] = useState(0)
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(async () => {
    if (isCustomer && auth?.token) {
      setLoading(true)
      try {
        const res = await shopAPI.getCart()
        setCart(res.data)
        setItemCount(res.data?.itemCount || 0)
      } catch {
        setCart(null)
        setItemCount(0)
      } finally {
        setLoading(false)
      }
      return
    }
    const guest = guestCartSummary()
    setCart(guest)
    setItemCount(guest.itemCount)
  }, [isCustomer, auth?.token])

  useEffect(() => {
    refresh()
  }, [refresh])

  useEffect(() => {
    if (!isCustomer || !auth?.token) return
    const guestItems = readGuestCart()
    if (!guestItems.length) return
    shopAPI.mergeGuestCart(guestItems.map(i => ({
      productId: i.productId,
      quantity: i.quantity,
    })))
      .then(() => {
        clearGuestCart()
        refresh()
      })
      .catch(() => {})
  }, [isCustomer, auth?.token, refresh])

  const addItem = useCallback(async (variant, design, quantity = 1) => {
    if (!variant?.productId) {
      throw new Error('Select a size and color first')
    }
    if (isCustomer && auth?.token) {
      await shopAPI.addCartItem({ productId: variant.productId, quantity })
      await refresh()
      return
    }
    addGuestCartLine(variant, { ...design, primaryImageUrl: design?.primaryImageUrl }, quantity)
    await refresh()
  }, [isCustomer, auth?.token, refresh])

  const updateQuantity = useCallback(async (itemId, quantity, isGuestLine = false) => {
    if (isGuestLine || !isCustomer) {
      const items = readGuestCart().map(i =>
        i.productId === itemId ? { ...i, quantity } : i,
      ).filter(i => i.quantity > 0)
      writeGuestCart(items)
      await refresh()
      return
    }
    await shopAPI.updateCartItem(itemId, quantity)
    await refresh()
  }, [isCustomer, refresh])

  const removeItem = useCallback(async (itemId, isGuestLine = false) => {
    if (isGuestLine || !isCustomer) {
      const items = readGuestCart().filter(i => i.productId !== itemId)
      writeGuestCart(items)
      await refresh()
      return
    }
    await shopAPI.removeCartItem(itemId)
    await refresh()
  }, [isCustomer, refresh])

  const clearCart = useCallback(async () => {
    if (isCustomer && auth?.token) {
      await shopAPI.clearCart()
    } else {
      clearGuestCart()
    }
    await refresh()
  }, [isCustomer, auth?.token, refresh])

  return {
    cart,
    itemCount,
    loading,
    isCustomer,
    isGuest: !isCustomer || !auth?.token,
    refresh,
    addItem,
    updateQuantity,
    removeItem,
    clearCart,
    guestCartCount,
  }
}
