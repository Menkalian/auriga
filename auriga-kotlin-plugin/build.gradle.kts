plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
    `maven-publish`
}

group = "de.menkalian.auriga"
version = "1.0.0"

if (System.getenv("CI_COMMIT_BRANCH") != "main" && System.getenv("CI_COMMIT_BRANCH") != null) {
    version = "${version}-${System.getenv("CI_COMMIT_BRANCH")}-SNAPSHOT"
}

repositories {
    mavenCentral()
}

publishing {
    repositories {
        maven {
            url = uri("http://server.menkalian.de:8081/artifactory/auriga")
            name = "artifactory-menkalian"
            authentication {
                credentials {
                    username = System.getenv("MAVEN_REPO_USER")
                    password = System.getenv("MAVEN_REPO_PASS")
                }
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

kapt {
    includeCompileClasspath = false
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(kotlin("compiler-embeddable"))

    compileOnly("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}
