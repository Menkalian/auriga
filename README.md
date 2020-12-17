# Auriga Log-Generating Processor

## Usage

### Gradle + Kotlin (+ Java)

#### Kotlin-DSL:

```kotlin
plugins {
    /*...*/
    kotlin("kapt") version "$KOTLIN_VERSION"
}

dependencies {
    /*...*/
    implementation("de.menkalian:logprocessor:1.0-SNAPSHOT")
    kapt("de.menkalian:logprocessor:1.0-SNAPSHOT")
}
```

#### Groovy-DSL:

```groovy
TODO
```

### Gradle + Java (no Kotlin)

#### Kotlin-DSL:

```kotlin
dependencies {
    /*...*/
    implementation("de.menkalian:logprocessor:1.0-SNAPSHOT")
    annotationProcessor("de.menkalian:logprocessor:1.0-SNAPSHOT")
}
```

#### Groovy-DSL:

```groovy
TODO
```

### Maven

```xml
<dependencies>
    <!-- ... -->
    <dependency>
        <groupId>de.menkalian</groupId>
        <artifactId>logprocessor</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

TODO: Check with Kotlin
