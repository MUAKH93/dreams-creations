/**
 * Export rows to CSV and trigger browser download.
 * Adds UTF-8 BOM so Excel opens Urdu/special chars correctly.
 */
export function downloadCsv(filename, columns, rows) {
  const escape = (val) => {
    if (val == null) return ''
    const s = String(val)
    if (/[",\n\r]/.test(s)) return `"${s.replace(/"/g, '""')}"`
    return s
  }

  const header = columns.map(c => escape(c.title)).join(',')
  const body = rows.map(row =>
    columns.map(c => escape(c.value(row))).join(',')
  ).join('\n')

  const csv = `\uFEFF${header}\n${body}`
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

export function todayStamp() {
  return new Date().toISOString().slice(0, 10)
}
