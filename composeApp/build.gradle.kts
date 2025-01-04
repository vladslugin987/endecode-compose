import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.preview)
            implementation(compose.material3)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation("org.jetbrains.compose.ui:ui-util:1.5.11")

            // Drag-and-drop dependencies
            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)

            // Core dependencies
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("commons-io:commons-io:2.15.1")
            implementation("org.openpnp:opencv:4.7.0-0")
            implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
            implementation("ch.qos.logback:logback-classic:1.4.14")
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

// Define icon paths
val iconsDir = project.file("src/commonMain/resources/icons")
val macIcon = iconsDir.resolve("icon.icns")
val winIcon = iconsDir.resolve("icon.ico")

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "ENDEcode"
            packageVersion = "2.0.9"
            description = "File encryption and watermarking tool"
            copyright = "© 2025 vsdev. All rights reserved."
            vendor = "vsdev"

            macOS {
                bundleID = "com.vsdev.endecode"
                appCategory = "public.app-category.productivity"
                dockName = "ENDEcode"
                iconFile.set(macIcon)

                infoPlist {
                    extraKeysRawXml = """
                        <key>LSMinimumSystemVersion</key>
                        <string>10.13</string>
                        <key>CFBundleVersion</key>
                        <string>2.0.9</string>
                        <key>CFBundleShortVersionString</key>
                        <string>2.0.9</string>
                        <key>LSArchitecturePriority</key>
                        <array>
                            <string>x86_64</string>
                            <string>arm64</string>
                        </array>
                    """.trimIndent()
                }
            }

            windows {
                menuGroup = "ENDEcode"
                upgradeUuid = "61DAB35E-17B1-4B61-B6E3-9CD413D5AA96"
                iconFile.set(winIcon)
                dirChooser = true
                perUserInstall = true
                shortcut = true
                jvmArgs += listOf("-Dfile.encoding=UTF-8")
            }

            // Common options for all platforms
            modules("java.sql", "java.naming", "jdk.unsupported")

            // JVM options
            jvmArgs += listOf(
                "-Xms512m",
                "-Xmx2048m",
                "--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
                "--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED",
                "-XX:+UseG1GC",
                "-XX:+UseStringDeduplication",
                "-Dfile.encoding=UTF-8"
            )
        }
    }
}

tasks.withType<JavaExec> {
    jvmArgs(
        "--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
        "--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED"
    )
}

tasks.register("cleanDist") {
    group = "build"
    description = "Cleans the distribution directory"
    doLast {
        delete(project.buildDir.resolve("compose/binaries"))
    }
}

tasks.named("clean") {
    dependsOn("cleanDist")
}

tasks.named("build") {
    dependsOn("createDistributable")
}