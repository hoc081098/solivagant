# Repository Status — Final Decision

Engineering trade-off analysis and **final maintenance decision** for **solivagant** and **kmp-viewmodel** as of mid-2026.

---

## Framework

For each library, the recommendation is evaluated against:

1. **Maintenance cost** — what ongoing effort is required
2. **User impact** — how many users are affected and how severely
3. **Unique value** — what the library still provides that official APIs do not
4. **Migration path** — how difficult migration is for existing users
5. **Risk** — what happens if maintenance stops

Possible statuses:
- **Keep maintained** — active development, new features, fast bug fixes
- **Minimal maintenance mode** — dependency updates, critical bugs only; no new features
- **Deprecated but not archived** — maintenance stops; documentation banner added; Maven artifacts
  remain accessible
- **Archived** — repository frozen; README updated; no further changes
- **Reframed as educational / historical** — codebase preserved as a learning reference

---

## solivagant

### Current situation

- Latest release: 0.5.0 (August 2024). Snapshot targets Kotlin 2.0.10.
- Still alpha (`@ExperimentalSolivagantApi` throughout).
- Active samples, working CI, no known compilation failures.
- Depends on kmp-viewmodel, which shares the same maintenance questions.
- Based on Freeletics Khonshu (Apache 2.0 fork).

### Unique value that official APIs do not yet provide

1. **Multi-platform navigation today.** Navigation 3 is in alpha and does not have full
   iOS/Desktop support as of mid-2025. solivagant works on Android, iOS, Desktop, Web,
   watchOS, tvOS, macOS.
2. **NavRoot/NavRoute multi-backstack model.** Clean, explicit multi-backstack for bottom
   navigation with no boilerplate.
3. **NavEventNavigator.** Testable navigation commands from ViewModels without Compose test
   infrastructure.
4. **Overlay destinations.** Dialogs and bottom sheets as first-class navigation entries with
   their own ViewModel scope.
5. **Per-entry lifecycle and ViewModel scope.** Correct lifecycle management per entry across all
   platforms.

### Maintenance cost

| Activity | Frequency | Effort |
|----------|-----------|--------|
| Kotlin version bump | Every Kotlin release (~quarterly) | Low-Medium |
| Compose Multiplatform version bump | Every CMP release (~quarterly) | Medium (ABI checks) |
| kmp-viewmodel version bump | On kmp-viewmodel releases | Low |
| AndroidX Lifecycle bump | Every AndroidX release | Low |
| Bug fixes | Irregular | Variable |
| New features | Roadmap: tests, docs, API polish | High |

Realistically: 4–8 hours per quarter for dependency tracking + CI fixes; more for significant
Compose API changes.

### Risks

- **Navigation 3 becomes stable on all platforms.** This is the primary obsolescence risk.
  Timeline is uncertain (6–18 months based on current trajectory).
- **API breaking changes in Compose Multiplatform.** CMP has introduced breaking changes in
  minor versions; each requires an update.
- **kmp-viewmodel deprecation.** If kmp-viewmodel stops being maintained, solivagant must
  either inline its dependencies or migrate to official lifecycle APIs.
- **Sparse test coverage.** The `MultiStack` / `Stack` logic has limited automated test coverage.
  A regression could go undetected.

### Existing user impact

- Users in production with solivagant get a working, tested navigation stack across all platforms.
  Deprecating now would require migrating to a less mature alternative.
- Users starting new projects should be informed of Navigation 3's trajectory so they can plan.

### Final decision: **Not maintained — repository archived for reference**

**Reasoning:**
Navigation 3 is on a clear trajectory to become the official multiplatform navigation solution.
With Navigation 3 eventually going stable and multiplatform, solivagant's unique value as a
library will disappear. Continuing maintenance would require ongoing effort for diminishing returns.

The library is preserved as a public reference. All architectural concepts, patterns, and design
decisions are documented in `docs/` and remain worth reading.

**Actions taken:**
- README updated with a `[🔴 NOT MAINTAINED]` banner and a deprecation warning.
- No further Maven releases planned after 0.5.0.
- Repository is not archived on GitHub so that users can still file issues, but no responses
  or fixes are guaranteed.
- The `docs/` directory documents everything worth keeping for future library authors.

**Patterns worth preserving (see [`docs/architecture/`](../architecture/) and [`docs/ideas/`](../ideas/)):**

| Pattern | Why it matters |
|---------|----------------|
| `NavEventNavigator` — Channel-backed nav commands from ViewModel | Testable navigation without Compose test infrastructure |
| `NavRoot`/`NavRoute` multi-backstack ownership | Clean two-level hierarchy for bottom navigation |
| `StackValidationMode` (Strict / Lenient / Warning) | Defensive programming for navigation state machines |
| `SavedStateSupport` for non-Android platforms | Bridging saved state to platforms that have no built-in support |
| Back stack as observable Compose state | Endorsed independently by Navigation 3; solivagant arrived here first |

---

## kmp-viewmodel

### Current situation

- Latest release: 0.8.0 (June 2024). Snapshot targets Kotlin 2.0.10.
- Stable API (no `@Experimental` on core modules).
- Active samples, working CI.
- Widely used as a dependency of solivagant.

### Unique value that official APIs do not yet provide

1. **`@Parcelize` / `Parcelable` in `commonMain`.** The official multiplatform lifecycle
   artifacts do not include a multiplatform `@Parcelize` mechanism. This module remains uniquely
   useful for Android-targeting KMP projects that need Parcelable in shared code.
2. **Type-safe `SavedStateHandle` keys.** `NonNullSavedStateHandleKey` / `NullableSavedStateHandleKey`
   / `SavedStateHandle.safe` — no official equivalent.
3. **Swift Flow wrappers.** Still valuable for teams on older Kotlin versions or specific Swift
   interop requirements.
4. **DI adapter modules (Koin, Koject).** Clean integration layers for the two most popular
   KMP DI libraries.

### Overlapping / superseded value

1. **Core ViewModel.** `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` supersedes this
   for new projects.
2. **Compose viewModel() composable.** `lifecycle-viewmodel-compose` supersedes `kmpViewModel()`.
3. **`LocalViewModelStoreOwner`.** Official equivalent now exists.

### Maintenance cost

| Activity | Frequency | Effort |
|----------|-----------|--------|
| Kotlin version bump | Quarterly | Low |
| AndroidX Lifecycle bump | Each release | Low-Medium (actual delegate must align) |
| Compose Multiplatform bump | Each CMP release | Low |
| Koin / Koject version bumps | Each DI release | Low |
| Parcelize plugin compatibility | Each Kotlin release | Low (but fragile) |
| Bug fixes | Irregular | Variable |

### Risks

- **Official multiplatform `@Parcelize`.** If/when Kotlin ships native multiplatform Parcelize
  support, the `viewmodel-savedstate` module's primary unique value disappears.
- **AndroidX Lifecycle API changes.** Each major AndroidX Lifecycle change requires updating
  the Android `actual` implementations.

### Existing user impact

- The core ViewModel module can be replaced with the official equivalent with minimal code changes.
  The migration is low-risk and low-effort (see migration guide).
- The savedstate module (type-safe keys, `@Parcelize`) provides unique value that users cannot
  easily get elsewhere.
- Abrupt deprecation of the savedstate module would require users to either write their own
  key abstraction or regress to raw string-based `SavedStateHandle` access.

### Final decision: **Not maintained — repository archived for reference**

**Reasoning:**
The core ViewModel module is already superseded by `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel`.
The `savedstate` module (type-safe keys, `@Parcelize` in common code) retains some unique value,
but the maintenance cost and the uncertainty around official multiplatform `@Parcelize` support
make continued maintenance unjustifiable.

**Actions taken:**
- No further Maven releases planned.
- Repository is not archived on GitHub so existing users can still view the code and history.
- The `docs/` directory documents the patterns worth keeping.

**Patterns worth preserving (see [`docs/architecture/`](../architecture/) and [`docs/ideas/`](../ideas/)):**

| Pattern | Why it matters |
|---------|----------------|
| `NonNullSavedStateHandleKey` / `NullableSavedStateHandleKey` | Type-safe SavedStateHandle access; prevents runtime key-name mistakes |
| `@Parcelize` / `Parcelable` in `commonMain` | Still no official KMP equivalent; the workaround technique is worth documenting |
| `expect`/`actual` ViewModel delegation | Canonical pattern for wrapping platform-specific APIs in KMP |
| Swift Flow wrappers | Practical interop technique regardless of which Kotlin library you use |

---

## Comparison Table

| Aspect | solivagant | kmp-viewmodel |
|--------|------------|---------------|
| Core problem | Multiplatform navigation | Multiplatform ViewModel + lifecycle abstractions |
| Official alternative exists | Partial (Navigation 3, not yet stable multiplatform) | Yes (JetBrains lifecycle) |
| Unique value remaining | High (multi-backstack, overlay, NavEventNavigator, cross-platform) | Medium (type-safe keys, `@Parcelize` in common) |
| **Final status** | **Not maintained. Preserved for reference.** | **Not maintained. Preserved for reference.** |
| Archive GitHub? | Not yet — users can still browse/fork | Not yet — users can still browse/fork |
| Further Maven releases? | ❌ No | ❌ No |
| Documentation preserved? | ✅ Yes — see `docs/` | ✅ Yes — see `docs/` |
| Migration guide available? | ✅ Yes — see `docs/migration/` | ✅ Yes — see `docs/migration/` |

---

## Final note

Both libraries were written to fill real gaps in the Kotlin Multiplatform ecosystem at the time.
Those gaps are now being closed by official APIs from JetBrains and Google. The right decision
is to stop maintaining these libraries and let the official ecosystem take over.

The concepts, patterns, and design decisions documented in `docs/` are the lasting contribution
of this work. They are applicable regardless of which navigation or lifecycle library you use,
and they reflect production-tested thinking about:

- How to model navigation state as observable Compose state
- How to architect multi-backstack navigation cleanly
- How to decouple navigation commands from UI using Channels
- How to preserve state across process death on non-Android platforms
- How to write idiomatic Kotlin Multiplatform abstractions over platform-specific APIs

Thank you to everyone who used, contributed to, or filed issues on these libraries.
