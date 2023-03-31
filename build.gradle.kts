/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.util.GradleVersion

plugins {
    groovy
    id("com.gradle.plugin-publish") version "1.1.0"
    dev.jacomet.build.functional
    id("com.github.hierynomus.license") version "0.16.1"
    signing
    id("dev.sigstore.sign") version "0.4.0"
}

repositories {
    mavenCentral()
}

group = "dev.jacomet.gradle.plugins"
version = "0.12.0-dev"

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(gradleApi())

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

gradlePlugin {
    plugins {
        website.set("https://github.com/ljacomet/logging-capabilities")
        vcsUrl.set("https://github.com/ljacomet/logging-capabilities.git")
        create("logging-capabilities") {
            id = "dev.jacomet.logging-capabilities"
            implementationClass = "dev.jacomet.gradle.plugins.logging.LoggingCapabilitiesPlugin"
            displayName = "Logging libraries capabilities"
            description = """Release notes:
                |* Gradle 8 compatibility
                |* Add capabilities to spring-jcl and log4j-slf4j2-impl
            """.trimMargin()
            tags.set(listOf("dependency", "dependencies", "dependency-management", "logging", "slf4j", "log4j2"))
        }
    }
}

publishing {
    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    useGpgCmd()
}

license {
    header = rootProject.file("config/HEADER.txt")
    strictCheck = true
    ignoreFailures = true
    mapping(mapOf(
        "java"   to "SLASHSTAR_STYLE",
        "kt"     to "SLASHSTAR_STYLE",
        "groovy" to "SLASHSTAR_STYLE",
        "kts"    to "SLASHSTAR_STYLE"
    ))
    ext.set("year", "2023")
    exclude("**/build/*")
    exclude("**/.gradle/*")
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }
    functionalTest {
        systemProperty("test.gradle-version", project.findProperty("test.gradle-version") ?: GradleVersion.current().version)
    }
}
