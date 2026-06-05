# solivagant & kmp-viewmodel — Documentation Index

This directory contains architectural retrospectives, design pattern documentation, migration guides,
and maintenance recommendations for the **solivagant** and **kmp-viewmodel** libraries.

These documents were written as of mid-2025, when JetBrains Compose Multiplatform, AndroidX Lifecycle
multiplatform, and Navigation 3 had matured enough to cover most of the original use-cases these
libraries were created to address.

---

## Contents

### `/legacy` — Why these libraries existed and what they contributed

| File | Description |
|------|-------------|
| [why-these-libraries-existed.md](legacy/why-these-libraries-existed.md) | Historical context: the 2022–2023 KMP ecosystem gap these libraries filled |
| [solivagant-retrospective.md](legacy/solivagant-retrospective.md) | Full audit of the solivagant navigation library |
| [kmp-viewmodel-retrospective.md](legacy/kmp-viewmodel-retrospective.md) | Full audit of the kmp-viewmodel library |

### `/architecture` — Design patterns worth preserving

| File | Description |
|------|-------------|
| [navigation-patterns.md](architecture/navigation-patterns.md) | Type-safe routing, NavRoot/NavRoute split, multi-backstack ownership, overlay destinations |
| [viewmodel-patterns.md](architecture/viewmodel-patterns.md) | Multiplatform ViewModel lifecycle, `viewModelScope`, `Closeable` chain, Swift wrappers |
| [state-preservation.md](architecture/state-preservation.md) | `SavedStateHandle` multiplatform, `SaveableStateHolder`, `SavedStateSupport` design |

### `/migration` — Guidance for users moving to official APIs

| File | Description |
|------|-------------|
| [modern-compose-multiplatform.md](migration/modern-compose-multiplatform.md) | Mapping solivagant + kmp-viewmodel concepts to modern official APIs |

### `/maintenance` — Repository status decisions

| File | Description |
|------|-------------|
| [repository-status.md](maintenance/repository-status.md) | Recommendation: keep, deprecate, archive, or reframe each repository |

### `/ideas` — Extracted patterns

| File | Description |
|------|-------------|
| [extracted-patterns.md](ideas/extracted-patterns.md) | Catalogue of valuable ideas, patterns, and techniques from both libraries |

---

## Quick summary

| Library | Current status recommendation | Maven artifacts |
|---------|------------------------------|-----------------|
| **solivagant** | Minimal maintenance mode → document deprecation path | Keep publishing; add deprecation notice after Navigation 3 is stable |
| **kmp-viewmodel** | Minimal maintenance mode → document deprecation path | Keep publishing; the `parcelable` and `savedstate` modules remain useful for Android-targeting KMP projects |

See [maintenance/repository-status.md](maintenance/repository-status.md) for the detailed
engineering trade-off analysis.
