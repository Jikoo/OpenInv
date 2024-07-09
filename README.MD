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
      <groupId>com.github.jikoo.OpenInv</groupId>
      <artifactId>openinvapi</artifactId>
      <version>${openinv.version}</version>
    </dependency>
  </dependencies>
```

### Compilation
To compile, the relevant Spigot jars must be installed in the local repository.
As OpenInv is compiled against Mojang's mappings, you must run BuildTools with the `--remapped` argument:  
`java -jar BuildTools.jar --remapped --rev $serverVersion`  
`$serverVersion` is the version of the server, i.e. `1.18.1`

To compile just the API, execute Maven as usual:  
`mvn clean package`  

To compile the full plugin, use the provided profile with `-P all`:  
`mvn clean package -am -P all`  
For more information, check out the [official Maven guide](http://maven.apache.org/guides/introduction/introduction-to-profiles.html).
