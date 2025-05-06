import java.net.URI

plugins {
    kotlin("jvm") version "2.1.20"
    id("maven-publish")
}

group = "dev.emirman.lib"
version = "1.0.0-SNAPSHOT"

val telegramBotsVersion = "8.3.0"
val jacksonVersion = "2.19.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("org.telegram:telegrambots-client:$telegramBotsVersion")
    implementation("org.telegram:telegrambots-springboot-longpolling-starter:$telegramBotsVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.5")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

publishing {
    repositories {
        maven {
            val releasesRepoUrl = "https://repo.emirman.dev/repository/maven-releases/"
            val snapshotsRepoUrl = "https://repo.emirman.dev/repository/maven-snapshots/"
            url = if (version.toString().endsWith("SNAPSHOT")) URI(snapshotsRepoUrl) else URI(releasesRepoUrl)
            credentials {
                username = System.getenv("REPO_USER") as String
                password = System.getenv("REPO_KEY") as String
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            // Add the sources jar
            artifact(tasks.named("kotlinSourcesJar"))
        }
    }
}

