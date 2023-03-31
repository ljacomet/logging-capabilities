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
package dev.jacomet.gradle.plugins.logging

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

abstract class AbstractLoggingCapabilitiesPluginFunctionalTest extends Specification {

    static GradleVersion testGradleVersion = System.getProperty("test.gradle-version")?.with { GradleVersion.version(it) } ?: GradleVersion.current()

    @TempDir
    Path testFolder
    File buildFile

    def setup() {
        buildFile = testFolder.resolve('build.gradle.kts').toFile()
        testFolder.resolve('settings.gradle.kts').toFile() << 'rootProject.name = "test-project"'
    }

    TaskOutcome outcomeOf(BuildResult result, String path) {
        result.task(path)?.outcome
    }

    BuildResult build(List<String> args) {
        gradleRunnerFor(args).build()
    }

    BuildResult buildAndFail(List<String> args) {
        gradleRunnerFor(args).buildAndFail()
    }

    GradleRunner gradleRunnerFor(List<String>  args) {
        GradleRunner.create()
                .forwardOutput()
                .withGradleVersion(testGradleVersion.version)
                .withPluginClasspath()
                .withProjectDir(testFolder.toFile())
                .withArguments(args + ["-s"])
    }

    void withBuildScript(String content) {
        buildFile << content
    }

    void withBuildScriptWithDependencies(String... dependencies) {
        buildFile << """
            plugins {
                `java-library`
                id("dev.jacomet.logging-capabilities")
            }

            repositories {
                mavenCentral()
            }

            dependencies {
${dependencies.collect { "                implementation(\"$it\")" }.join("\n")}
            }

            tasks.register("doIt") {
                doLast {
                    println(configurations["compileClasspath"].files)
                }
            }
        """
    }
}
