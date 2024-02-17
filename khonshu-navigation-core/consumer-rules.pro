# don't obfuscate route classes because the destination id is derived from its name, the short
# obfuscated names lead to collisions
-keepnames class * extends com.hoc081098.solivagant.navigation.BaseRoute
