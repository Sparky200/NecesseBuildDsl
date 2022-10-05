# Necesse Build DSL

Necesse Build DSL is a Gradle plugin which allows you
to create Necesse mods with easier syntax.

## Using Necesse Build DSL

The plugin is available on the Gradle Plugin Portal [here](https://plugins.gradle.org/plugin/dev.sparky200.necesse-build-dsl).

Here is example usage of the plugin:

build.gradle.kts
```kts
dependencies {
    // Depends on necesse automatically, as well as the
    // ./mods/ local folder and lib folder.
    necesse(project, "GAME_PATH_HERE")
}

mod {
    id.set("modid")
    name.set("My Mod")
    description.set("This is an example mod!")
    gamePath.set("GAME_PATH_HERE")
    author.set("Sparky")
    version.set("1.0")
    // Game version as of writing this.
    // Make sure to check for the latest version!
    gameVersion.set("0.21.25")
}
```
