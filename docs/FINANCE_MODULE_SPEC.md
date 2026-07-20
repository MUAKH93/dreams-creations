# Finance Module — Specification (v2.0)

**Branch:** `feature/finance-v2`  
**Status:** Scaffold / Phase F0  
**Target merge:** `main` after UAT (Operations v1.0 remains stable until then)

---

## 1. Purpose

Add **double-entry accounting** on top of existing operations (bills, payments, inventory) without breaking the live factory system. Finance runs behind a feature flag until merge.

### Outcomes

- Chart of accounts
- Manual and automated journal entries
- General ledger & trial balance
- Balance sheet & profit & loss
- AR aging (linked to existing customers/bills)
- Optional: AP, bank reconciliation (later phases)

### Not in scope (v2.0)

- Statutory audit certification
- Multi-currency
- Payroll
- Tax filing automation

---

## 2. Integration with existing system

| Existing feature | Finance integration |
|------------------|---------------------|
| Bill confirmed | Auto-post: Dr AR, Cr Sales Revenue |
| Payment received | Auto-post: Dr Cash/Bank, Cr AR |
| Inventory from production | Optional: Dr Inventory, Cr WIP clearing |
| Bill stock deduction | Dr COGS, Cr Inventory |
| `customer_balance` | Reconcile with AR ledger; operational balance remains for sales UI |

Auto-posting controlled by:

```properties
modules.finance.enabled=true
modules.finance.auto-post-ar=false   # enable in Phase F2
modules.finance.auto-post-inventory=false
```

---

## 3. Chart of accounts (default seed)

| Code | Name | Type | Normal balance |
|------|------|------|----------------|
| 1000 | Cash & Bank | ASSET | Debit |
| 1100 | Accounts Receivable | ASSET | Debit |
| 1200 | Inventory — Finished Goods | ASSET | Debit |
| 1300 | Work in Progress | ASSET | Debit |
| 2000 | Accounts Payable | LIABILITY | Credit |
| 2100 | Customer Deposits | LIABILITY | Credit |
| 3000 | Owner's Equity | EQUITY | Credit |
| 3100 | Retained Earnings | EQUITY | Credit |
| 4000 | Sales Revenue | INCOME | Credit |
| 4100 | Sales Discounts | INCOME | Debit (contra) |
| 5000 | Cost of Goods Sold | EXPENSE | Debit |
| 5100 | Production Expenses | EXPENSE | Debit |
| 5200 | Operating Expenses | EXPENSE | Debit |

*Final list to be signed off in Phase F0 with factory owner / accountant.*

---

## 4. Database tables (Phase F1+)

See `backend/src/main/resources/db/add-finance-module.sql`.

| Table | Purpose |
|-------|---------|
| `finance_account` | Chart of accounts |
| `finance_fiscal_period` | Month/year periods, open/closed |
| `finance_journal_entry` | Header (date, reference, memo, source) |
| `finance_journal_line` | Debit/credit lines per account |
| `finance_posting_link` | Links journal to bill_id, payment_id, etc. |

Phase F5+: `finance_vendor`, `finance_expense_voucher`, `finance_bank_account`, `finance_bank_transaction`.

---

## 5. API namespace

All finance APIs under `/api/finance/**` (Admin & Manager only).

| Endpoint | Phase | Description |
|----------|-------|-------------|
| `GET /api/finance/status` | Scaffold | Module version & enabled phases |
| `GET /api/finance/accounts` | F1 | List chart of accounts |
| `POST /api/finance/accounts` | F1 | Create account |
| `GET /api/finance/journals` | F1 | List journal entries |
| `POST /api/finance/journals` | F1 | Create manual journal |
| `GET /api/finance/reports/trial-balance` | F1 | Trial balance |
| `GET /api/finance/reports/general-ledger` | F1 | GL by account |
| `GET /api/finance/reports/ar-aging` | F2 | AR aging |
| `GET /api/finance/reports/profit-loss` | F4 | P&L |
| `GET /api/finance/reports/balance-sheet` | F4 | Balance sheet |

`GET /api/modules` — always available; returns `{ finance: { enabled: true/false } }`.

---

## 6. Frontend routes

| Route | Phase | Page |
|-------|-------|------|
| `/finance` | Scaffold | Finance home / roadmap |
| `/finance/accounts` | F1 | Chart of accounts |
| `/finance/journals` | F1 | Journal entries |
| `/finance/reports` | F1+ | Reports hub |

Menu items visible only when **both**:

- `modules.finance.enabled=true` (backend)
- `VITE_FINANCE_MODULE_ENABLED=true` (frontend `.env`)

---

## 7. Phased delivery & timeline

### F0 — Discovery (Week 1–2)

- [ ] Sign chart of accounts with accountant
- [ ] Define fiscal year start
- [ ] List auto-post events and approval rules
- [ ] Sign scope document

**Exit:** Signed `FINANCE_MODULE_SPEC.md` appendix (accounts + rules)

---

### F1 — Core ledger (Week 3–5)

- [ ] Run `add-finance-module.sql` on staging DB
- [ ] JPA entities: Account, FiscalPeriod, JournalEntry, JournalLine
- [ ] Chart of accounts CRUD
- [ ] Manual journal entry (debits = credits validation)
- [ ] General ledger & trial balance reports
- [ ] UI: accounts, journals, trial balance

**Exit:** Opening balances can be posted manually

---

### F2 — Accounts receivable (Week 6–8)

- [ ] Auto-post on bill confirm & payment
- [ ] AR aging report
- [ ] Reconciliation check vs `customer_balance`
- [ ] Feature flag: `auto-post-ar=true` on staging

**Exit:** Every test bill/payment creates correct journals

---

### F3 — Inventory & COGS (Week 9–10)

- [ ] Average cost or standard cost per suit/SKU
- [ ] COGS on bill stock deduction
- [ ] Optional production → inventory journals
- [ ] Inventory valuation report

**Exit:** COGS and inventory asset accounts move correctly

---

### F4 — Financial statements (Week 11–12)

- [ ] Profit & Loss by period
- [ ] Balance sheet as-at date
- [ ] Period close / lock
- [ ] PDF/Excel export

**Exit:** Balance sheet balances; P&L matches revenue/expense accounts

---

### F5 — Payables & expenses (Week 13–14) — optional

- [ ] Vendor master
- [ ] Expense vouchers
- [ ] AP aging

---

### F6 — Bank reconciliation & UAT (Week 15–16)

- [ ] Bank register
- [ ] Reconciliation UI
- [ ] Opening balance migration tool
- [ ] Staging UAT sign-off → merge to `main`

---

## 8. Timeline summary

| Milestone | Weeks | Cumulative |
|-----------|-------|------------|
| F0 Discovery | 2 | Week 2 |
| F1 Core ledger | 3 | Week 5 |
| F2 AR | 3 | Week 8 |
| F3 Inventory/COGS | 2 | Week 10 |
| F4 Statements | 2 | Week 12 |
| F5 Payables (opt.) | 2 | Week 14 |
| F6 Bank & UAT | 2 | **Week 16** |

**MVP (F0–F4):** ~12 weeks  
**Full (F0–F6):** ~16 weeks

---

## 9. UAT checklist (before merge to main)

- [ ] Chart of accounts matches accountant's structure
- [ ] Manual journal: debits = credits enforced
- [ ] Trial balance always balances
- [ ] Test bill → AR journal correct
- [ ] Test payment → cash/AR journal correct
- [ ] P&L and balance sheet tie to trial balance
- [ ] Closed period rejects edits
- [ ] Operations v1.0 flows still work with finance **disabled**
- [ ] Operations v1.0 flows still work with finance **enabled** on staging
- [ ] DB backup + rollback tested

---

## 10. Acceptance sign-off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Factory owner | | | |
| Accountant | | | |
| Developer | | | |

---

*Dreams Creations — Finance Module v2.0 · Confidential*
