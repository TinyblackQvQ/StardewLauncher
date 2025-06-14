import org.jetbrains.compose.desktop.application.dsl.TargetFormat


plugins {
    kotlin("jvm") version "2.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.compose") version "1.8.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0-RC2"
    kotlin("plugin.serialization") version "2.1.21"
}


kotlin {
    jvmToolchain(21)
}

detekt {
    toolVersion = "1.23.8"
    config.setFrom(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    config.setFrom(files("${project.rootDir}/config/detekt/custom.yml"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "org.miluko"
version = "1.0.0-pre.25615"

repositories {
    mavenCentral()
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.material3AdaptiveNavigationSuite)
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("com.russhwolf:multiplatform-settings:1.3.0")
    implementation("com.mayakapps.compose:window-styler:0.3.3-SNAPSHOT")
    implementation("com.materialkolor:material-kolor:2.1.1")
    implementation("com.charleskorn.kaml:kaml:0.80.1")
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
    val koinVersion = "4.0.3"
    implementation("io.insert-koin:koin-core:${koinVersion}")
    implementation("io.insert-koin:koin-compose:${koinVersion}")
    implementation("io.insert-koin:koin-compose-viewmodel:${koinVersion}")
    implementation("io.insert-koin:koin-compose-viewmodel-navigation:${koinVersion}")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += "-Djava.awt.headless=false"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "StardewLauncher"
            packageVersion = "1.0.0"
        }
    }
}
