plugins {
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish`
}

group = "de.menkalian.auriga"
version = "1.0.0"

if (System.getenv("CI_COMMIT_BRANCH") != "main" && System.getenv("CI_COMMIT_BRANCH") != null) {
    version = "${version}-${System.getenv("CI_COMMIT_BRANCH")}-SNAPSHOT"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
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
    mavenLocal()
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
    implementation(kotlin("reflect"))
    compileOnly(kotlin("compiler-embeddable"))

    implementation("de.menkalian.auriga:auriga-annotations:$version")
    implementation("de.menkalian.auriga:auriga-config:$version")

    compileOnly("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}
