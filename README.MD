## About
OpenInv is a [Bukkit plugin](https://dev.bukkit.org/projects/openinv) which allows users to open and edit anyone's inventory or ender chest - online or not!

## Features
- **OpenInv**: Open anyone's inventory, even if they're offline.
  - Read-only mode! Don't grant edit permission.
  - Cross-world support! Allow access only from the same world.
  - No duplicate slots! Only armor is accessible when opening self (if allowed at all)!
  - Drop items as the player! Place items in the dropper slot in the bottom right. Can be disabled via permission!
  - Allow any item in armor slots! Configurable via permission.
- **OpenEnder**: Open anyone's ender chest, even if they're offline.
  - Allow access only to own ender chest! Don't grant permission to open others.
  - Read-only mode! Don't grant edit permission.
  - Cross-world support! Allow access only from the same world.
- **SilentContainer**: Open containers without displaying an animation or making sound.
- **AnyContainer**: Open containers, even if blocked by ocelots or blocks.

## Commands
See [the wiki](https://github.com/Jikoo/OpenInv/wiki/Commands).

## Permissions
See [the wiki](https://github.com/Jikoo/OpenInv/wiki/Permissions)

## For Developers

### As a Dependency
The OpenInv API is available via [JitPack](https://jitpack.io/).
```xml
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
```
```xml
  <dependencies>
    <dependency>
      <groupId>com.github.Jikoo</groupId>
      <artifactId>OpenInv</artifactId>
      <version>${openinv.version}</version>
    </dependency>
  </dependencies>
```
Note that since JitPack only builds the API now, the "full" OpenInv jar on JitPack is actually the openinvapi artifact. This is a change from previous dependency declaration that I hope to revert.

### Compilation
Execute the gradle wrapper:  
`./gradlew build`

If you encounter issues with building the Spigot module, try running BuildTools manually.
