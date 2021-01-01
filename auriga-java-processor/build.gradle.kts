plugins {
    java
    `maven-publish`
}

group = "de.menkalian.auriga"
version = "1.0.0"

if (System.getenv("CI_COMMIT_BRANCH") != "main" && System.getenv("CI_COMMIT_BRANCH") != null) {
    version = "${version}-${System.getenv("CI_COMMIT_BRANCH")}-SNAPSHOT"
}

java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

tasks.withType(JavaCompile::class) {
    options.compilerArgs.addAll(
        listOf(
            "-proc:none",
            "--add-exports",
            "jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
            "--add-exports",
            "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
            "--add-exports",
            "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-exports",
            "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
            "--add-exports",
            "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"
        )
    )
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

dependencies {
    implementation("de.menkalian.auriga:auriga-annotations:$version")
}
