import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
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

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "ENDEcode"
            packageVersion = "2.0.0"
            description = "File encryption and watermarking tool"
            copyright = "© 2024 vsdev. All rights reserved."
            vendor = "vsdev"

            macOS {
                bundleID = "com.vsdev.endecode"
                appCategory = "public.app-category.productivity"
                dockName = "ENDEcode"

                iconFile.set(project.file("icon.icns"))


                // macOS specific settings
                infoPlist {
                    extraKeysRawXml = """
                        <key>LSMinimumSystemVersion</key>
                        <string>10.13</string>
                        <key>CFBundleVersion</key>
                        <string>2.0.0</string>
                        <key>CFBundleShortVersionString</key>
                        <string>2.0.0</string>
                        <key>LSArchitecturePriority</key>
                        <array>
                            <string>x86_64</string>
                            <string>arm64</string>
                        </array>
                    """.trimIndent()
                }
            }

            // Required Java modules
            modules("java.sql", "java.naming", "jdk.unsupported")
        }
    }
}

tasks.withType<JavaExec> {
    jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")
}