plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt") version("1.23.8")
}

detekt {
    toolVersion = "1.23.8"
    config.setFrom(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

group = "org.miluko"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.22")
    implementation("io.gitlab.arturbosch.detekt:detekt-api:1.23.8")
    implementation("io.gitlab.arturbosch.detekt:detekt-parser:1.23.8")
}

tasks.jar {
    archiveBaseName.set("detekt-custom-rules") // JAR 文件名
}