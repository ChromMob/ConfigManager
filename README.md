1. Add jitpack repository.
```
repositories {
    maven {
        name = "jitpack"
        url = "https://jitpack.io"
    }
}
```
2. Add dependency
```
dependencies {
    implementation 'com.github.ChromMob:ConfigManager:master-SNAPSHOT'
}
```
## Files the user has edited
- Keys in the file that your code does not declare, at any depth, are kept and written back as they are. Read them through `getKey(name).getValue()` (a section gives its raw map).
- If a file cannot be read, or one of its values cannot, `loadConfig` returns false, the problem goes to the logger (`new ConfigManager(folder, plugin.getLogger())`), and `addConfig` does not save over the file.
- On reload, a key removed from the file goes back to its default. `removeKey` (and `removeChild` in a section) drops a key a newer version no longer uses from the file on the next save.
- Files are replaced in one step, in UTF-8, and only when their content changes.
