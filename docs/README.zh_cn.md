## StardewLauncher ☕

*StardewLauncher 是一款以 [SMAPI](https://github.com/Pathoschild/SMAPI) 基础开发的 Stardew Valley
桌面自定义模组管理器与启动器。* 🕶️

本项目采用 [Kotlin Multiplatform](https://www.jetbrains.com.cn/en-us/help/kotlin-multiplatform-dev/get-started.html)
构建，并遵循 [Material Design 3](https://m3.material.io/) 的设计规范。

> 您可以阅读此 README 的其他语言版本：
> [English](https://github.com/TinyblackQvQ/StardewLauncher/tree/dev/README.md) | 简体中文

## 主要功能 ✨

目前功能正在积极开发中，敬请期待！

## 开发路线图 📖

我们欢迎您通过 [创建新问题](https://github.com/TinyblackQvQ/StardewLauncher/issues/new)
来提出任何建议或想法。经过讨论后，有价值的提议将会被纳入开发计划。

### 当前开发计划概览

| 功能名称                         | 实现状态   | 已支持版本 | 开发优先级 |
|------------------------------|--------|-------|-------|
| 基础 ModPack 管理（创建/模组检查）       | ❌ 未实现  | -     | 高 ⭐⭐⭐ |
| SMAPI 安装                     | ❌ 未实现  | -     | 高 ⭐⭐⭐ |
| 应用内游戏启动                      | ❌ 未实现  | -     | 高 ⭐⭐⭐ |
| ModPack 导入 / 导出              | ❌ 未实现  | -     | 高 ⭐⭐⭐ |
| ModPack 配置自定义                | ❌ 未实现  | -     | 中 ⭐⭐  |
| ModPack 存档分离                 | ✅ 已完成  | 未发布   | -     |
| ModPack 存档实时备份               | ❌ 未实现  | -     | 中 ⭐⭐  |
| 模组更新检查                       | ❌ 未实现  | -     | 高 ⭐⭐⭐ |
| 支持从 Nexus Mods 下载模组          | ❌ 未实现  | -     | 高 ⭐⭐⭐ |
| 支持从 Stardrop 下载模组            | ❌ 未实现  | -     | 中 ⭐⭐  |
| 支持从 CurseForge 下载模组          | ❌ 未实现  | -     | 高 ⭐⭐⭐ |
| 支持从 Chucklefish 下载模组         | ❌ 未实现  | -     | 低 ⭐   |
| 支持从 GitHub 下载模组              | ❌ 未实现  | -     | 低 ⭐   |
| i18n 文件下载与安装                 | ❌ 未实现  | -     | 中 ⭐⭐  |
| i18n 文件本地编辑                  | ❌ 未实现  | -     | 低 ⭐   |
| 本地 i18n 文件上传                 | ❌ 未实现  | -     | 低 ⭐   |
| 将 XNB 模组转换为 ContentPacker 格式 | ❌ 未实现  | -     | 低 ⭐   |
| 应用程序设置修改                     | ❌ 未实现  | -     | 高 ⭐⭐⭐ |
| 国际化支持                        | ✅ 已完成  | 未发布   | 高 ⭐⭐⭐ |
| 自定义 Material3 主题支持           | - 部分完成 | 未发布   | 中 ⭐⭐  |

## 如何安装与运行 💻

如需获取最新版本，请访问我们的 [发布页面](https://github.com/TinyblackQvQ/StardewLauncher/releases/)，您可以在该页面下载适用于
Windows 的 `.msi` 安装包或通用的 ZIP 分发包。

## 本地构建 🛠️

### 环境 💻

- [IntelliJ IDEA 社区版 2025.1](https://www.jetbrains.com/idea/download/)
- [JDK 21+](https://www.oracle.com/java/technologies/downloads/)

### 快速开始 ✍️

在您希望存放仓库的终端中执行以下命令：

```
git clone "https://github.com/TinyblackQvQ/StardewLauncher.git"
```

然后使用 IDEA 打开项目，并使用 Gradle 同步项目。

```
./gradlew --refresh-dependencies
```

下载完依赖后，您可以开始构建项目。

## 如何贡献 ❤️

1. 在您的 GitHub 面板中 fork 本仓库
2. 在项目的根文件夹中打开终端
3. 运行以下命令为您的 git 工作区添加上游：

   ```bash
   git clone "${your_forked_repo_git_url}"
   cd "./${your_forked_repo_name}"
   git remote add upstream "https://github.com/TinyblackQvQ/StardewLauncher.git"
   ```

4. 然后像上面介绍的步骤一样同步项目
5. 为您的工作创建分支，然后进行任何您想做的贡献
6. 完成工作后，使用 `git pull upstream` 从上游仓库同步最新更改，在解决可能的冲突后，运行 `git push` 将更改上传到您的远程仓库
7. 在 GitHub 面板中，提交一个 `Pull Request` 告诉我您的更改，之后我会评估您的更改，完成后，您将知道您的代码是否已合并到我们的新分支中。您可以跟踪您的
   PR 信息以获取进一步的信息。

## 许可证 📄

本项目使用 **AGPL-3.0** 协议，您可以自由地非商业使用我们的代码！

**不允许将代码用于任何商业用途。**
