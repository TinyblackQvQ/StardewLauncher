## StardewLauncher ☕

*StardewLauncher is a custom mod manager & launcher with [SMAPI](https://github.com/Pathoschild/SMAPI) for Stardew
Valley on Desktop.* 🕶️

It's developed
based on [Kotlin Multiplatform](https://www.jetbrains.com.cn/en-us/help/kotlin-multiplatform-dev/get-started.html) with
the design of [Material Design 3](https://m3.material.io/).

> You can read this README with another language:
> English | [中文（简体）](https://github.com/TinyblackQvQ/StardewLauncher/tree/dev/docs/README.zh_cn.md)

## Features ✨

- Easy management of mods, auto-process dependencies for every enable / disable switch.

- Easy version updating, keep your configs for every update.

- Individual saves & configs separation would give you a different experience for every adventure.

## Develop Roadmap 📖

You can [create a new issue](https://github.com/TinyblackQvQ/StardewLauncher/issues/new) to discuss your idea.

Suitable ideas will be added to the roadmap.

| Feature                                     | Status | Supported Version | Priority |
|---------------------------------------------|--------|-------------------|----------|
| Basic ModPack Management(Create/Check Mods) | ❌      | -                 | High     |
| SMAPI installation                          | ❌      | -                 | High     |
| In app game launch                          | ❌      | -                 | High     |
| ModPack Import / Export                     | ❌      | -                 | High     |
| ModPack Configuration Customization         | ❌      | -                 | Medium   |
| ModPack Saves Separation                    | ✅      | not released      | High     |
| ModPack Saves Live Backup                   | ❌      | -                 | Medium   |
| Mod update check                            | ❌      | -                 | High     |
| Mod download support for Nexus Mods         | ❌      | -                 | High     |
| Mod download support for ModDrop            | ❌      | -                 | Medium   |
| Mod download support for CurseForge         | ❌      | -                 | High     |
| Mod download support for ChuckleFish        | ❌      | -                 | Low      |
| Mod download support for Github             | ❌      | -                 | Low      |
| i18n files download & install               | ❌      | -                 | Medium   |
| Local editor for i18n file                  | ❌      | -                 | Low      |
| Local i18n file upload                      | ❌      | -                 | Low      |
| XNB mod conversion to ContentPacker mod     | ❌      | -                 | Low      |
| Settings modification for application       | ❌      | -                 | High     |
| i18n localization support                   | ✅      | not released      | High     |
| Customized Material3 Theme support          | ✅      | not released      | Medium   |

## How to install & run 💻

See [Releases](https://github.com/TinyblackQvQ/StardewLauncher/releases/) to download the `.msi` installer or
distributed zip file.

## Local Build 🛠️

### Environment 💻

- [IntelliJ IDEA Community Edition 2025.1](https://www.jetbrains.com/idea/download/)
- [JDK 21+](https://www.oracle.com/java/technologies/downloads/)

### Get Started ✍️

Open the terminal where you want to copy the repository with git:

```bash
git clone "https://github.com/TinyblackQvQ/StardewLauncher.git"
```

Then open the project with IDEA. use gradle to sync the project.

```bash
./gradlew --refresh-dependencies
```

After downloaded the dependencies, you can start to build the project.

## How to contribute ❤️

1. Fork this repository in your GitHub panel
2. Open the terminal in the project's root folder
3. Run commands below to add upstream for your git workspace.

   ```bash
   git clone "${your_forked_repo_git_url}"
   cd "./${your_forked_repo_name}"
   git remote add upstream "https://github.com/TinyblackQvQ/StardewLauncher.git"
   ```

4. Then sync project like steps introduced above
5. Create your branch for your work, then do anything you want to make your contribution
6. After finish your work, use `git pull upstream` to sync latest changes from upstream repo, after possible conflicts
   solved, run `git push` to upload your changes to your remote repo
7. In the GitHub panel, commit a `Pull Request` to tell me about your changes, then I will evaluate your changes, when
   finished, you will know whether you code have been merged to our newest branches. You can track the info of your PR
   to get further information.

## License 📄

This project uses **AGPL-3.0**, you can use our code freely for non-commercial use!

**It's not allowed to use the code for ANY commercial use.**