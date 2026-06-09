plugins {
    kotlin("jvm") version("2.3.0")
    id("java")
    id("maven-publish")
}


group = "net.mcbrawls"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    api("net.minestom:minestom:2026.06.05-26.1.2")
    api("org.slf4j:slf4j-api:2.0.17")
}

kotlin {
    jvmToolchain(25)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
        }
    }

    repositories {
        val mavenUrl = System.getenv("MAVEN_URL")
        if (mavenUrl != null) {
            maven {
                name = "envmaven"
                url = uri(mavenUrl)
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }
    }
}
