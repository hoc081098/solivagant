# solivagant 🔆

## Compose Multiplatform Navigation - Pragmatic, type safety navigation for Compose Multiplatform. Based on [Freeletics Navigation](https://freeletics.github.io/khonshu/navigation/get-started/).

- Integrate with `Jetpack Compose` and `Compose Multiplatform` seamlessly.

- Integrate with `kmp-viewmodel` library seamlessly
  - Stack entry scoped `ViewModel`, exists as long as the
    stack entry is on the navigation stack, including the configuration changes on `Android`.
  - Supports `SavedStateHandle`, used to save and restore data over configuration changes
    or process death on `Android`.

- Type safety navigation, easy to pass data between destinations.

- Supports Multi-Backstacks, this is most commonly used in apps that use bottom navigation to
  separate the back stack of each tab.
  See [Freeletics Navigation - Multiple back stacks](https://freeletics.github.io/khonshu/navigation/back-stacks/).

- Supports `Lifecycle` events, similar to `AndroidX Lifecycle` library.

## Credits

- Most of code in `solivagant-khonshu-navigation-core` and `solivagant-navigation` libraries is
  taken from [Freeletics Navigation](https://freeletics.github.io/khonshu/navigation/get-started/),
  and ported to `Kotlin Multiplatform` and `Compose Multiplatform`.

- The `solivagant-lifecycle` library is inspired
  by [Essenty Lifecycle](https://github.com/arkivanov/Essenty?tab=readme-ov-file#lifecycle),
  and [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle).

[![maven-central](https://img.shields.io/maven-central/v/io.github.hoc081098/solivagant-navigation)](https://search.maven.org/search?q=g:io.github.hoc081098%20solivagant-navigation)
[![codecov](https://codecov.io/gh/hoc081098/solivagant/branch/master/graph/badge.svg?token=jBFg12osvP)](https://codecov.io/gh/hoc081098/solivagant)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin version](https://img.shields.io/badge/Kotlin-1.9.22-blueviolet?logo=kotlin&logoColor=white)](http://kotlinlang.org)
[![KotlinX Coroutines version](https://img.shields.io/badge/Kotlinx_Coroutines-1.7.3-blueviolet?logo=kotlin&logoColor=white)](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.7.3)
[![Compose Multiplatform version](https://img.shields.io/badge/Compose_Multiplatform-1.5.11-blueviolet?logo=kotlin&logoColor=white)](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.5.11)
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fhoc081098%2Fsolivagant&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)

![badge][badge-android]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-js-ir]
![badge][badge-nodejs]
![badge][badge-linux]
![badge][badge-windows]
![badge][badge-ios]
![badge][badge-mac]
![badge][badge-watchos]
![badge][badge-tvos]
![badge][badge-apple-silicon]

<p align="center">
    <img src="https://github.com/hoc081098/solivagant/raw/master/logo.png" width="400">
</p>

## Author: [Petrus Nguyễn Thái Học](https://github.com/hoc081098)

Liked some of my work? Buy me a coffee (or more likely a beer)

<a href="https://www.buymeacoffee.com/hoc081098" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" height=64></a>

## Docs

### **0.x release** docs: https://hoc081098.github.io/solivagant/docs/0.x

### Snapshot docs: https://hoc081098.github.io/solivagant/docs/latest

## Installation

```kotlin
allprojects {
  repositories {
    [...]
    mavenCentral()
  }
}
```

```kotlin
implementation("io.github.hoc081098:solivagant-navigation:0.0.1-alpha01")
```

## Roadmap

## License

```license
                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/
```

[badge-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat

[badge-android-native]: http://img.shields.io/badge/support-[AndroidNative]-6EDB8D.svg?style=flat

[badge-wearos]: http://img.shields.io/badge/-wearos-8ECDA0.svg?style=flat

[badge-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat

[badge-js]: http://img.shields.io/badge/-js-F8DB5D.svg?style=flat

[badge-js-ir]: https://img.shields.io/badge/support-[IR]-AAC4E0.svg?style=flat

[badge-nodejs]: https://img.shields.io/badge/-nodejs-68a063.svg?style=flat

[badge-linux]: http://img.shields.io/badge/-linux-2D3F6C.svg?style=flat

[badge-windows]: http://img.shields.io/badge/-windows-4D76CD.svg?style=flat

[badge-wasm]: https://img.shields.io/badge/-wasm-624FE8.svg?style=flat

[badge-apple-silicon]: http://img.shields.io/badge/support-[AppleSilicon]-43BBFF.svg?style=flat

[badge-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg?style=flat

[badge-mac]: http://img.shields.io/badge/-macos-111111.svg?style=flat

[badge-watchos]: http://img.shields.io/badge/-watchos-C0C0C0.svg?style=flat

[badge-tvos]: http://img.shields.io/badge/-tvos-808080.svg?style=flat
