# Beralu — Context-Aware Note-Taking App

## Current Project Status
*Note: The project is currently experiencing a critical build failure (AGP 9.0.1 / Hilt plugin incompatibility). The team is investigating build configuration workarounds.*

## Overview
...

Beralu is a **context-aware personal note-taking Android app** that surfaces your notes based on what app or screen you are currently using. It sits as a discreet **floating bubble (chat-head)** on the screen and expands into a panel showing notes relevant to the current context when tapped. It also has a **full standalone app** for browsing and managing all notes.

---

## Core Concept

> "The right note, at the right time, in the right context."

When you're using any app — say, a meeting tool, a browser, or a design tool — Beralu remembers notes you've associated with that app or screen and shows them to you without switching away. Notes are manually tagged to a context by the user, and surfaced automatically when that context is active.

---

## Platform

- **Android native** (Kotlin)
- **Min SDK**: TBD (target API 34+)
- **Architecture**: MVVM + Clean Architecture

---

## Authentication & Sync

- **Optional** — app works fully offline without an account
- Local-first storage using **Room (SQLite)**
- Optional account sync may be added in a future release (not v1)

---

## Data Model (Hierarchy)

```
Context (App)
  └── Sub-context (Screen / URL / Tag)
        └── Notes
```

- **Context**: Identified by app package name (e.g., `com.google.android.youtube`) or a user-defined label
- **Sub-context**: A named screen, URL pattern, or keyword within an app (user-defined in v1)
- **Note**: The actual note content, linked to one context + optional sub-context

Notes are **strictly bound** to a context — they show only when that context is active.

---

## Note Types (v1)

- Plain text notes
- Rich text (bold, italic, bullet lists, headings)

---

## Floating Bubble (Chat-Head) Behaviour

- Always-visible floating bubble using `SYSTEM_ALERT_WINDOW` permission
- **Tap bubble** → expands a panel showing notes for the currently tagged context
- **Long-press bubble** → opens the full standalone Beralu app
- **Tap '+' in panel** → quick-create a note and tag it to the current context
- **Drag bubble** → reposition it on screen
- **Tap outside panel** → collapses back to bubble

The bubble passively shows the count of notes available for the active context (badge on bubble icon).

---

## Context Detection (v1 — Manual)

In v1, context detection is **manual**:
- The user taps the bubble, opens the panel, and selects or creates a context
- Future versions will auto-detect the foreground app using **Accessibility Service** or **UsageStats API**

Permissions used in v1:
- `SYSTEM_ALERT_WINDOW` — for the floating bubble overlay
- Future: `BIND_ACCESSIBILITY_SERVICE`, `PACKAGE_USAGE_STATS`

---

## Standalone App Screens

1. **Home / All Notes** — flat list of all notes, grouped by context
2. **Contexts Manager** — list of all user-defined contexts and sub-contexts; CRUD operations
3. **Note Editor** — rich text editor for creating/editing notes
4. **Note Detail** — read-only view with edit button
5. **Settings** — enable/disable bubble, manage permissions, theme preferences

---

## Design Language

- **Dark mode first** (no light mode in v1)
- **Glassmorphism** aesthetic — frosted glass panels, subtle blur effects
- **Premium feel** — smooth animations, micro-interactions
- Material 3 components adapted to dark glassmorphism style
- Typography: Modern sans-serif (e.g., Google Fonts — Inter or Outfit)
- Accent color: TBD (vibrant single accent, e.g., electric indigo or cyan)

---

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| Database | Room (SQLite) |
| Floating Overlay | `SYSTEM_ALERT_WINDOW` + `WindowManager` |
| Rich Text Editor | Compose-based (Markwon or custom Compose spans) |
| DI | Hilt |
| Navigation | Jetpack Navigation Compose |
| State Management | StateFlow + ViewModel |

---

## MVP Scope (v1)

**In Scope:**
- [x] Floating bubble (always-on overlay)
- [x] Expand bubble → panel with context-filtered notes
- [x] Quick-create note from bubble panel
- [x] Standalone app: Home, Context Manager, Note Editor, Settings
- [x] Manual context tagging (user assigns context to each note)
- [x] Plain text + rich text notes (bold, italic, bullets, headings)
- [x] Local-only storage (Room DB)
- [x] Dark mode glassmorphism UI
- [x] Drag-and-drop reposition of bubble

**Out of Scope (v1):**
- Auto-detection of foreground app/screen
- Cloud sync / account sign-in
- Media attachments (images, audio)
- Export / sharing
- Light mode
- Localization

---

## Permissions Required

| Permission | Purpose |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw the floating bubble over other apps |

Future (v2+):
- `BIND_ACCESSIBILITY_SERVICE` — to detect active screen/UI context
- `PACKAGE_USAGE_STATS` — to detect which app is in foreground

---

## Open Items / Future Considerations

- **Accent color** — final decision pending (suggest: Electric Indigo `#6C63FF` or Cyan `#00D4FF`)
- **Bubble icon** — custom icon or letter-based avatar
- **Min SDK** — recommendation: API 26 (Android 8.0) for `SYSTEM_ALERT_WINDOW` with `TYPE_APPLICATION_OVERLAY`
- **Auto-detection (v2)** — Accessibility Service approach + Accessibility Service permission justification for Play Store
- **Cloud sync (v2)** — Firebase Firestore + Firebase Auth

---

*Last updated: 2026-06-14*
