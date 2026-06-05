# Repository Status Recommendation

Engineering trade-off analysis for **solivagant** and **kmp-viewmodel** as of mid-2025.

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

### Recommendation: **Minimal maintenance mode with documented deprecation intent**

**Reasoning:**
- solivagant still solves a real problem that official APIs do not yet solve on all platforms.
  Deprecating it before Navigation 3 is stable and multiplatform would harm existing users.
- However, the alpha status and sparse test coverage mean that significant new investment is
  not justified.
- The library should continue to receive dependency updates (Kotlin, CMP, AndroidX) to remain
  compilable. Critical bugs should be fixed. New features should not be added.
- A deprecation notice should be added to the README once Navigation 3 is stable on iOS and
  Desktop, not before.

**Recommended README changes (add now):**
```markdown
> [!NOTE]
> solivagant is in minimal maintenance mode. It continues to receive dependency updates and
> critical bug fixes. New features are not planned. If you are starting a new project,
> evaluate [Navigation 3](https://developer.android.com/guide/navigation/navigation3) for
> Android and assess its multiplatform readiness before choosing a navigation library.
> See [docs/maintenance/repository-status.md] for details.
```

**GitHub archive:** Do **not** archive. The library is still compilable and used by active projects.
Archiving would prevent users from reporting bugs and would signal abandonment to library consumers.

**Maven artifact deprecation:** Do **not** deprecate Maven artifacts now. Deprecate when
Navigation 3 is stable and multiplatform (probably 2026). Deprecated Maven artifacts should
point to the official Navigation 3 documentation.

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

### Recommendation: **Split into two maintenance tracks**

**Core `viewmodel` module:** Move to deprecated but not archived. Add a README notice pointing
to `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel`. Continue receiving Kotlin/CMP
dependency updates for at least one year to give users time to migrate. Stop accepting new
features.

**`viewmodel-savedstate` module (type-safe keys, `@Parcelize`):** Keep in minimal maintenance
mode. This module has unique value that official APIs do not yet cover. Deprecate when either:
- Official multiplatform `@Parcelize` support ships, or
- The type-safe key API has an official equivalent.

**`viewmodel-compose` and DI modules:** Tie their lifecycle to the core module. Once the core is
deprecated, these become thin wrappers with no reason to exist. Deprecate them alongside the core.

**Recommended README changes (add now):**
```markdown
> [!NOTE]
> The core `kmp-viewmodel` ViewModel is superseded by
> [`org.jetbrains.androidx.lifecycle:lifecycle-viewmodel`](https://central.sonatype.com/artifact/org.jetbrains.androidx.lifecycle/lifecycle-viewmodel)
> for new projects. The `kmp-viewmodel-savedstate` module (type-safe `SavedStateHandle` keys and
> `@Parcelize` support in common code) retains unique value and remains maintained.
> See [docs/maintenance/repository-status.md] for details and migration guidance.
```

**GitHub archive:** Do **not** archive. The savedstate module is actively useful. Archive
only after the savedstate module is superseded or explicitly deprecated.

**Maven artifact deprecation for core ViewModel:** Publish a 0.9.0 release with
`@Deprecated` annotations on the core `ViewModel` class pointing to the official artifact.
Keep the Maven coordinates alive so existing users' builds do not break.

---

## Comparison Table

| Aspect | solivagant | kmp-viewmodel |
|--------|------------|---------------|
| Core problem | Multiplatform navigation | Multiplatform ViewModel + lifecycle abstractions |
| Official alternative exists | Partial (Navigation 3, not yet multiplatform) | Yes (JetBrains lifecycle) |
| Unique value remaining | High (multi-backstack, overlay, NavEventNavigator, cross-platform) | Medium (type-safe keys, `@Parcelize` in common) |
| Recommended status | Minimal maintenance | Split: core deprecated; savedstate minimal maintenance |
| Archive GitHub now? | ❌ No | ❌ No |
| Deprecate Maven now? | ❌ No | ⚠️ Core only, with notice |
| README banner needed? | ✅ Maintenance mode note | ✅ Core superseded note |
| Migration guide available? | ✅ Partial (see migration docs) | ✅ Yes (see migration docs) |

---

## Suggested Next Actions

### Immediate (no code changes required)

1. Add the maintenance mode note to solivagant README.
2. Add the superseded note to kmp-viewmodel README.
3. Publish these docs to the repository.
4. Check the latest Navigation 3 release notes and update this document's assessment of its
   multiplatform readiness.

### Short term (3–6 months)

1. Add `@Deprecated` annotations to `kmp-viewmodel`'s core ViewModel class with a migration
   pointer to the JetBrains lifecycle artifact.
2. Improve test coverage on `MultiStack` and `Stack` in solivagant to reduce regression risk
   during dependency update maintenance.
3. Write a blog post documenting solivagant's design decisions and their relationship to
   Navigation 3's approach. This creates lasting educational value.

### Medium term (6–18 months, after Navigation 3 stabilises)

1. Add deprecation banners to solivagant when Navigation 3 is stable on iOS and Desktop.
2. Deprecate Maven artifacts for `solivagant-navigation` with a link to Navigation 3.
3. Archive solivagant if no new issues are being filed and no migration questions remain open.
4. If official multiplatform `@Parcelize` ships, deprecate `kmp-viewmodel-savedstate` accordingly.
