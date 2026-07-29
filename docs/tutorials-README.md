# Dreams Creations — Tutorial documentation

In-app help, written guides, and video script for **Operations** and **Finance** modules.

## In the application

| Feature | How to access |
|---------|----------------|
| **Interactive tour** | First login welcome, or **Help → Watch tour again** |
| **Detailed help drawer** | **Help** button in the header (operations & finance) |
| **Full written guide** | **Help → Open full written guide**, user menu **Tutorials & guide**, or `/guide` |
| **Reset welcome** | **Help → Reset & show welcome** |

## Repository docs

| File | Purpose |
|------|---------|
| [operations-tutorial.md](./operations-tutorial.md) | Full operations manual (Admin, Manager, Supervisor, Customer) |
| [finance-tutorial.md](./finance-tutorial.md) | Full finance / accounting manual |
| [finance-uat-checklist.md](./finance-uat-checklist.md) | **UAT sign-off checklist** (use before merge to main) |
| [shop-roadmap.md](./shop-roadmap.md) | **Shop module roadmap** (S1–S6 phases on `feature/shop-v1`) |
| [finance-video-script.md](./finance-video-script.md) | Screencast script (~28 min) for recording a finance walkthrough |
| [tutorial-slides-outline.md](./tutorial-slides-outline.md) | Slide deck outline for classroom / PDF export |

## Recording a video

1. Read `finance-video-script.md` (and operations sections as needed).
2. Run backend + frontend with finance enabled and SQL migrations applied.
3. Record at 1920×1080 using OBS, Loom, or Windows Game Bar.
4. Follow chapter markers in the script.

## For developers

Tutorial text lives in `frontend/src/content/tutorialContent.js` (single source for in-app Help and `/guide`).
