import './report-print.css'

const fmt = (n) => `Rs. ${Number(n || 0).toLocaleString()}`

function customerName(c) {
  return `${c?.firstName || ''} ${c?.lastName || ''}`.trim() || '—'
}

export default function ReportPrint({ report }) {
  if (!report) return null

  const { title, subtitle, columns, rows, summary } = report

  return (
    <div id="report-print-root" className="report-print-root">
      <div className="report-print-header">
        <div>
          <h1>Dreams Creations</h1>
          <p>{title}</p>
          {subtitle && <p className="report-print-sub">{subtitle}</p>}
        </div>
        <div className="report-print-meta">
          <div><strong>Generated</strong> {new Date().toLocaleString()}</div>
        </div>
      </div>

      {summary?.length > 0 && (
        <div className="report-print-summary">
          {summary.map((item, i) => (
            <div key={i}><span>{item.label}</span><strong>{item.value}</strong></div>
          ))}
        </div>
      )}

      <table className="report-print-table">
        <thead>
          <tr>
            {columns.map(col => <th key={col.key}>{col.title}</th>)}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, i) => (
            <tr key={row.id ?? i}>
              {columns.map(col => (
                <td key={col.key}>{col.render ? col.render(row) : row[col.key]}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>

      <p className="report-print-footer">
        {rows.length} record(s) — Dreams Creations Management System
      </p>
    </div>
  )
}

export function printReportDocument() {
  window.print()
}

export { fmt, customerName }
