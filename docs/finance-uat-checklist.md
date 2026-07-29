# Finance module — UAT checklist

**Branch:** `feature/finance-v2`  
**Tester:** _______________  
**Environment:** _______________  
**Date started:** _______________  
**Date signed off:** _______________

Mark each item **Pass / Fail / N/A** and note issues in the Issues log at the bottom.

---

## 0. Prerequisites

| # | Check | Pass | Notes |
|---|--------|------|-------|
| 0.1 | Backend runs from `~\dreams-creations\backend` without startup errors | ☐ | |
| 0.2 | Frontend at http://localhost:3000, finance enabled | ☐ | |
| 0.3 | SQL run: `add-finance-module.sql` | ☐ | |
| 0.4 | SQL run: `add-finance-payables.sql` | ☐ | |
| 0.5 | SQL run: `add-finance-bank.sql` | ☐ | |
| 0.6 | `modules.finance.enabled=true` in `application.properties` | ☐ | |
| 0.7 | `modules.finance.auto-post-ar=true` | ☐ | |
| 0.8 | `modules.finance.auto-post-inventory=true` | ☐ | |
| 0.9 | Login as **Admin** or **Manager** | ☐ | |
| 0.10 | Finance Overview shows **Phase F6** and auto-post tags | ☐ | |

---

## 1. Chart of Accounts (F1)

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 1.1 | Open Finance → Chart of Accounts | Accounts 1000, 1100, 1200, 2000, 4000, 5000 visible | ☐ | |
| 1.2 | Create custom expense account (e.g. 5300 Utilities) | Saves successfully | ☐ | |
| 1.3 | Try delete system account 1000 | Blocked or error | ☐ | |

---

## 2. Manual journal (F1)

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 2.1 | Post opening entry: Dr Cash 1000 / Cr Equity 3000 (e.g. 50,000) | Entry posted, balanced | ☐ | |
| 2.2 | Try unbalanced entry | Rejected with clear error | ☐ | |
| 2.3 | View entry detail | Debit/credit lines correct | ☐ | |

---

## 3. AR auto-post (F2)

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 3.1 | Operations → create **new bill** (unpaid) | Bill saved | ☐ | |
| 3.2 | Finance → Journals | New entry: Dr AR / Cr Revenue (or discount split) | ☐ | |
| 3.3 | No duplicate journal on same bill | Only one AR entry per bill | ☐ | |
| 3.4 | Record **partial payment** on bill | Payment saved | ☐ | |
| 3.5 | Finance → Journals | Dr Cash / Cr AR for payment amount | ☐ | |
| 3.6 | Reports → AR Aging | Customer line shows unpaid balance | ☐ | |
| 3.7 | AR reconciliation alert | Ledger AR vs operational totals noted | ☐ | |
| 3.8 | Void/cancel bill (if supported) | Reversal journal or documented behavior | ☐ | N/A if not used |

---

## 4. Inventory & COGS auto-post (F3)

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 4.1 | Design has **production cost** set | Cost > 0 on design used in test | ☐ | |
| 4.2 | Create bill with inventory line items | Bill saved | ☐ | |
| 4.3 | Finance → Journals | COGS entry: Dr 5000 / Cr 1200 (if auto-post inventory on) | ☐ | |
| 4.4 | Complete production → packing (adds stock) | Inventory qty increases | ☐ | |
| 4.5 | Finance → Journals | Inventory receipt journal (if applicable) | ☐ | |
| 4.6 | Reports → Inventory Valuation | Operational stock value vs ledger 1200 | ☐ | |

---

## 5. Financial statements (F4)

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 5.1 | Reports → Trial Balance | Total debit = total credit (Balanced tag) | ☐ | |
| 5.2 | Reports → General Ledger on Cash (1000) | Opening + movements match expectations | ☐ | |
| 5.3 | Reports → Profit & Loss (month to date) | Revenue and expenses populate | ☐ | |
| 5.4 | Reports → Balance Sheet (today) | Assets ≈ Liabilities + Equity (or known difference explained) | ☐ | |

---

## 6. Payables (F5)

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 6.1 | Payables → New vendor | Vendor created | ☐ | |
| 6.2 | New payable (invoice + expense account) | Payable listed as unpaid | ☐ | |
| 6.3 | Finance → Journals | Dr Expense / Cr AP (2000) | ☐ | |
| 6.4 | Record full payment on payable | Status = paid | ☐ | |
| 6.5 | Finance → Journals | Dr AP / Cr Cash | ☐ | |
| 6.6 | Payables → AP Aging | Vendor balance; reconciliation message | ☐ | |

---

## 7. Bank reconciliation (F6)

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 7.1 | Bank → New account linked to GL 1000 | Account created | ☐ | |
| 7.2 | Add statement lines (+ deposit, − withdrawal) | Lines listed | ☐ | |
| 7.3 | Run reconciliation (as-of today) | Statement vs ledger balances shown | ☐ | |
| 7.4 | Mark line Reconciled | Status updates | ☐ | |

---

## 8. Portal & tutorials

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 8.1 | Operations → Help → tour / guide | Drawer opens, links work | ☐ | |
| 8.2 | Finance → Help → Watch tour again | Nav tour runs | ☐ | |
| 8.3 | `/guide` page | Operations + Finance tabs readable | ☐ | |
| 8.4 | Back to Operations from Finance | Returns to dashboard | ☐ | |

---

## 9. Regression (operations)

| # | Test | Expected | Pass | Notes |
|---|------|----------|------|-------|
| 9.1 | Create production batch + advance stage | No errors | ☐ | |
| 9.2 | Create quotation → convert to bill | Works as before finance | ☐ | |
| 9.3 | Customer portal login + view bills | Unaffected | ☐ | |
| 9.4 | Supervisor assignments | Unaffected | ☐ | |

---

## Sign-off criteria

All sections **0–8** must pass (section 9 spot-check). No **Critical** or **High** open issues.

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Tester | | | |
| Business owner | | | |
| Dev sign-off | | | |

---

## Issues log

| ID | Section | Severity (Critical/High/Medium/Low) | Description | Status |
|----|---------|-------------------------------------|-------------|--------|
| UAT-001 | | | | Open |
| UAT-002 | | | | |

---

## After UAT pass

1. Fix issues on `feature/finance-v2` (or `main` after merge).
2. Create PR: `feature/finance-v2` → `main`.
3. Run SQL on production DB.
4. Deploy backend + frontend with finance flags enabled.
5. Archive this checklist with Pass marks and sign-off dates.

---

## Quick test data (suggested)

| Item | Suggested value |
|------|-----------------|
| Opening cash journal | Dr 1000: 100,000 / Cr 3000: 100,000 |
| Test customer bill | 25,000 final amount |
| Test payment | 10,000 partial |
| Test vendor payable | 15,000 to Production Expenses 5100 |
| Bank opening | Match ledger cash after opening journal |
