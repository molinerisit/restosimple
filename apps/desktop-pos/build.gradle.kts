import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.kotlin.compose)
}

group   = "ar.ticketsimple"
version = "1.0.0"

kotlin { jvmToolchain(26) }

tasks.withType<JavaCompile>().configureEach {
    options.release.set(23)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.coroutines.swing)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.sqlite.jdbc)
    implementation(libs.logback.classic)
}

compose.desktop {
    application {
        mainClass = "ar.ticketsimple.pos.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            packageName        = "TicketSimple"
            packageVersion     = "1.0.0"
            description        = "POS para restaurantes, cafeterías y pastelerías"
            copyright          = "© 2026 TicketSimple"
            vendor             = "TicketSimple"
            windows {
                menuGroup   = "TicketSimple"
                upgradeUuid = "b1e2c3d4-e5f6-7890-abcd-ef1234567890"
            }
        }
    }
}
