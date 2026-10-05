import org.gradle.api.tasks.testing.logging.TestExceptionFormat

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

val remoteUser = providers.gradleProperty("remoteUser")
    .orElse(providers.environmentVariable("REMOTE_USER"))
    .orElse(providers.systemProperty("user.name"))

val remoteHost = providers.gradleProperty("remoteHost")
    .orElse(providers.environmentVariable("REMOTE_HOST"))
    .orElse("localhost")

val remotePort = providers.gradleProperty("remotePort")
    .orElse(providers.environmentVariable("REMOTE_PORT"))
    .map(String::toInt)
    .orElse(22)

val remoteKeyPath = providers.gradleProperty("remoteKeyPath")
    .orElse(providers.environmentVariable("REMOTE_KEY_PATH"))

tasks.register<Exec>("uploadServer") {
    group = "deployment"
    description = "Uploads the shadow JAR to the remote server using `scp`."

    dependsOn(tasks.shadowJar)

    doFirst {
        val archiveFile = tasks.shadowJar.get().archiveFile.get().asFile

        val identity = remoteKeyPath.orNull?.let { path ->
            when {
                path == "~" -> File(System.getProperty("user.home"))
                path.startsWith("~/") -> File(System.getProperty("user.home"), path.substring(2))
                else -> File(path)
            }
        }

        val command = mutableListOf("scp", "-P", remotePort.get().toString())
        if (identity != null) command += listOf("-i", identity.absolutePath)

        command += listOf(archiveFile.absolutePath, "${remoteUser.get()}@${remoteHost.get()}:.")

        commandLine(command)

        logger.lifecycle("Uploading ${archiveFile.name} to ${remoteUser.get()}@${remoteHost.get()}...")
    }
}
