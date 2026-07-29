# Operations module — detailed tutorial

Dreams Creations ERP operations portal: production, sales, inventory, and customer management.

---

## Who uses what

| Role | Portal | Main tasks |
|------|--------|------------|
| **Admin** | Management + Setup | Everything Manager can do, plus staff and factory setup |
| **Manager** | Management | Production, sales, inventory, reports |
| **Supervisor** | Supervisor | Complete assigned production stages |
| **Customer** | Customer | Browse designs, view quotes and bills |

---

## 1. Dashboard

After login, the dashboard shows role-specific summaries: production status, sales KPIs, or your assignments/quotes.

**Tip:** Use the sidebar for navigation; on mobile, open the menu with the ☰ icon.

---

## 2. Production Batches (Admin / Manager)

**Path:** Production Batches

1. **Create batch** — select design, quantity, and start date.
2. **Stages:** Cutting → Stitching → Press → Packing.
3. Each stage can be assigned to a supervisor.
4. When **Packing** is completed, finished quantity is added to **Inventory**.

**Workflow tip:** Track batch status before promising delivery dates to customers.

---

## 3. Dispatch Management (Admin / Manager)

**Path:** Dispatch Management

- View orders ready to ship.
- Record dispatch with date and details.
- Ensure inventory is available before dispatching.

---

## 4. Inventory (Admin / Manager)

**Path:** Inventory

- Stock by design, size, and color.
- Increases when production completes; decreases when bills sell stock.
- Set **production cost** on designs for accurate costing and finance COGS.

---

## 5. Designs Catalog (All roles)

**Path:** Designs Catalog

- Management: add/edit designs, images, sizes, colors, pricing, **production cost**.
- Customers: browse and request quotes.
- Supervisors: reference specs while working.

---

## 6. Customers (Admin / Manager)

**Path:** Customers

- Maintain customer records and contact details.
- Customer **balance** is operational; **AR aging** in Finance tracks unpaid bills.

---

## 7. Quotations (Admin / Manager)

**Path:** Quotations

1. Create quote with catalog line items.
2. Send to customer (customer sees under **My Quotes**).
3. Convert approved quote to a **Bill**.

---

## 8. Bills & Payments (Admin / Manager)

**Path:** Bills & Payments

1. **Create bill** — customer, line items, discount.
2. **Record payment** — amount, method, reference.
3. Print bill for customer.

**Finance integration:** When `modules.finance.auto-post-ar=true`, bills post **Dr AR / Cr Revenue** and payments post **Dr Cash / Cr AR** automatically.

---

## 9. Operations Reports & Analytics (Admin / Manager)

- **Reports** — factory and sales operational reports.
- **Analytics** — trends and charts.

These are **not** the same as Finance P&L or Balance Sheet — use the **Finance Portal** for accounting reports.

---

## 10. Activity Log & Alerts (Admin / Manager)

- **Activity Log** — audit trail of key actions.
- **Alerts** — low stock, overdue items, system notices.

---

## 11. Admin-only: Staff & Factory Setup

- **Staff & Supervisors** — user accounts and roles.
- **Factory Setup** — sizes, payment methods, packing defaults.

---

## 12. Supervisor: My Assignments

**Path:** My Assignments

1. Open assigned batch stage.
2. Record quantity completed.
3. Submit — managers see updated batch progress.

---

## 13. Customer portal

- **Designs Catalog** — browse products.
- **My Quotes** — quotations from the factory.
- **My Bills** — invoices and amounts due.

---

## 14. Finance Portal (Admin / Manager)

Green **Open Finance Portal** button at the bottom of the operations sidebar.

Separate workspace for accounting — see [finance-tutorial.md](./finance-tutorial.md).

---

## Replay the tutorial in-app

1. Click **Help** in the operations header.
2. Choose **Watch tour again** for the interactive walkthrough.
3. Choose **Open full written guide** for `/guide?tab=operations`.
4. User menu → **Tutorials & guide**.

---

## Common questions

**Q: Bill created but no journal in Finance?**  
A: Enable `modules.finance.enabled=true` and `auto-post-ar=true` in backend config.

**Q: Where is profit & loss?**  
A: Finance Portal → Reports → Profit & Loss (not Operations Reports).

**Q: Customer cannot see a quote?**  
A: Ensure the quote is linked to their customer account and they log in as Customer role.
