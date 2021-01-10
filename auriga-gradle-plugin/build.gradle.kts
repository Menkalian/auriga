plugins {
    kotlin("jvm")
    kotlin("kapt")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.12.0"
    `maven-publish`
}

group = "de.menkalian.auriga"
version = "1.0.0"

if (System.getenv("CI_COMMIT_BRANCH") != "main" && System.getenv("CI_COMMIT_BRANCH") != null) {
    version = "${version}-${System.getenv("CI_COMMIT_BRANCH")}-SNAPSHOT"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "artifactory-menkalian"
        url = uri("http://server.menkalian.de:8081/artifactory/auriga")
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

pluginBundle {
    website = "https://gitlab.com/kiliankra/auriga"
    vcsUrl = "https://gitlab.com/kiliankra/auriga.git"
    tags = listOf("auriga", "logging", "debug", "tool", "kotlin", "java")
}

gradlePlugin {
    plugins {
        create("aurigaPlugin") {
            id = "de.menkalian.auriga"
            displayName = "Auriga Plugin"
            description = "A plugin that applies the Auriga tools to your projects."
            implementationClass = "de.menkalian.auriga.AurigaGradlePlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("http://server.menkalian.de:8081/artifactory/auriga")
            name = "artifactory-menkalian"
            credentials {
                username = System.getenv("MAVEN_REPO_USER")
                password = System.getenv("MAVEN_REPO_PASS")
            }
        }
    }
}


dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("gradle-plugin-api"))
    implementation("de.menkalian.auriga:auriga-kotlin-plugin:$version")
    implementation("de.menkalian.auriga:auriga-config:$version")

    compileOnly("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}
