import './bill-print.css'

const fmt = (n) => `Rs. ${Number(n || 0).toLocaleString()}`

export default function BillPrint({ bill, payments = [] }) {
  if (!bill) return null

  const customer = bill.customer
  const customerName = customer
    ? `${customer.firstName || ''} ${customer.lastName || ''}`.trim()
    : '—'

  return (
    <div id="bill-print-root" className="bill-print-root">
      <div className="bill-print-header">
        <div>
          <h1>Dreams Creations</h1>
          <p>Factory &amp; Sales Invoice</p>
        </div>
        <div className="bill-print-meta">
          <div><strong>Bill #</strong> {bill.billNumber}</div>
          <div><strong>Date</strong> {bill.billDate ? new Date(bill.billDate).toLocaleString() : new Date().toLocaleString()}</div>
          <div><strong>Status</strong> {bill.status?.toUpperCase()}</div>
        </div>
      </div>

      <div className="bill-print-customer">
        <strong>Bill To:</strong> {customerName}
        {customer?.phone && <span> &nbsp;|&nbsp; {customer.phone}</span>}
        {customer?.city && <span> &nbsp;|&nbsp; {customer.city}</span>}
      </div>

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

      <div className="bill-print-totals">
        <div><span>Subtotal</span><span>{fmt(bill.totalAmount)}</span></div>
        <div><span>Discount</span><span>{fmt(bill.discount)}</span></div>
        <div className="bill-print-final"><span>Final Amount</span><span>{fmt(bill.finalAmount)}</span></div>
      </div>

      {payments.length > 0 && (
        <>
          <h3 className="bill-print-section">Payments Received</h3>
          <table className="bill-print-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Method</th>
                <th>Reference</th>
                <th>Amount</th>
              </tr>
            </thead>
            <tbody>
              {payments.map(p => (
                <tr key={p.paymentId}>
                  <td>{p.paymentDate ? new Date(p.paymentDate).toLocaleDateString() : '—'}</td>
                  <td>{p.paymentMethod?.methodName || '—'}</td>
                  <td>{p.referenceNo || '—'}</td>
                  <td>{fmt(p.amount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      <p className="bill-print-footer">Thank you for your business — Dreams Creations</p>
    </div>
  )
}

export function printBillDocument() {
  window.print()
}
