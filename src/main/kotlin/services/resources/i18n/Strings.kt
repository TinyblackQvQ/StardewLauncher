package services.resources.i18n

import kotlinx.serialization.Serializable

@Serializable
data class StringRoot(
    val general: GeneralStrings,
    val ui: UIStrings,
    val modpack: ModPackStrings,
    val settings: SettingsStrings
)

@Serializable
data class GeneralStrings(
    val title: String,
    val menu: GeneralMenuStrings,
    val popups: GeneralPopupsStrings
)

@Serializable
data class GeneralMenuStrings(
    val launch: String,
    val packageManager: String,
    val download: String,
    val settings: String
)

@Serializable
data class GeneralPopupsStrings(
    val confirm: String,
    val cancel: String
)

@Serializable
data class UIStrings(
    val packageManager: PackageManagerStrings
)

@Serializable
data class PackageManagerStrings(
    val title: String,
    val subTitle: String,
    val popups: PackageManagerPopupsStrings
)

@Serializable
data class PackageManagerPopupsStrings(
    val create: CreateModPackStrings,
    val delete: DeleteModPackStrings,
    val copy: CopyModPackStrings,
    val install: InstallModPackStrings,
    val export: ExportModPackStrings
)

@Serializable
data class CreateModPackStrings(
    val title: String,
    val input: CreateModPackInputStrings
)

@Serializable
data class CreateModPackInputStrings(
    val name: InputFieldStrings,
    val description: InputFieldStrings,
    val gameVersion: InputFieldStrings,
    val apiVersion: InputFieldStrings
)

@Serializable
data class InputFieldStrings(
    val label: String,
    val placeholder: String? = null
)

@Serializable
data class DeleteModPackStrings(
    val title: String
)

@Serializable
data class CopyModPackStrings(
    val title: String,
    val input: CopyModPackInputStrings
)

@Serializable
data class CopyModPackInputStrings(
    val copyConfig: InputFieldStrings,
    val copySave: InputFieldStrings
)

@Serializable
data class InstallModPackStrings(
    val title: String,
    val input: InstallModPackInputStrings
)

@Serializable
data class InstallModPackInputStrings(
    val file: InputFieldStrings,
    val useModPackConfig: InputFieldStrings
)

@Serializable
data class ExportModPackStrings(
    val title: String,
    val input: ExportModPackInputStrings
)

@Serializable
data class ExportModPackInputStrings(
    val file: InputFieldStrings,
    val attachConfigs: InputFieldStrings
)

@Serializable
data class ModPackStrings(
    val import: ImportStrings
)

@Serializable
data class ImportStrings(
    val importingMods: String,
    val importingMod: String,
    val importingSaves: String,
    val alreadyExists: String,
    val completed: String,
    val failed: String,
    val invalidSourceDirectory: String,
    val manifestNotFound: String,
    val modAlreadyInstalled: String,
    val success: String,
    val invalidZipFile: String,
    val unsupportedZipFormat: String,
    val failedToExtract: String
)

@Serializable
data class SettingsStrings(
    val general: GeneralSettingsStrings
)

@Serializable
data class GeneralSettingsStrings(
    val appLanguage: SettingOptionStrings,
    val colorMode: SettingOptionStrings,
    val gameDirectory: SettingOptionStrings,
    val isAppFirstLaunch: SettingOptionStrings,
    val modLibraryDirectory: SettingOptionStrings
)

@Serializable
data class SettingOptionStrings(
    val title: String,
    val desc: String
)

val AppString = StringRoot(
    general = GeneralStrings(
        title = "",
        menu = GeneralMenuStrings(
            launch = "",
            packageManager = "",
            download = "",
            settings = ""
        ),
        popups = GeneralPopupsStrings(
            confirm = "",
            cancel = ""
        )
    ),
    ui = UIStrings(
        packageManager = PackageManagerStrings(
            title = "",
            subTitle = "",
            popups = PackageManagerPopupsStrings(
                create = CreateModPackStrings(
                    title = "",
                    input = CreateModPackInputStrings(
                        name = InputFieldStrings("", ""),
                        description = InputFieldStrings("", ""),
                        gameVersion = InputFieldStrings("", ""),
                        apiVersion = InputFieldStrings("", "")
                    )
                ),
                delete = DeleteModPackStrings(""),
                copy = CopyModPackStrings(
                    title = "",
                    input = CopyModPackInputStrings(
                        copyConfig = InputFieldStrings("", ""),
                        copySave = InputFieldStrings("", "")
                    )
                ),
                install = InstallModPackStrings(
                    title = "",
                    input = InstallModPackInputStrings(
                        file = InputFieldStrings("", ""),
                        useModPackConfig = InputFieldStrings("", "")
                    )
                ),
                export = ExportModPackStrings(
                    title = "",
                    input = ExportModPackInputStrings(
                        file = InputFieldStrings("", ""),
                        attachConfigs = InputFieldStrings("", "")
                    )
                )
            )
        )
    ),
    modpack = ModPackStrings(
        import = ImportStrings(
            importingMods = "",
            importingMod = "",
            importingSaves = "",
            alreadyExists = "",
            completed = "",
            failed = "",
            invalidSourceDirectory = "",
            manifestNotFound = "",
            modAlreadyInstalled = "",
            success = "",
            invalidZipFile = "",
            unsupportedZipFormat = "",
            failedToExtract = ""
        )
    ),
    settings = SettingsStrings(
        general = GeneralSettingsStrings(
            appLanguage = SettingOptionStrings(
                title = "",
                desc = ""
            ),
            colorMode = SettingOptionStrings(
                title = "",
                desc = ""
            ),
            gameDirectory = SettingOptionStrings(
                title = "",
                desc = ""
            ),
            isAppFirstLaunch = SettingOptionStrings(
                title = "",
                desc = ""
            ),
            modLibraryDirectory = SettingOptionStrings(
                title = "",
                desc = ""
            )
        )
    )
)