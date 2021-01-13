# Auriga Log-Generating Processor

Auriga is a tool, that helps you to generate Logging-Messages for Java (and Kotlin). These may be used for debugging or just improve log-amount in general.

The user experience is the best if you use **Gradle** since there is a dedicated plugin that takes care of the necessary configurations.

## Usage

### Gradle

Usage in Gradle is as easy as possible by using the `auriga-gradle-plugin`. To use it you have to add the auriga-repository to your plugin repositories. To do this you have to add the following to your `settings.gradle`:

```groovy
pluginManagement {
    repositories {
        maven {
            url "http://server.menkalian.de:8081/artifactory/auriga"
            name "artifactory-menkalian"
        }
        gradlePluginPortal()
    }
}
```

and apply the plugin in your `build.gradle`:

```groovy
plugins {
    /*other plugins...*/
    id 'de.menkalian.auriga' version '1.0.1'
}
```

You can configure Auriga via Gradle. The Syntax is like this (equivalent for Kotlin-DSL):

```groovy
auriga {
    base = 'PRINT'
    logger {
        source = 'org.slf4j.LoggerFactory.getLogger("AURIGA")'
    }
}
```

### Gradle (Kotlin DSL)

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("http://server.menkalian.de:8081/artifactory/auriga")
            name = "artifactory-menkalian"
        }
        gradlePluginPortal()
    }
}
```

`build.gradle.kts`:

```groovy
plugins {
    /*other plugins...*/
    id("de.menkalian.auriga") version "1.0.1"
}
```

### Maven

For Maven only Java is supported (in theory Kotlin could be possible, but I can't be bothered. Just use Gradle for Kotlin)

```xml

<project>
    <dependencies>
        <!-- ... -->
        <dependency>
            <groupId>de.menkalian.auriga</groupId>
            <artifactId>auriga-annotations</artifactId>
            <version>1.0.1</version>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>menkalian-artifactory-auriga</id>
            <name>artifactory-auriga</name>
            <url>http://server.menkalian.de:8081/artifactory/auriga</url>
        </repository>
    </repositories>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>de.menkalian.auriga</groupId>
                            <artifactId>auriga-java-processor</artifactId>
                            <version>1.0.1</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <!--...-->
        </plugins>
    </build>

    <!--...-->
</project>
```

With maven you have to configure Auriga using the config-file

## Configuration

Auriga may be configured via gradle or config-file. You may also use Commandline parameters for direct invocation. If you wish to do so you have to figure that out for yourself (the keys are the same as they are for the configuration-file).

The following values are available to configure
(for gradle these values have to be places inside of the `auriga-Closure` or prefixed with `auriga.`)


| key (config)                      | key (gradle)                  | Example            | Description |
|-----------------------------------|-------------------------------|--------------------|-------------|
| **auriga.config.type**            | `type`                        | `FILE` `ARGS`      |The way the config is handed to  the processor (mainly to use a file with gradle)
| **auriga.config.base**            | `base`                        | `PRINT` `SLF4J`   | The base configuration to be used. Currently *PRINT* (For regular print outputs) and *SLF4J* are supported.
| **auriga.config.location**        | `location`                    | `auriga_cfg.xml`   | Location of your config-file (used if `type` == FILE)
| **auriga.logger.type**            | `loggerConfig.type`           | `NONE` `SLF4J`   | Type of Logger to use. Currently choosing `NONE` results in no Logger generation. Used to set defaults for clazz and source
| **auriga.logger.clazz**           | `loggerConfig.clazz`          | `org.slf4j.Logger` | Fully qualified classname of the Logger to use
| **auriga.logger.source**          | `loggerConfig.source`         | `org.slf4j.LoggerFactory.getLogger("Auriga")` | How to get the Logger for a class. The Placeholder `{{CLASS}}` will be replaced with the Classname
| **auriga.logging.mode**           | `loggingConfig.mode`          | `DEFAULT_ON` `DEFAULT_OFF` | Configures the default behaviour for auriga. This behaviour will then be overwritten by the `@Log` and `@NoLog` Annotations
| **auriga.logging.method**         | `loggingConfig.method`        | `System.out.printf` `log.debug` | The Method invoked for logging
| **auriga.logging.placeholder**    | `loggingConfig.placeholder`   | `PRINTF` `SLF4J` `NONE` | What kind of Placeholder is supported by the logging-environment. There are 3 types supported: "PRINTF" with placeholders like `%s`, "SLF4J"(`{}`) or "NONE" (Strings will be concatenated before)
| **auriga.logging.template.entry** | `loggingConfig.entryTemplate` | "Entering {{METHOD}}" | Template for logging method-entries. Supported placeholders are listed below
| **auriga.logging.template.param** | `loggingConfig.paramTemplate` | "{{PARAM_NAME}}={{PARAM_VALUE}}" | Template for logging method-parameters. Placeholders are `{{PARAM_NAME}}`, `{{PARAM_VALUE}}` and `{{PARAM_TYPE}}`

#### Supported Placeholders for *auriga.logging.template.entry*

|Placeholder    | Description |
|---------------|-------------|
| `{{CLASS}}`   | Replaced with the name of the parental class
| `{{METHOD}}`  | Replaced with the name of the method
| `{{THIS}}`    | Replaced with a call to `toString` of the current object (or the classname if the method is static)
| `{{PARAMS}}`  | Replaced with a Listing of all params following the value of *auriga.logging.template.param*

### Example File

If there is no File at the specified location there will be one generated.
Default location: `{projectDir}/auriga_cfg.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <comment>GENERATED FILE BY AURIGA</comment>
    <entry key="auriga.logging.template.param">{{PARAM_NAME}} : {{PARAM_TYPE}} = {{PARAM_VALUE}}
    </entry>
    <entry key="auriga.logging.template.entry">Executing {{CLASS}}:{{METHOD}} with Params: {
        {{PARAMS}}
        }
    </entry>
    <entry key="auriga.config.base">SLF4J</entry>
</properties>
```

### Example Gradle Configuration

```groovy
auriga {
    base 'SLF4J'
    loggerConfig {
        source 'org.slf4j.LoggerFactory.getLogger("AURIGA")'
    }
}
```
