# Finance module — detailed tutorial

Dreams Creations **Finance Portal** — accounting separate from factory operations.

---

## Prerequisites

1. MySQL scripts (in order):
   - `backend/src/main/resources/db/add-finance-module.sql`
   - `add-finance-payables.sql`
   - `add-finance-bank.sql`
2. Backend `application.properties`:
   ```properties
   modules.finance.enabled=true
   modules.finance.auto-post-ar=false
   modules.finance.auto-post-inventory=false
   modules.finance.auto-post-ap=true
   ```
3. Frontend `.env`: `VITE_FINANCE_MODULE_ENABLED=true`
4. Login as **Admin** or **Manager**.

---

## Two portals

| Operations | Finance |
|------------|---------|
| Bills, production, inventory | Chart of accounts, journals |
| Customer payments (operational) | AP, bank reconciliation |
| Factory reports | P&L, balance sheet |

Access: **Open Finance Portal** (operations sidebar) or `/finance`.

---

## 1. Chart of Accounts

**Path:** Finance → Chart of Accounts

### Pre-seeded accounts

| Code | Name | Type |
|------|------|------|
| 1000 | Cash & Bank | Asset |
| 1100 | Accounts Receivable | Asset |
| 1200 | Inventory | Asset |
| 2000 | Accounts Payable | Liability |
| 3000 | Owner's Equity | Equity |
| 4000 | Sales Revenue | Income |
| 5000 | Cost of Goods Sold | Expense |

Add custom accounts as needed. System accounts cannot be deleted.

---

## 2. Journal Entries

**Path:** Finance → Journal Entries

**Rule:** Total debits = total credits on every entry.

### Manual example (opening balance)

| Account | Debit | Credit |
|---------|-------|--------|
| 1000 Cash | 50,000 | |
| 3000 Owner's Equity | | 50,000 |

### Auto-posting (when enabled)

| Event | Debit | Credit |
|-------|-------|--------|
| Customer bill | 1100 AR | 4000 Revenue |
| Customer payment | 1000 Cash | 1100 AR |
| Vendor invoice | Expense | 2000 AP |
| Vendor payment | 2000 AP | 1000 Cash |

---

## 3. Accounts Payable

**Path:** Finance → Payables

### Workflow

1. **Vendors tab** — create supplier.
2. **Open Payables** — New Payable: invoice #, dates, amount, expense account.
3. Journal posts automatically: **Dr Expense / Cr AP**.
4. **Pay** — records payment: **Dr AP / Cr Cash**.
5. **AP Aging** — buckets by age; reconciles to ledger AP.

---

## 4. Bank Reconciliation

**Path:** Finance → Bank Reconciliation

1. **Bank Accounts** — link to GL 1000, set opening balance.
2. **Statement Lines** — enter bank activity (+ deposit, − withdrawal).
3. **Reconciliation** — compare statement vs ledger; mark lines **Reconciled**.

---

## 5. Reports

**Path:** Finance → Reports

| Report | Use |
|--------|-----|
| Trial Balance | Verify debits = credits |
| General Ledger | Account activity + running balance |
| AR Aging | Unpaid customer bills by age |
| Inventory Valuation | Stock at cost vs ledger 1200 |
| Profit & Loss | Income − expenses for a period |
| Balance Sheet | Assets = liabilities + equity at a date |

---

## 6. Operations ↔ Finance flow

```
Customer bill (Operations)  →  AR + Revenue journal (Finance, if auto-post AR)
Customer payment          →  Cash + AR journal
Production / COGS         →  Inventory journals (if auto-post inventory)
Vendor payable            →  Expense + AP (Finance Payables)
```

---

## Replay the tutorial in-app

1. **Help** in Finance header.
2. **Watch tour again** — interactive sidebar tour.
3. **Open full written guide** — `/guide?tab=finance`.
4. **Reset & show welcome** — first-time welcome drawer again.

---

## Video walkthrough

See [finance-video-script.md](./finance-video-script.md) for a full screencast script with timestamps.

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| Backend won't start: missing table `finance_payable` | Run `add-finance-payables.sql` |
| Missing `finance_bank_account` | Run `add-finance-bank.sql` |
| Finance menu missing | Enable module in backend + frontend env |
| AR difference in reports | Review auto-posted journals vs operational balances |
