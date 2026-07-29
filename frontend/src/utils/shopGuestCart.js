const GUEST_CART_KEY = 'dc_shop_guest_cart'

export function readGuestCart() {
  try {
    const raw = localStorage.getItem(GUEST_CART_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

export function writeGuestCart(items) {
  localStorage.setItem(GUEST_CART_KEY, JSON.stringify(items || []))
}

export function clearGuestCart() {
  localStorage.removeItem(GUEST_CART_KEY)
}

export function guestCartCount(items = readGuestCart()) {
  return items.reduce((sum, line) => sum + Number(line.quantity || 0), 0)
}

export function addGuestCartLine(variant, design, quantity = 1) {
  const items = readGuestCart()
  const productId = variant.productId
  const existing = items.find(i => i.productId === productId)
  const qty = Math.max(1, Number(quantity) || 1)

  if (existing) {
    existing.quantity = Number(existing.quantity || 0) + qty
  } else {
    items.push({
      productId,
      quantity: qty,
      designId: design?.designId,
      designCode: design?.designCode,
      designName: design?.name,
      sizeValue: variant.sizeValue,
      color: variant.color,
      unitPrice: variant.sellingPrice,
      primaryImageUrl: design?.primaryImageUrl,
      stockAvailable: variant.stockQty,
    })
  }

  writeGuestCart(items)
  return items
}

export function guestCartSummary(items = readGuestCart()) {
  const subtotal = items.reduce(
    (sum, line) => sum + Number(line.unitPrice || 0) * Number(line.quantity || 0),
    0,
  )
  return {
    items,
    itemCount: guestCartCount(items),
    subtotal,
    discountPercent: 0,
    discountAmount: 0,
    total: subtotal,
  }
}
