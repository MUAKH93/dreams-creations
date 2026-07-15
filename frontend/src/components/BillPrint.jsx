import './bill-print.css'

const fmt = (n) => `Rs. ${Number(n || 0).toLocaleString()}`

export default function BillPrint({ bill }) {
  if (!bill) return null

  const customer = bill.customer
  const customerName = customer
    ? `${customer.firstName || ''} ${customer.lastName || ''}`.trim()
    : '—'

  const previousBalance = Number(bill.previousBalance || 0)
  const grandTotal = Number(bill.grandTotal || bill.finalAmount || 0)

  return (
    <div id="bill-print-root" className="bill-print-root">
      <div className="bill-print-top">
        <div className="bill-print-logo-slot">
          <div className="bill-print-logo-placeholder">Company Logo</div>
        </div>
        <div className="bill-print-company">
          <h1>Dreams Creations</h1>
          <p>Factory &amp; Sales Invoice</p>
        </div>
        <div className="bill-print-meta">
          <div><strong>Bill #</strong> {bill.billNumber}</div>
          <div><strong>Date</strong> {bill.billDate ? new Date(bill.billDate).toLocaleDateString() : new Date().toLocaleDateString()}</div>
          <div><strong>Status</strong> {bill.status?.toUpperCase()}</div>
        </div>
      </div>

      <div className="bill-print-customer">
        <div><strong>Bill To:</strong> {customerName}</div>
        {customer?.phone && <div><strong>Phone:</strong> {customer.phone}</div>}
        {customer?.city && <div><strong>City:</strong> {customer.city}</div>}
      </div>

      <div className="bill-print-body">
        <table className="bill-print-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Product</th>
              <th>Size</th>
              <th>Color</th>
              <th>Qty</th>
              <th>Unit Price</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            {(bill.items || []).map((item, i) => {
              const suit = item.product?.suit
              const design = suit?.design
              return (
                <tr key={item.billItemId || i}>
                  <td>{i + 1}</td>
                  <td>{design?.name || design?.designCode || `Product ${item.product?.productId}`}</td>
                  <td>{suit?.size?.sizeValue || '—'}</td>
                  <td>{suit?.color || '—'}</td>
                  <td>{item.quantity}</td>
                  <td>{fmt(item.unitPrice)}</td>
                  <td>{fmt(item.totalPrice)}</td>
                </tr>
              )
            })}
          </tbody>
        </table>

        <div className="bill-print-spacer" aria-hidden="true" />

        <div className="bill-print-totals-wrap">
          <div className="bill-print-totals">
            <div><span>Subtotal</span><span>{fmt(bill.totalAmount)}</span></div>
            <div><span>Discount</span><span>{fmt(bill.discount)}</span></div>
            <div><span>Bill Total</span><span>{fmt(bill.finalAmount)}</span></div>
            {previousBalance > 0 && (
              <div><span>Previous Balance</span><span>{fmt(previousBalance)}</span></div>
            )}
            <div className="bill-print-grand"><span>Grand Total Due</span><span>{fmt(grandTotal)}</span></div>
          </div>
        </div>
      </div>

      <p className="bill-print-footer">Thank you for your business — Dreams Creations</p>
    </div>
  )
}

export function printBillDocument() {
  window.print()
}
