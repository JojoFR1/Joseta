import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.util.Properties

plugins {
    java
    application
    alias(libs.plugins.shadow)
    alias(libs.plugins.jandex)
}

group = "dev.jojofr"
version = "2.6.0"

java.toolchain.languageVersion = JavaLanguageVersion.of(21)
application { mainClass = "dev.jojofr.joseta.JosetaBot" }

repositories { mavenCentral() }
dependencies {
    implementation(libs.jda) {
        // Optionally disable audio natives to reduce jar size by excluding `opus-java` and `tink`
        exclude(module = "opus-java")
        exclude(module = "tink")
    }

    implementation(libs.logback.classic)
    implementation(libs.dotenv)

    implementation(libs.jandex)

    implementation(libs.postgresql)
    implementation(libs.hikari)

    implementation(libs.jdbi.core)
    implementation(libs.jdbi.sqlobject)
    implementation(libs.jdbi.postgres)

    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform)

    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)

    testImplementation(libs.assertj)
}
jandex { version = libs.versions.jandex }

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // Enable parameter name retention at runtime, required for the InteractionProcessor & for JDBI arguments
    options.compilerArgs.add("-parameters")
}

tasks.shadowJar {
    dependsOn(tasks.named("jandex"))
    archiveFileName = "JosetaBot.jar"

    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.FAIL

    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    exclude("META-INF/*LICENSE*", "META-INF/*LICENCE*", "META-INF/README*", "META-INF/NOTICE*")
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "1G"

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

val localProperties = providers.fileContents(layout.projectDirectory.file("local.properties"))
    .asText
    .map { text -> Properties().apply { load(text.reader()) } }
    .orElse(Properties())

val remoteUser = localProperties.map { it.getProperty("remoteUser") ?: System.getProperty("user.name") }
val remoteHost = localProperties.map { it.getProperty("remoteHost") ?: "localhost" }
val remotePort = localProperties.map { it.getProperty("remotePort")?.toInt() ?: 22 }
val remoteKeyPath = localProperties.map { it.getProperty("remoteKeyPath")?.let { path ->
    val home = System.getProperty("user.home")
    when {
        path == "~" -> home
        path.startsWith("~/") -> home + path.substring(1)
        else -> path
    }
}}

tasks.register<Exec>("uploadServer") {
    group = "deployment"
    description = "Uploads the shadow JAR to the remote server using `scp`."

    isIgnoreExitValue = true // Weird quirk of Pelican's SFTP

    dependsOn(tasks.shadowJar)

    val jarFile = tasks.shadowJar.flatMap { it.archiveFile }
    val user = remoteUser
    val host = remoteHost
    val port = remotePort
    val key = remoteKeyPath

    doFirst {
        val file = jarFile.get().asFile
        val target = "${user.get()}@${host.get()}"

        val command = mutableListOf("scp", "-P", port.get().toString())
        key.orNull?.let { command += listOf("-i", File(it).absolutePath) }
        command += listOf(file.absolutePath, "$target:.")

        commandLine(command)
        logger.lifecycle("Uploading ${file.name} to $target...")
    }

    doLast { logger.lifecycle("scp finished (exit code ${executionResult.get().exitValue})") }
}
