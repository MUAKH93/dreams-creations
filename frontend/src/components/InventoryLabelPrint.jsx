import { useEffect, useRef } from 'react'
import JsBarcode from 'jsbarcode'
import './label-print.css'

/** Barcode payload — suit ID is unique and scannable */
export function barcodeValue(item) {
  return `DC${String(item.suitId).padStart(8, '0')}`
}

function LabelCard({ item, price }) {
  const svgRef = useRef(null)
  const code = barcodeValue(item)

  useEffect(() => {
    if (svgRef.current) {
      try {
        JsBarcode(svgRef.current, code, {
          format: 'CODE128',
          width: 1.4,
          height: 48,
          displayValue: true,
          fontSize: 12,
          margin: 4,
        })
      } catch {
        // invalid code — skip
      }
    }
  }, [code])

  return (
    <div className="inventory-label-card">
      <div className="inventory-label-brand">Dreams Creations</div>
      <div className="inventory-label-title">{item.designName}</div>
      <div className="inventory-label-code">{item.designCode}</div>
      <div className="inventory-label-sku">
        {item.sizeValue} / {item.color}
      </div>
      {price > 0 && (
        <div className="inventory-label-price">Rs. {price.toLocaleString()}</div>
      )}
      <svg ref={svgRef} />
    </div>
  )
}

export default function InventoryLabelPrint({ items = [], prices = {} }) {
  if (!items.length) return null

  return (
    <div id="label-print-root" className="label-print-root">
      <div className="inventory-label-grid">
        {items.map(item => (
          <LabelCard
            key={item.suitId}
            item={item}
            price={prices[item.suitId] || 0}
          />
        ))}
      </div>
    </div>
  )
}

export function printLabelDocument() {
  window.print()
}
